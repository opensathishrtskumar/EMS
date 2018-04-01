package com.ems.scheduler;

import static com.ems.constants.QueryConstants.DAILY_CUMULATIVE_REPORT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ems.config.listener.AppContextAware;
import org.ems.dao.DeviceDetailsDAO;
import org.ems.dao.PollingDetailsDAO;
import org.joda.time.LocalDateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.AttachmentDTO;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.EmailDTO;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.constants.EmailConstants;
import com.ems.constants.QueryConstants;
import com.ems.constants.SettingsConstants;
import com.ems.db.DBConnectionManager;
import com.ems.mailer.EmailUtil;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;

public class CumulativeReportJob extends AbstractJob {
	private static final Logger logger = LoggerFactory.getLogger(CumulativeReportJob.class);

	private EmailDTO emailDTO = null;
	private List<DeviceDetailsDTO> devices = null;
	private List<AttachmentDTO> attachments = null;

	private static Map<Integer, Integer[]> excelCoordinates = null;

	static {
		excelCoordinates = new HashMap<>();

		excelCoordinates.put(1, new Integer[] { 7, 2 });
		excelCoordinates.put(2, new Integer[] { 8, 2 });
		excelCoordinates.put(3, new Integer[] { 9, 2 });
		excelCoordinates.put(4, new Integer[] { 10, 2 });
		excelCoordinates.put(5, new Integer[] { 11, 2 });
		excelCoordinates.put(6, new Integer[] { 12, 2 });

		excelCoordinates.put(7, new Integer[] { 7, 4 });
		excelCoordinates.put(8, new Integer[] { 8, 4 });
		excelCoordinates.put(9, new Integer[] { 9, 4 });
		excelCoordinates.put(10, new Integer[] { 10, 4 });
		excelCoordinates.put(11, new Integer[] { 11, 4 });
		excelCoordinates.put(12, new Integer[] { 12, 4 });

		excelCoordinates.put(13, new Integer[] { 7, 6 });
		excelCoordinates.put(14, new Integer[] { 8, 6 });
		excelCoordinates.put(15, new Integer[] { 9, 6 });
		excelCoordinates.put(16, new Integer[] { 10, 6 });
		excelCoordinates.put(17, new Integer[] { 11, 6 });
		excelCoordinates.put(18, new Integer[] { 12, 6 });

		excelCoordinates.put(19, new Integer[] { 7, 8 });
		excelCoordinates.put(20, new Integer[] { 8, 8 });
		excelCoordinates.put(21, new Integer[] { 9, 8 });
		excelCoordinates.put(22, new Integer[] { 10, 8 });
		excelCoordinates.put(23, new Integer[] { 11, 8 });
		excelCoordinates.put(24, new Integer[] { 12, 8 });

		// Sets coordinates for devicename and other fields
		excelCoordinates.put(98, new Integer[] { 3, 4 });// Device name
		excelCoordinates.put(99, new Integer[] { 4, 4 });// Device id
		excelCoordinates.put(100, new Integer[] { 5, 4 });// report Date
		excelCoordinates.put(101, new Integer[] { 13, 4 });// total consumption

	}

	public CumulativeReportJob() {

	}

