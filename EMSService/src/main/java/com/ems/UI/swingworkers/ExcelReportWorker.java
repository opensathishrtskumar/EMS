package com.ems.UI.swingworkers;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.util.EMSUtility;
import com.ems.util.ExcelUtils;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExcelReportWorker implements Callable<Object> {

	private static final Logger logger = LoggerFactory.getLogger(ExcelReportWorker.class);
	List<DeviceDetailsDTO> reportDeviceList;
	private static final int CHUNK = 4;

	public ExcelReportWorker(List<DeviceDetailsDTO> reportDeviceList) {
		this.reportDeviceList = reportDeviceList;
	}

	public ExcelReportWorker() {
		//
	}

	public ExcelReportWorker setReportDeviceList(List<DeviceDetailsDTO> reportDeviceList) {
		this.reportDeviceList = reportDeviceList;
		return this;
	}

	@Override
	public ByteArrayOutputStream call() throws Exception {

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(
				ConcurrencyUtils.getWorkers());

		logger.trace("Completion service created for workers...");

		try {
			int taskSize = reportDeviceList.size();
			DeviceDetailsDTO devices[] = reportDeviceList.toArray(new DeviceDetailsDTO[taskSize]);
			int taskSubmitted = 0;

			for (int i = 0; i < taskSize; i = i + CHUNK, taskSubmitted++) {
				DeviceDetailsDTO[] slice = Arrays.copyOfRange(devices, i, i + CHUNK);
				logger.trace("begin {} end {}", i, i + CHUNK);
				completionService.submit(new NewExcelReportWorker(slice));
			}

			// Create excel work book and write response in respective sheet
			HSSFWorkbook workBook = ExcelUtils.createWorkBook();

			for (int j = 0; j < taskSubmitted; j++) {

				try {
					Future<Object> response = completionService.take();
					Map<DeviceDetailsDTO, List<PollingDetailDTO>> workerResponse = (Map<DeviceDetailsDTO, List<PollingDetailDTO>>) response
							.get();

					for (Entry<DeviceDetailsDTO, List<PollingDetailDTO>> entry : workerResponse.entrySet()) {

						DeviceDetailsDTO device = entry.getKey();
						List<PollingDetailDTO> reportData = entry.getValue();
						ExtendedSerialParameter serialDevice = EMSUtility.mapDeviceToSerialParam(device);
						Map<String, String> headers = ExcelUtils.createReportHeaderMap(serialDevice);
						// Write only selected memory
						if (!device.isAllMemory()) {
							Map<String, String> requiredColumn = EMSUtility.convertProp2Map(device.getReportMapping());
							headers.clear();
							headers.put("Polled on", "Time");
							headers.putAll(requiredColumn);
						}

						logger.trace("Selected memory for excel {}", headers);
						HSSFSheet sheet = ExcelUtils.createWorkSheet(workBook, device.getDeviceName(), headers);
						headers.remove("Polled on", "Time");
						serialDevice.setHeaders(headers);
						sheet = ExcelUtils.writeResultToSheet(serialDevice, reportData, sheet);
					}

				} catch (Exception e) {
					logger.error("Error Waiting for completion service ", e);
				}
			}

			workBook.write(stream);

		} catch (Exception e) {
			logger.error("Error creating excel report : {}", e);
		}

		return stream;
	}
}
