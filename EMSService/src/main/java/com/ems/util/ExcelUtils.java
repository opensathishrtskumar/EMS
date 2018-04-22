package com.ems.util;

import static com.ems.constants.QueryConstants.RETRIEVE_DEVICE_STATE;
import static com.ems.util.EMSUtility.REPORTNAME_FORMAT;
import static com.ems.util.EMSUtility.getFormattedDate;
import static com.ems.util.EMSUtility.getOrderedProperties;
import static com.ems.util.EMSUtility.loadProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.EmsConstants;
import com.ems.db.DBConnectionManager;

public abstract class ExcelUtils {
	private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

	public static Map<String, String> getMap(Properties props) {
		Map<String, String> map = new TreeMap<String, String>((Map) props);
		return map;
	}

	public static HSSFWorkbook createWorkBook() {
		HSSFWorkbook workBook = new HSSFWorkbook();
		return workBook;
	}

	public static HSSFSheet createWorkSheet(HSSFWorkbook workBook, String sheetName) {

		HSSFFont font = workBook.createFont();
		font.setFontHeightInPoints((short) 9);
		font.setFontName("Arial");
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		font.setItalic(false);

		HSSFCellStyle headerStyle = workBook.createCellStyle();
		headerStyle.setFont(font);

		HSSFSheet sheet = workBook.createSheet(sheetName);

		return sheet;
	}

	public static HSSFSheet createWorkSheet(HSSFWorkbook workBook, String sheetName, Map<String, String> headers) {

		HSSFFont font = workBook.createFont();
		font.setFontHeightInPoints((short) 9);
		font.setFontName("Arial");
		font.setColor(IndexedColors.BLACK.getIndex());
		font.setBold(true);
		font.setItalic(false);

		HSSFCellStyle headerStyle = workBook.createCellStyle();
		headerStyle.setFont(font);

		HSSFSheet sheet = workBook.createSheet(sheetName);
		HSSFRow row = sheet.createRow(0);
		int column = 0;
		for (Entry<String, String> entry : headers.entrySet()) {
			// Skip memory mapping record whose value is "NoMap"
			if (!EmsConstants.NO_MAP.equalsIgnoreCase(entry.getValue().trim())) {
				sheet.autoSizeColumn(column);
				HSSFCell cell = row.createCell(column);
				cell.setCellValue(entry.getValue());// Register mapping name
				cell.setCellStyle(headerStyle);
				column += 1;
			}
		}

		return sheet;
	}

	public static Workbook writeReadingsToWorkBook(String filePath, ExtendedSerialParameter device, ResultSet result)
			throws IOException {
		// All the values becomes header of column
		Map<String, String> headers = createReportHeaderMap(device);
		HSSFWorkbook workBook = createWorkBook();
		HSSFSheet sheet = createWorkSheet(workBook, device.getDeviceName(), headers);
		sheet = writeResultToSheet(device, result, sheet);

		workBook.write(new File(filePath));

		return workBook;
	}

	public static Map<String, String> createReportHeaderMap(ExtendedSerialParameter device) {
		// All the values becomes header of column
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Polled on", "Time");
		// Keep the order of properties
		Map<String, String> memoryMap = getOrderedProperties(device);
		headers.putAll(memoryMap);
		return headers;
	}

	public static HSSFSheet writeResultToSheet(ExtendedSerialParameter device, ResultSet result, HSSFSheet sheet) {
		// Keep the order of properties
		Map<String, String> memoryMap = getOrderedProperties(device);
		// All the values becomes header of column
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Polled on", "Time");
		headers.putAll(memoryMap);

		if (result != null) {
			try {
				// Firt row reserved for headers so start with 1
				for (int rowIndex = 1; result.next(); rowIndex++) {
					HSSFRow row = sheet.createRow(rowIndex);
					writeReadingsRow(row, result.getString("formatteddate"), result.getString("unitresponse"),
							memoryMap);
				}
			} catch (Exception e) {
				logger.error("Report write to excel failed for device {} : {}", device, e);
			}
		}

		return sheet;
	}

