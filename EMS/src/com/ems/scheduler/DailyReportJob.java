package com.ems.scheduler;

import static com.ems.util.ExcelUtils.createReportHeaderMap;
import static com.ems.util.ExcelUtils.createWorkBook;
import static com.ems.util.ExcelUtils.createWorkSheet;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.ems.constants.EmailConstants;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.mailer.EmailUtil;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;
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
		this.emailDTO.setBody(EmailConstants.DAILY_REPORT);
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
				new SheetWriter(param, sheet, QueryConstants.RETRIEVE_DEVICE_STATE,
						new Object[] { param.getUniqueId(), startTime, endTime }).call();
			} catch (Exception e) {
				logger.error("{}",e);
			}
		}
		
		try {
			String reportDir = ConfigHelper.getDailyReportDir();
			this.tempReportFile = new File(reportDir + File.separator +  this.emailDTO.getDate() + "-EMSReport.xls");
			workBook.write(this.tempReportFile);
			workBook.close();
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
		//Set company name date and so on
		EmailUtil.setEmailDetails(this.emailDTO);
		this.emailDTO.setAttachments(this.attachments);
		boolean sent = EmailUtil.sendEmail(this.emailDTO);
		//No need to delete report file
		logger.debug("mail triggered  for daily report..");
	}
	
	public static void main(String[] args) throws JobExecutionException {
		AbstractJob job = new DailyReportJob();
		job.execute(null);
	}
	
}