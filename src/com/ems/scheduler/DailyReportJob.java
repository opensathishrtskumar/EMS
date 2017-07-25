package com.ems.scheduler;

import static com.ems.constants.QueryConstants.RETRIEVE_DEVICE_STATE;
import static com.ems.util.EMSUtility.getOrderedProperties;
import static com.ems.util.ExcelUtils.createReportHeaderMap;
import static com.ems.util.ExcelUtils.createWorkBook;
import static com.ems.util.ExcelUtils.createWorkSheet;
import static com.ems.util.ExcelUtils.writeResultToSheet;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.AttachmentDTO;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.EmailDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.mailer.EmailUtil;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;
import com.ems.util.ExcelUtils;
import com.ems.util.Helper;

public class DailyReportJob extends AbstractJob {
	private static final Logger logger = LoggerFactory.getLogger(DailyReportJob.class);

	private EmailDTO emailDTO = null;
	private List<DeviceDetailsDTO> devices = null;
	private List<AttachmentDTO> attachments = null;
	private File tempReportFile = null;
	
	public DailyReportJob() {

	}

	@Override
	protected void preProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing required parameters...");
		// Initialize here required values
		this.emailDTO = ConfigHelper.getEmailDetails();
		this.devices = DBConnectionManager.getAvailableDevices(QueryConstants.SELECT_ENABLED_ENDEVICES);
		logger.debug("Initializing required parameters completed...");
	}

	@Override
	protected void work(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing report preparation...");
		// Fetch report to send
		this.attachments = new ArrayList<>();
		/*
		 * Scheduler will be triggered next day only to send mail in office
		 * hours, ie 10AM . so get one day prior data to send
		 */

		LocalDateTime reportDate = LocalDateTime.now().minusDays(1);
		long yesterday = reportDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		
		this.emailDTO.setDate(reportDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
		
		//Set company name date and so on
		EmailUtil.setEmailDetails(this.emailDTO);

		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFileName("EMSReport-" + this.emailDTO.getDate() + ".xls");
		
		HSSFWorkbook workBook = createWorkBook();
		long startTime = Helper.getStartOfDay(yesterday);
		long endTime = Helper.getEndOfDay(yesterday);
		
		for(DeviceDetailsDTO device : this.devices){
			ExtendedSerialParameter param = EMSUtility.mapDeviceToSerialParam(device);
			Map<String, String> headers = createReportHeaderMap(param);
			HSSFSheet sheet = createWorkSheet(workBook, device.getDeviceName(), headers);
			
			try {
				new SheetWriter(param, sheet).setReportStartTime(startTime).setReportEndTime(endTime).call();
			} catch (Exception e) {
				logger.error("{}",e);
			}
		}
		
		try {
			this.tempReportFile = File.createTempFile(this.emailDTO.getDate() + "EMS_Report", ".xls");
			workBook.write(this.tempReportFile);
			attachment.setFile(this.tempReportFile);
		} catch (Exception e) {
			logger.error("{}",e);
		}
		
		this.attachments.add(attachment);
		
		logger.debug("Initializing report preparation completed...");
	}

	@Override
	protected void postProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Trigger mail daily report..");
		this.emailDTO.setAttachments(this.attachments);
		boolean sent = EmailUtil.sendEmail(this.emailDTO);
		//Delete if mail sent
		if(tempReportFile != null && sent)
			tempReportFile.delete();
		logger.debug("mail triggered  for daily report..");
	}
	
	static class SheetWriter implements Callable<Object>{
		
		private static final Logger logger = LoggerFactory.getLogger(SheetWriter.class);
		
		private long reportStartTime;
		private long reportEndTime;
		private ExtendedSerialParameter device;
		private HSSFSheet sheet;
		
		public SheetWriter(ExtendedSerialParameter device, HSSFSheet sheet) {
			this.device = device;
			this.sheet = sheet;
		}
		
		@Override
		public HSSFSheet call() throws Exception {
			
			logger.trace("trying to get memory details for daily report");
			
			Connection connection = DBConnectionManager.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			try {
				ps = connection.prepareStatement(RETRIEVE_DEVICE_STATE);
				ps.setLong(1, this.device.getUniqueId());
				ps.setLong(2, reportStartTime);
				ps.setLong(3, reportEndTime);
				rs = ps.executeQuery();

				// Keep the order of properties
				Map<String, String> memoryMap = getOrderedProperties(device);
				// All the values becomes header of column
				Map<String, String> headers = new LinkedHashMap<>();
				headers.put("Polled on", "Time");
				headers.putAll(memoryMap);

				// Firt row reserved for headers so start with 1
				for (int rowIndex = 1;rs.next(); rowIndex++) {
					HSSFRow row = sheet.createRow(rowIndex);
					ExcelUtils.writeReadingsRow(row, rs.getString("formatteddate"), 
							rs.getString("unitresponse"), memoryMap);
				}

				this.sheet = writeResultToSheet(device, rs, sheet);

			} catch (Exception e) {
				logger.error("error creating sheet for daily report  for device {}  {}", this.device, e);
			} finally {
				DBConnectionManager.closeConnections(connection, ps, rs);
			}
			
			logger.trace("sheet creation completed for daily report");
			
			return this.sheet;
		}

		public long getReportStartTime() {
			return reportStartTime;
		}

		public SheetWriter setReportStartTime(long reportStartTime) {
			this.reportStartTime = reportStartTime;
			return this;
		}

		public long getReportEndTime() {
			return reportEndTime;
		}

		public SheetWriter setReportEndTime(long reportEndTime) {
			this.reportEndTime = reportEndTime;
			return this;
		}
	}
	
	public static void main(String[] args) throws JobExecutionException {
		AbstractJob job = new DailyReportJob();
		job.execute(null);
	}
	
}