	public static HSSFSheet writeResultToSheet(ExtendedSerialParameter device, List<PollingDetailDTO> unitData,
			HSSFSheet sheet) {
		// Keep the order of properties
		Map<String, String> memoryMap = getOrderedProperties(device);
		Map<String, String> deviceHeaders = device.getHeaders();
		if (deviceHeaders != null) {
			memoryMap = deviceHeaders;
		}

		// All the values becomes header of column
		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("Polled on", "Time");
		headers.putAll(memoryMap);

		if (unitData != null) {
			try {
				// Firt row reserved for headers so start with 1
				for (int rowIndex = 1; rowIndex < unitData.size(); rowIndex++) {
					HSSFRow row = sheet.createRow(rowIndex);
					writeReadingsRow(row, unitData.get(rowIndex - 1).getFormattedDate(),
							unitData.get(rowIndex - 1).getUnitresponse(), memoryMap);
				}
			} catch (Exception e) {
				logger.error("Report write to excel failed for device {} : {}", device, e);
			}
		}

		return sheet;
	}

	public static void writeReadingsRow(HSSFRow row, String formattedDate, String unitResponse,
			Map<String, String> headers) {

		int columnIndex = 0;
		HSSFCell dateCell = row.createCell(columnIndex++);
		dateCell.setCellValue(formattedDate);

		Properties readingMap = loadProperties(unitResponse);
		for (Entry<String, String> entry : headers.entrySet()) {
			// Skip memory mapping record whose value is "NoMap"
			if (!EmsConstants.NO_MAP.equalsIgnoreCase(entry.getValue().trim())) {
				HSSFCell readingCell = row.createCell(columnIndex);
				readingCell.setCellValue(readingMap.getProperty(String.valueOf(entry.getKey()), "0.0"));
				columnIndex += 1;
			}
		}
	}

	public static String prepareUnitData(long startDate, long endDate, DeviceDetailsDTO detailsDTO) {
		Connection connection = DBConnectionManager.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		String fileName = detailsDTO.getDeviceName() + getFormattedDate(REPORTNAME_FORMAT) + ".xls";
		fileName = new File(fileName).getAbsolutePath();

		try {
			ps = connection.prepareStatement(RETRIEVE_DEVICE_STATE);
			ps.setLong(1, detailsDTO.getUniqueId());
			ps.setLong(2, startDate);
			ps.setLong(3, endDate);
			rs = ps.executeQuery();

			ExtendedSerialParameter device = EMSUtility.mapDeviceToSerialParam(detailsDTO);
			ExcelUtils.writeReadingsToWorkBook(fileName, device, rs);

		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Report data fetching failed : {}", e.getLocalizedMessage());
		} finally {
			DBConnectionManager.closeConnections(connection, ps, rs);
		}
		return fileName;
	}

	public static String compressFile(File sourceFile) {
		String compressedFile = null;

		try {
			String zipFile = sourceFile.getAbsolutePath() + ".zip";

			byte[] buffer = new byte[1024];
			FileOutputStream fos = new FileOutputStream(zipFile);
			ZipOutputStream zos = new ZipOutputStream(fos);

			FileInputStream fis = new FileInputStream(sourceFile);
			zos.putNextEntry(new ZipEntry(sourceFile.getName()));
			int length;
			while ((length = fis.read(buffer)) > 0) {
				zos.write(buffer, 0, length);
			}
			zos.closeEntry();
			fis.close();
			zos.close();

			compressedFile = zipFile;

		} catch (IOException ioe) {
			logger.debug("zip file is not found");
		}
		return compressedFile;
	}

	public static void main(String[] args) {
		compressFile(new File("F:\\Reports\\01-Feb-2018-Report.xls"));
	}
}
