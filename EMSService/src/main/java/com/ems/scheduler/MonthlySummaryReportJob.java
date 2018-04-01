package com.ems.scheduler;

import static com.ems.scheduler.FinalReportJob.FinalReportConstants.BODY_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.GA_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.KW;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.OFFICE;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.PAINT_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.PRESS_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.UTILITY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ems.config.listener.AppContextAware;
import org.ems.dao.DeviceDetailsDAO;
import org.ems.dao.PollingDetailsDAO;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.ems.UI.dto.AttachmentDTO;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.EmailDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.EmailConstants;
import com.ems.constants.QueryConstants;
import com.ems.constants.SettingsConstants;
import com.ems.mailer.EmailUtil;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;

public class MonthlySummaryReportJob extends AbstractJob {

	private static final Logger logger = LoggerFactory.getLogger(MonthlySummaryReportJob.class);
	private PollingDetailsDAO pollingDAO = AppContextAware.getContext().getBean(PollingDetailsDAO.class);
	private DeviceDetailsDAO deviceDetailsDAO = AppContextAware.getContext().getBean(DeviceDetailsDAO.class);

	private List<DeviceDetailsDTO> enabledDevices = null;

	private EmailDTO emailDTO = null;
	private List<AttachmentDTO> attachments = null;
	private static final String[] COLUMNS = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O",
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH" };

	public static final String[] consumptionDevices = new String[] { BODY_SHOP[1], PAINT_SHOP[1], GA_SHOP[1],
			UTILITY[1], OFFICE[1], PRESS_SHOP[1] };

	private static HashMap<String, Integer[]> rowIndexs = new HashMap<>();

	static {
		rowIndexs.put(BODY_SHOP[1], new Integer[] { 3, 3 });
		rowIndexs.put(PAINT_SHOP[1], new Integer[] { 4, 3 });
		rowIndexs.put(GA_SHOP[1], new Integer[] { 5, 3 });
		rowIndexs.put(UTILITY[1], new Integer[] { 6, 3 });
		rowIndexs.put(OFFICE[1], new Integer[] { 7, 3 });
		rowIndexs.put(PRESS_SHOP[1], new Integer[] { 8, 3 });
	}

	@Override
	protected void preProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing required parameters - Final Summary report...");
		this.emailDTO = ConfigHelper.getEmailDetails();
		this.attachments = new ArrayList<>();
		this.enabledDevices = pollingDAO.fetchAllDeviceDetails(QueryConstants.SELECT_ENABLED_ENDEVICES,
				new Object[] {});
		logger.debug("Initializing required parameters completed - Final Summary report...");
	}

	@Override
	protected void work(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing report preparation- Final Summary report...");

		this.emailDTO.setDate(
				EMSUtility.getFormattedTime(LocalDateTime.now().minusDays(1).toDate().getTime(), "dd-MMM-yyyy"));
		this.emailDTO.setBody(EmailConstants.FINAL_REPORT);

		String reportPath = deviceDetailsDAO.getSettings().get(SettingsConstants.reportPath);
		// LOad the report template
		File reportTemplate = new File(
				TempDataManager.getTempFolder().getAbsoluteFile() + File.separator + "MONTHLY_SUMMARY.xlsx");
		File report = new File(reportPath + File.separator + this.emailDTO.getDate() + "-MonthlySummary.xlsx");

		try (FileOutputStream stream = new FileOutputStream(report);
				FileInputStream inStream = new FileInputStream(reportTemplate);
				XSSFWorkbook book = (XSSFWorkbook) WorkbookFactory.create(inStream);) {

			XSSFSheet template = (XSSFSheet) book.getSheet("TEMPLATE");
			logger.debug("Final summary report created " + report.getAbsolutePath());

			// Code to calculate and set values in sheetr
			prepareSummary(template);

			book.write(stream);
		} catch (Exception e) {
			logger.error("{}", e);
		} finally {
			logger.trace("Final Report resources are relased");
		}

		AttachmentDTO attachment = new AttachmentDTO();
		attachment.setFileName("EMSMonthlyReport-" + this.emailDTO.getDate() + ".xlsx");
		this.attachments.add(attachment);
		this.attachments.get(0).setFile(report);
	}

	private void prepareSummary(XSSFSheet template) {

		logger.trace(" preparing summary begins");

		LocalDate today = LocalDate.now();

		long[] dateRange = Helper.dateRange(today);
		dateRange[1] += 1000000;// include next date first record to find difference

		// Set Report Month-Year value
		template.getRow(1).getCell(3).setCellValue(EMSUtility.getFormattedTime(dateRange[0], "MMM-yy"));

		logger.info("begining {} end {}", dateRange[0], dateRange[1]);

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

		for (String device : consumptionDevices) {
			try {
				List<PollingDetailDTO> consumptionList = dao.fetchMainIncomerDailySummary(
						QueryConstants.DAILYSUMMARY_REPORT,
						new Object[] { device, dateRange[0], dateRange[1], device, dateRange[0], dateRange[1] });

				if (!consumptionList.isEmpty()) {
					List<DeviceDetailsDTO> mainIncomer = this.enabledDevices.stream().filter(
							enabledDevice -> enabledDevice.getUniqueId() == consumptionList.get(0).getDeviceuniqueid())
							.collect(Collectors.toList());

					Properties mappings = EMSUtility.loadProperties(mainIncomer.get(0).getMemoryMapping());
					Properties valueAndAddress = EMSUtility.interChangeKeyValue(mappings);
					String kwAddress = valueAndAddress.getProperty(KW);

					Integer[] indexs = rowIndexs.get(device);

					int column = indexs[1].intValue();

					for (int i = 1; i < consumptionList.size(); i++) {
						Properties maxProperties = EMSUtility.loadProperties(consumptionList.get(i).getUnitresponse());
						Properties minProperties = EMSUtility
								.loadProperties(consumptionList.get(i - 1).getUnitresponse());

						BigDecimal totalKWConsumed = new BigDecimal(maxProperties.getProperty(kwAddress))
								.subtract(new BigDecimal(minProperties.getProperty(kwAddress)));

						template.getRow(indexs[0]).getCell(column)
								.setCellValue((long) totalKWConsumed.doubleValue() / 1000);

						if (column != 5)
							template.getRow(9).getCell(column)
									.setCellFormula(String.format("SUM(%s4:%s9)", COLUMNS[column], COLUMNS[column]));

						column += 1;
					}
				}

				logger.debug("size {}", consumptionList.size());

			} catch (Exception e) {
				logger.error("{}", e);
			}
		}

		logger.trace(" preparing summary ends");
	}

	@Override
	protected void postProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Trigger mail for Final summary report..");
		EmailUtil.setEmailDetails(this.emailDTO);

		this.emailDTO.setAttachments(this.attachments);
		boolean sent = EmailUtil.sendEmail(this.emailDTO);

		logger.debug("mail triggered  for Final summary report.. ");

	}

	public static void main(String[] args) throws JobExecutionException {

		FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(
				"D:/GitRepo/EMS_Repo/EMS/EMSService/src/main/webapp/WEB-INF/spring/appServlet/servlet-context.xml");
		AbstractJob job = new MonthlySummaryReportJob();
		job.execute(null);

		context.close();
	}

}