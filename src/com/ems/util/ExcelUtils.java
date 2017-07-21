package com.ems.util;

import static com.ems.util.EMSUtility.getOrderedProperties;
import static com.ems.util.EMSUtility.loadProperties;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.constants.EmsConstants;

public abstract class ExcelUtils {
	private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
	
	public static Map<String,String> getMap(Properties props){
		Map<String, String> map = new TreeMap<String,String>((Map)props);
		return map;
	}
	
	public static HSSFWorkbook createWorkBook(String sheetName, Map<String,String> headers){
		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet sheet = workBook.createSheet(sheetName);
		HSSFRow row = sheet.createRow(0);
		int column = 0;
		
		HSSFFont font = workBook.createFont();
		font.setFontHeightInPoints((short)9);
		font.setFontName("Arial");
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		font.setItalic(false);
		
		HSSFCellStyle header = workBook.createCellStyle();
		header.setFont(font);
		
		for(Entry<String, String> entry : headers.entrySet()){
			//Skip memory mapping record whose value is "NoMap"
			if(!EmsConstants.NO_MAP.equalsIgnoreCase(entry.getValue().trim())){
				sheet.autoSizeColumn(column);
				HSSFCell cell = row.createCell(column);
				StringBuilder builder = new StringBuilder();
				builder.append(entry.getValue());
				builder.append("(");
				builder.append(entry.getKey());
				builder.append(")");
				cell.setCellValue(builder.toString());
				cell.setCellStyle(header);
				column += 1;
			}
		}
		
		return workBook;
	}
	
	public static Workbook writeReadingsToWorkBook(String filePath, ExtendedSerialParameter device, ResultSet result)
			throws IOException {
		//Keep the order of properties
		Map<String, String> memoryMap = getOrderedProperties(device);
		
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Polled on","Date");
		headers.putAll(memoryMap);
		HSSFWorkbook workBook = createWorkBook(device.getDeviceName(), headers);
		HSSFSheet sheet = workBook.getSheet(device.getDeviceName());
		
		for(int i=0;i<headers.size();i++){
			sheet.autoSizeColumn(i);
		}
		
		if(result != null){
			try {
				//Firt row reserved for headers
				int rowIndex = 1;
				while(result.next()){
					HSSFRow row = sheet.createRow(rowIndex++);
					writeReadingsRow(row, result.getString("formatteddate"),
							result.getString("unitresponse"), memoryMap);
				}
			} catch (Exception e) {
				logger.error("Report export to excel failed : {}",e.getLocalizedMessage());
				logger.error("{}",e);
			}
		}
		
		workBook.write(new File(filePath));
		
		return workBook;
	}
	
	private static void writeReadingsRow(HSSFRow row, String formattedDate,
			String unitResponse, Map<String, String> headers) {
		
		int columnIndex = 0;
		HSSFCell dateCell = row.createCell(columnIndex++);
		dateCell.setCellValue(formattedDate);
		StringBuilder builder = new StringBuilder(unitResponse);
		
		Properties readingMap = loadProperties(builder.toString());
		for(Entry<String, String> entry : headers.entrySet()){
			//Skip memory mapping record whose value is "NoMap"
			if(!EmsConstants.NO_MAP.equalsIgnoreCase(entry.getValue().trim())){
				HSSFCell readingCell = row.createCell(columnIndex);
				readingCell.setCellValue(readingMap.getProperty(String.valueOf(Integer.valueOf(entry.getKey())), "0.0"));
				columnIndex += 1;
			}
		}
	}
}
