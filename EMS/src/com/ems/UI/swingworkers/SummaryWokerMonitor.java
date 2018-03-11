package com.ems.UI.swingworkers;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.helpers.HSSFRowShifter;
import org.apache.poi.ss.usermodel.CellType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.UI.dto.SummaryReportDTO;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.util.EMSUtility;
import com.ems.util.ExcelUtils;

public class SummaryWokerMonitor implements Callable<Object> {

	private static final Logger logger = LoggerFactory.getLogger(SummaryWokerMonitor.class);
	List<DeviceDetailsDTO> reportDeviceList;
	private static final int CHUNK = 4;

	public SummaryWokerMonitor(List<DeviceDetailsDTO> reportDeviceList) {
		this.reportDeviceList = reportDeviceList;
	}

	@Override
	public String call() throws Exception {
		String date = EMSUtility.getFormattedTime(System.currentTimeMillis(), EMSUtility.EXCEL_REPORTNAME_FORMAT);
		File file = new File(date + ".xls");

		ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(
				ConcurrencyUtils.getWorkerPool());

		logger.trace("Completion service created for summary workers...");
		String reportMsg = "Summary Report created at " + file.getAbsolutePath();
		
		try {
			int taskSize = reportDeviceList.size();
			DeviceDetailsDTO devices[] = reportDeviceList.toArray(new DeviceDetailsDTO[taskSize]);

			int taskSubmitted = 0;

			for (int i = 0; i < taskSize; i = i + CHUNK, taskSubmitted++) {
				DeviceDetailsDTO[] slice = Arrays.copyOfRange(devices, i, i + CHUNK);
				logger.trace("begin {} end {}", i, i + CHUNK);
				completionService.submit(new SummaryWorker(slice));
			}

			// Create excel work book and write response in respective sheet

			HSSFWorkbook workBook = ExcelUtils.createWorkBook();
			
			for (int j = 0; j < taskSubmitted; j++) {

				try {
					Future<Object> response = completionService.take();
					Map<DeviceDetailsDTO, List<SummaryReportDTO>> workerResponse = (Map<DeviceDetailsDTO, List<SummaryReportDTO>>)response.get();
					
					for(Entry<DeviceDetailsDTO, List<SummaryReportDTO>> entry : workerResponse.entrySet()){
						
						DeviceDetailsDTO device = entry.getKey();
						List<SummaryReportDTO> reportData = entry.getValue();
						
						HSSFSheet sheet = ExcelUtils.createWorkSheet(workBook, device.getDeviceName());
						
						writerSummary(sheet, reportData);
						
					}
					
				} catch (Exception e) {
					logger.error("Error Waiting for completion service ", e);
				}
			}

			workBook.write(file);
			
		} catch (Exception e) {
			logger.error("Error creating excel report : {}", e);
			reportMsg = "Summary Report creation failed, Please try again." + e.getMessage() ;
		}

		return reportMsg;
	}
	
	
	private void writerSummary(HSSFSheet sheet, List<SummaryReportDTO> list){
		
		int startingRow = 5;
		int startingColumn = 1;
		
		HSSFRow header = sheet.createRow(startingRow);//Header row
		HSSFRow minRow = sheet.createRow(startingRow + 1);// Min Value row
		HSSFRow maxRow = sheet.createRow(startingRow + 2);// max Value row
		HSSFRow averageRow = sheet.createRow(startingRow + 3);// average Value row
		
		minRow.createCell(startingColumn - 1, CellType.STRING).setCellValue("Minimum");
		maxRow.createCell(startingColumn - 1, CellType.STRING).setCellValue("Maximum");
		averageRow.createCell(startingColumn - 1, CellType.STRING).setCellValue("Average");

		HSSFFont headerFont = sheet.getWorkbook().createFont();
		headerFont.setBold(true);
		headerFont.setItalic(true);
		headerFont.setFontName("Arial");
		
		HSSFCellStyle style = sheet.getWorkbook().createCellStyle();
		style.setFont(headerFont);
		style.setAlignment(style.ALIGN_CENTER);
		
		header.setRowStyle(style);
		
		for(SummaryReportDTO entry : list){
			
			String minimum = entry.getMinimum() + " at " + entry.getMinimumTimeStamp();
			String maximum = entry.getMaximum() + " at " + entry.getMaximumTimeStamp();
			String average = entry.getAverage() + "";
			String memoryName = entry.getMemoryAddressName();
			
			header.createCell(startingColumn, CellType.STRING).setCellValue(memoryName);
			minRow.createCell(startingColumn, CellType.STRING).setCellValue(minimum);
			maxRow.createCell(startingColumn, CellType.STRING).setCellValue(maximum);
			averageRow.createCell(startingColumn, CellType.STRING).setCellValue(average);
			
			startingColumn += 1;
		}
		
	}
}