	@Override
	protected void preProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing required parameters - Daily Summary report...");
		this.emailDTO = ConfigHelper.getEmailDetails();
		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);
		this.devices = dao.fetchAllDeviceDetails(QueryConstants.SELECT_ENABLED_ENDEVICES, new Object[] {});
		this.attachments = new ArrayList<>();
		logger.debug("Initializing required parameters completed - Daily Summary report...");
	}

	@Override
	protected void work(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing report preparation- Daily Summary report...");

		/*
		 * Scheduler will be triggered next day only to send mail in office hours, ie
		 * 10AM . so get one day prior data to send
		 */
		long yesterday = LocalDateTime.now().minusDays(1).toDate().getTime();

		this.emailDTO.setDate(EMSUtility.getFormattedTime(yesterday, "dd-MMM-yyyy"));
		this.emailDTO.setBody(EmailConstants.CUMULATIVE_REPORT);
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFileName("EMSReport-" + this.emailDTO.getDate() + ".xlsx");
		this.attachments.add(attachment);

		long startTime = Helper.getStartOfDay(yesterday);
		// 1 lack added to include next day value so we get 25 values from query
		long endTime = Helper.getEndOfDay(yesterday) + 100000;

		List<ArrayList<DeviceDetailsDTO>> mainList = EMSUtility.split(2, this.devices);

		logger.debug("Daily cumulative report splitted to sublist");

		ArrayList<Future<Object>> futureList = new ArrayList<Future<Object>>();

		for (ArrayList<DeviceDetailsDTO> subList : mainList) {

			DailyCumulativeReportLoader job = new DailyCumulativeReportLoader(subList).setEndTime(endTime)
					.setStartTime(startTime);

			Future<Object> future = ConcurrencyUtils.execute(job);
			logger.debug("Daily cumulative report task submitted {}", subList);
			futureList.add(future);
		}

		Map<DeviceDetailsDTO, List<String>> finalResponse = new LinkedHashMap<>();

		for (Future<Object> future : futureList) {
			try {
				Map<DeviceDetailsDTO, List<String>> result = (Map<DeviceDetailsDTO, List<String>>) future.get();

				finalResponse.putAll(result);
			} catch (Exception e) {
				logger.error("dailty Cumulative report fetching error : {}", e);
			}
		}

		logger.debug("Daily cumulative report final data : {}", finalResponse);

		try {
			createDailyCumulativeReport(finalResponse);
		} catch (Exception e) {
			logger.error("error writing Daily cumulative report {}", e);
		}

		logger.debug("Initializing report preparation completed- Daily Summary report...");
	}

	private void createDailyCumulativeReport(Map<DeviceDetailsDTO, List<String>> cumulativeData)
			throws EncryptedDocumentException, InvalidFormatException, IOException {

		File file = new File(TempDataManager.getTempFolder().getAbsoluteFile() + File.separator + "Daily.xlsx");
		FileInputStream inStream = new FileInputStream(file);
		XSSFWorkbook book = (XSSFWorkbook) WorkbookFactory.create(inStream);
		XSSFSheet template = (XSSFSheet) book.getSheet("Daily");

		for (Entry<DeviceDetailsDTO, List<String>> entry : cumulativeData.entrySet()) {

			try {
				XSSFSheet sheet = book.cloneSheet(book.getSheetIndex(template));
				book.setSheetName(book.getSheetIndex(sheet), entry.getKey().getDeviceName());

				writeContentToSheet(sheet, entry.getKey(), entry.getValue());
			} catch (Exception e) {
				logger.error("sheet copying failed {}", e);
			}
		}

		// Remove template sheet
		book.removeSheetAt(book.getSheetIndex(template));

		DeviceDetailsDAO dao = AppContextAware.getContext().getBean(DeviceDetailsDAO.class);
		String reportPath = dao.getSettings().get(SettingsConstants.reportPath);

		File report = new File(reportPath + File.separator + this.emailDTO.getDate() + "-CumulativeReport.xlsx");
		this.attachments.get(0).setFile(report);

		try (FileOutputStream stream = new FileOutputStream(report)) {
			book.write(stream);
			book.close();
			inStream.close();
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	/**
	 * @param sheet
	 * @param dto
	 * @param summaryData
	 * 
	 *            Assumption that sheet template wont be changes - Cell styles and
	 *            its position
	 */
	private void writeContentToSheet(XSSFSheet sheet, DeviceDetailsDTO dto, List<String> summaryData) {
		// Sets device generic details
		setFeederDetails(sheet, dto);
		// Writes reading details
		setReadingDetails(sheet, dto.getMemoryMapping(), summaryData);
	}

	private void setFeederDetails(XSSFSheet sheet, DeviceDetailsDTO dto) {
		String feederName = dto.getDeviceName();
		int feederId = dto.getDeviceId();
		String reportDate = this.emailDTO.getDate();

		Integer[] nameCO = excelCoordinates.get(98);
		Integer[] idCO = excelCoordinates.get(99);
		Integer[] dateCO = excelCoordinates.get(100);

		sheet.getRow(nameCO[0]).getCell(nameCO[1]).setCellValue(feederName);
		sheet.getRow(idCO[0]).getCell(idCO[1]).setCellValue(feederId);
		sheet.getRow(dateCO[0]).getCell(dateCO[1]).setCellValue(reportDate);
	}

	private void setReadingDetails(XSSFSheet sheet, String memoryMapping, List<String> summaryData) {
		String fwdKWRegister = EMSUtility.getForwarKWMappingRegister(memoryMapping);

		logger.debug("Forward Kilowatt register {} ", fwdKWRegister);
		if (fwdKWRegister != null) {
			TreeMap<Integer, BigDecimal> finalData = new TreeMap<>();

			int count = summaryData.size();

			for (int i = 0; i < count; i++) {

				BigDecimal fwdKW = null;
				String tempValue = "0.00";
				try {
					// get unitresponse
					String value = summaryData.get(i);
					// load as property
					Properties prop = EMSUtility.loadProperties(value);
					// Get Feeded value
					tempValue = prop.getProperty(fwdKWRegister);
				} catch (Exception e) {
					logger.error("{}", e);
				} finally {
					tempValue = tempValue == null ? "0.00" : tempValue;
					fwdKW = new BigDecimal(tempValue);
				}

				finalData.put(i, fwdKW);
			}

			writeHourlyConsumption(sheet, finalData);

			// to set total consumption
			setTotalConsumption(sheet, finalData);

			logger.debug("hour and FwdKWH Map : {}", finalData);
		}
	}

	private void writeHourlyConsumption(XSSFSheet sheet, TreeMap<Integer, BigDecimal> readings) {

		for (int i = 1; i < readings.size(); i++) {
			BigDecimal va = new BigDecimal(1000);
			BigDecimal value = readings.get(i).subtract(readings.get(i - 1)).divide(va);

			Integer[] coordinates = excelCoordinates.get(i);
			sheet.getRow(coordinates[0]).getCell(coordinates[1]).setCellValue(value.toString());
		}
	}

	private void setTotalConsumption(XSSFSheet sheet, TreeMap<Integer, BigDecimal> readings) {
		Integer[] consumptionCO = excelCoordinates.get(101);

		String total = "0.00";

		BigDecimal max = readings.get(readings.size() - 1);
		BigDecimal min = readings.get(0);

		BigDecimal va = new BigDecimal(1000);
		total = max.subtract(min).divide(va).toString();

		sheet.getRow(consumptionCO[0]).getCell(consumptionCO[1]).setCellValue(total);
	}

	@Override
	protected void postProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Trigger mail daily summary report..");
		// Set company name date and so on
		EmailUtil.setEmailDetails(this.emailDTO);
		// Trigger here mail
		this.emailDTO.setAttachments(this.attachments);
	boolean sent = EmailUtil.sendEmail(this.emailDTO);
		logger.debug("mail triggered  for daily summary report..{}");

	}

	final static class DailyCumulativeReportLoader implements Callable<Object> {

		private List<DeviceDetailsDTO> devices = null;
		private long startTime;
		private long endTime;

		public DailyCumulativeReportLoader(List<DeviceDetailsDTO> devices) {
			this.devices = devices;
		}

		public long getStartTime() {
			return startTime;
		}

		public DailyCumulativeReportLoader setStartTime(long startTime) {
			this.startTime = startTime;
			return this;
		}

		public long getEndTime() {
			return endTime;
		}

		public DailyCumulativeReportLoader setEndTime(long endTime) {
			this.endTime = endTime;
			return this;
		}

		@Override
		public Object call() throws Exception {
			Map<DeviceDetailsDTO, List<String>> result = new HashMap<>();

			Connection connection = DBConnectionManager.getConnection();
			connection.setAutoCommit(false);

			PreparedStatement ps = null;
			ResultSet rs = null;

			for (DeviceDetailsDTO device : devices) {
				try {
					ps = connection.prepareStatement(DAILY_CUMULATIVE_REPORT, java.sql.ResultSet.TYPE_FORWARD_ONLY,
							java.sql.ResultSet.CONCUR_READ_ONLY);
					ps.setLong(1, device.getUniqueId());
					ps.setLong(2, startTime);
					ps.setLong(3, endTime);
					ps.setFetchSize(Integer.MIN_VALUE);
					rs = ps.executeQuery();
					List<String> values = new ArrayList<>();

					while (rs.next()) {
						values.add(rs.getString(3));
					}

					result.put(device, values);

				} catch (Exception e) {
					logger.error("error loading daily cumulative report for device {}  {}", device, e);
				} finally {
					DBConnectionManager.closeConnections(null, ps, rs);
				}
			}

			DBConnectionManager.closeConnections(connection, ps, rs);

			return result;
		}

	}

	
	 public static void main(String[] args) throws JobExecutionException {
/*	 FileSystemXmlApplicationContext context = new
	 FileSystemXmlApplicationContext(
	 "C:\\Users\\USER\\Desktop\\EMS source code\\EMSService1\\src\\main\\webapp\\WEB-INF\\spring\\appServlet/servlet-context.xml"
	 ); AbstractJob job = new CumulativeReportJob(); job.execute(null);
	 context.close(); System.exit(0);*/ }
	
}