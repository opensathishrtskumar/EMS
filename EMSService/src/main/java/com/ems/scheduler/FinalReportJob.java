package com.ems.scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ems.config.listener.AppContextAware;
import org.ems.dao.DeviceDetailsDAO;
import org.ems.dao.PollingDetailsDAO;
import org.ems.reports.summary.AbstractDeviceSummary;
import org.ems.reports.summary.ConfigSummary;
import org.ems.reports.summary.DGSummary;
import org.ems.reports.summary.MainIncomerSummary;
import org.ems.reports.summary.MonthlySummary;
import org.joda.time.LocalDateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.AttachmentDTO;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.EmailDTO;
import com.ems.constants.EmailConstants;
import com.ems.constants.QueryConstants;
import com.ems.constants.SettingsConstants;
import com.ems.mailer.EmailUtil;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;

public class FinalReportJob extends AbstractJob {

	private static final Logger logger = LoggerFactory.getLogger(FinalReportJob.class);
	private PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);
	private List<DeviceDetailsDTO> enabledDevices = null;

	private EmailDTO emailDTO = null;
	private List<AttachmentDTO> attachments = null;

	public static Map<Integer, Integer[]> excelCoordinates = null;

	static {
		excelCoordinates = new HashMap<>();
		excelCoordinates.put(1, new Integer[] { 2, 0 });// Total Consumption Per Day KWH - Main Incommer Meter WH (
														// Morning 12.00 from 11.59 night)
		excelCoordinates.put(2, new Integer[] { 2, 2 });// Total Consumption Per Day KVAH - Main Incommer Meter VAH (
														// Morning 12.00 from 11.59 night)
		// Monthly main incomer
		excelCoordinates.put(24, new Integer[] { 2, 4 });// Total Consumption Per Day KWH - Main Incommer Meter WH (
															// Morning 12.00 from 11.59 night)
		excelCoordinates.put(25, new Integer[] { 2, 6 });// Total Consumption Per Day KVAH - Main Incommer Meter VAH (
															// Morning 12.00 from 11.59 night)

		excelCoordinates.put(6, new Integer[] { 2, 8 });// Abnormality Voltage observed - Mention range pls
		excelCoordinates.put(5, new Integer[] { 2, 10 });// Abnormality KVA observed - Mention range pls
		excelCoordinates.put(4, new Integer[] { 2, 12 });// Abnormality PF observed - below 0.95

		excelCoordinates.put(3, new Integer[] { 2, 17 });// DG - KWH - DG Meter WH( Morning 12.00 from 11.59 night) -
															// Total consumption
		excelCoordinates.put(26, new Integer[] { 2, 15 });// Power Failure coordinates

		excelCoordinates.put(7, new Integer[] { 6, 5 });
		excelCoordinates.put(8, new Integer[] { 6, 6 });
		excelCoordinates.put(9, new Integer[] { 6, 7 });
		excelCoordinates.put(10, new Integer[] { 6, 8 });
		excelCoordinates.put(11, new Integer[] { 6, 9 });
		excelCoordinates.put(12, new Integer[] { 6, 10 });

		excelCoordinates.put(13, new Integer[] { 11, 5 });
		excelCoordinates.put(14, new Integer[] { 11, 6 });
		excelCoordinates.put(15, new Integer[] { 11, 7 });
		excelCoordinates.put(16, new Integer[] { 11, 8 });
		excelCoordinates.put(17, new Integer[] { 11, 9 });
		excelCoordinates.put(18, new Integer[] { 11, 10 });

		excelCoordinates.put(19, new Integer[] { 17, 5 });
		excelCoordinates.put(20, new Integer[] { 17, 6 });
		excelCoordinates.put(21, new Integer[] { 17, 7 });
		excelCoordinates.put(22, new Integer[] { 17, 8 });
		excelCoordinates.put(23, new Integer[] { 17, 9 });

	}

	public FinalReportJob() {
		logger.debug("Final Summary report Job created");
		enabledDevices = dao.fetchAllDeviceDetails(QueryConstants.SELECT_ENABLED_ENDEVICES, new Object[] {});
		logger.trace("Loaded all enabled devices");
	}

	@Override
	protected void preProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing required parameters - Final Summary report...");
		this.emailDTO = ConfigHelper.getEmailDetails();
		this.attachments = new ArrayList<>();
		logger.debug("Initializing required parameters completed - Final Summary report...");
	}

	@Override
	protected void work(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing report preparation- Final Summary report...");

		this.emailDTO.setDate(
				EMSUtility.getFormattedTime(LocalDateTime.now().minusDays(1).toDate().getTime(), "dd-MMM-yyyy"));
		this.emailDTO.setBody(EmailConstants.FINAL_REPORT);

		DeviceDetailsDAO dao = AppContextAware.getContext().getBean(DeviceDetailsDAO.class);
		String reportPath = dao.getSettings().get(SettingsConstants.reportPath);
		File report = new File(reportPath + File.separator + this.emailDTO.getDate() + "-FinalReport.xlsx");

		File file = new File(TempDataManager.getTempFolder().getAbsoluteFile() + File.separator + "SummaryReport.xlsx");

		try (FileOutputStream stream = new FileOutputStream(report);
				FileInputStream inStream = new FileInputStream(file);
				XSSFWorkbook book = (XSSFWorkbook) WorkbookFactory.create(inStream);) {

			XSSFSheet template = (XSSFSheet) book.getSheet("Report");
			logger.debug("Final summary report created " + report.getAbsolutePath());

			// Code to calculate and set values in sheet
			prepareSummary(template);
			book.write(stream);
		} catch (Exception e) {
			logger.error("{}", e);
		} finally {
			logger.trace("Final Report resources are relased");
		}

		// Set attachment details for mail
		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFileName("EMSSummaryReport-" + this.emailDTO.getDate() + ".xlsx");
		this.attachments.add(attachment);
		this.attachments.get(0).setFile(report);
		logger.debug("Initializing report preparation completed- Daily Summary report...");

	}

	private void prepareSummary(XSSFSheet template) {
		logger.trace(" preparing summary begins");
		try {
			List<AbstractDeviceSummary> summaryPreparators = new ArrayList<>();
			// Summary of main incomer
			summaryPreparators.add(new MainIncomerSummary());
			// Summary of Generator
			summaryPreparators.add(new DGSummary());
			// Daily Summary
			summaryPreparators.add(new ConfigSummary());
			// Mnthly Summary
			summaryPreparators.add(new MonthlySummary());

			// Exection of individual summary finders
			for (AbstractDeviceSummary summary : summaryPreparators) {
				try {
					summary.initialize(enabledDevices);
					summary.findSummary(template);
					summary.completed();
				} catch (Exception e) {
					logger.error("{}", e);
				}
			}

			// Daily Total
			template.getRow(6).getCell(11).setCellFormula("SUM(F7:K7)");
			template.getRow(7).getCell(11).setCellFormula("SUM(F8:K8)");

			template.getRow(11).getCell(11).setCellFormula("SUM(F12:K12)");
			template.getRow(12).getCell(11).setCellFormula("SUM(F13:K13)");

		} catch (Exception e) {
			logger.error("{}", e);
		}
		logger.trace(" preparing summary ends");
	}

	@Override
	protected void postProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Trigger mail for Final summary report..");
		// Set company name date and so on
		EmailUtil.setEmailDetails(this.emailDTO);
		// Trigger here mail

		this.emailDTO.setAttachments(this.attachments);
		boolean sent = EmailUtil.sendEmail(this.emailDTO);

		logger.debug("mail triggered  for Final summary report.. ");

	}

	public static void main(String[] args) throws JobExecutionException {

		/*
		 * FileSystemXmlApplicationContext context = new
		 * FileSystemXmlApplicationContext(
		 * "C:\\Users\\USER\\Desktop\\EMS source code\\EMSService1\\src\\main\\webapp\\WEB-INF\\spring\\appServlet/servlet-context.xml"
		 * ); AbstractJob job = new FinalReportJob(); job.execute(null);
		 * 
		 * context.close();
		 */ }

	public static class FinalReportConstants {

		private FinalReportConstants() {
		}

		public static final String MAIN_INCOMER = "main_incomer";
		public static final String DG_INCOMER = "dg_incomer";
		public static final String[] BODY_SHOP = new String[] { "", "body_shop1", "body_shop2" };
		public static final String[] PAINT_SHOP = new String[] { "", "paint_shop1", "paint_shop2" };
		public static final String[] GA_SHOP = new String[] { "", "ga_shop1", "ga_shop2" };
		public static final String[] UTILITY = new String[] { "", "utility1", "utility2" };
		public static final String[] OFFICE = new String[] { "", "office1", "office2" };
		public static final String[] PRESS_SHOP = new String[] { "", "press_shop1" };

		public static final String KW = "WH";
		public static final String VAH = "VAH";
		public static final String PF = "PF";
		public static final String VOLTAGE_RY = "Voltage RY";
		public static final String VOLTAGE_YB = "Voltage YB";
		public static final String VOLTAGE_BR = "Voltage BR";

		public static final String VA1 = "VA1";
		public static final String VA2 = "VA2";
		public static final String VA3 = "VA3";

	}
}