package org.ems.service;

import static com.ems.util.EMSUtility.DD_MM_YYYY_HH_MM_S;
import static com.ems.util.EMSUtility.parseDateTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.ems.cache.CacheUtil;
import org.ems.dao.DeviceDetailsDAO;
import org.ems.model.DateRangeReportForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.swingworkers.ExcelReportWorker;
import com.ems.UI.swingworkers.SummaryWokerMonitor;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.constants.EmsConstants;
import com.ems.util.EMSUtility;

@Service
public class ReportService implements ApplicationContextAware, DisposableBean {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	private ApplicationContext applicationContext;

	@Autowired
	private CacheUtil cacheUtil;

	@Autowired
	private DeviceDetailsDAO deviceDetailsDAO;

	@Override
	public void destroy() throws Exception {

	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public List<DeviceDetailsDTO> fetchActiveDevices() {
		return deviceDetailsDAO.fetchActiveDeviceDetails();
	}

	public InputStream createReport(DateRangeReportForm form) throws Exception {

		String deviceNames = form.getDeviceName();
		String memoryMappings = form.getMemoryMappingDetails();
		long startTime = parseDateTime(form.getReportStartTime(), DD_MM_YYYY_HH_MM_S);
		long endTime = parseDateTime(form.getReportEndTime(), DD_MM_YYYY_HH_MM_S);

		String[] deviceIdArray = deviceNames.split(",");
		final List<Long> deviceIds = Arrays.asList(deviceIdArray).stream().map(value -> Long.parseLong(value))
				.collect(Collectors.toList());

		List<DeviceDetailsDTO> selectedDevices = fetchActiveDevices().stream()
				.filter(device -> deviceIds.contains(device.getUniqueId())).collect(Collectors.toList());

		selectedDevices.stream().forEach(device -> {
			device.setStartTime(startTime).setEndTime(endTime).setRecordCount(1);
			setMemoryMappingDetails(device, false, memoryMappings.split(","));
		});

		/*SummaryWokerMonitor worker = applicationContext.getBean(SummaryWokerMonitor.class)
				.setReportDeviceList(selectedDevices);
		Future<Object> excelReport = ConcurrencyUtils.execute(worker);
		ByteArrayOutputStream stream = (ByteArrayOutputStream) excelReport.get();*/
		
		ExcelReportWorker worker = applicationContext.getBean(ExcelReportWorker.class)
				.setReportDeviceList(selectedDevices);
		Future<Object> excelReport = ConcurrencyUtils.execute(worker);
		ByteArrayOutputStream stream = (ByteArrayOutputStream) excelReport.get();

		return new ByteArrayInputStream(stream.toByteArray());
	}

	private void setMemoryMappingDetails(DeviceDetailsDTO device, boolean allMemory, String[] selected) {
		if (device != null) {
			Properties props = EMSUtility.loadProperties(device.getMemoryMapping());
			props.remove(EmsConstants.SPLIT_JOIN.split("=")[0]);

			if (allMemory) {
				device.setReportMapping(props);
			} else {
				Properties temp = new Properties();

				if (selected != null) {

					ArrayList<String> list = new ArrayList<>();
					list.addAll(Arrays.asList(selected));

					for (Entry<Object, Object> entry : props.entrySet()) {
						String value = entry.getValue().toString().trim();

						if (!value.equalsIgnoreCase(EmsConstants.NO_MAP) && list.contains(value)) {
							temp.putIfAbsent(entry.getKey(), entry.getValue());
						}
					}
				}

				device.setReportMapping(temp);
			}
		}
	}

}
