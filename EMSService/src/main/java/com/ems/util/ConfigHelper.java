package com.ems.util;

import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;
import static com.ems.constants.LimitConstants.DEFAULT_COMPANY_NAME;
import static com.ems.constants.LimitConstants.DEFAULT_NUMBER_OF_DEVICES;
import static com.ems.constants.MessageConstants.COMPANYNAME_KEY;
import static com.ems.constants.MessageConstants.CUMULATIVE_REPORT_CRON_KEY;
import static com.ems.constants.MessageConstants.DAILY_REPORT_CRON_KEY;
import static com.ems.constants.MessageConstants.DAILY_REPORT_CUMULATIVE_KEY;
import static com.ems.constants.MessageConstants.DAILY_REPORT_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_REFRESHFREQUENCY_KEY;
import static com.ems.constants.MessageConstants.DEVICES_GROUPING_KEY;
import static com.ems.constants.MessageConstants.LIVECHART_FREQUENCY_KEY;
import static com.ems.constants.MessageConstants.NUMBER_OF_DEVICES_KEY;
import static com.ems.constants.SettingsConstants.bccEmail;
import static com.ems.constants.SettingsConstants.ccEmail;
import static com.ems.constants.SettingsConstants.companyName;
import static com.ems.constants.SettingsConstants.fromEmail;
import static com.ems.constants.SettingsConstants.mailPassword;
import static com.ems.constants.SettingsConstants.port;
import static com.ems.constants.SettingsConstants.smtpHost;
import static com.ems.constants.SettingsConstants.toEmail;
import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.tmp.datamngr.TempDataManager.retrieveTempConfig;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.ems.config.listener.AppContextAware;
import org.ems.db.repo.DeviceDetailsDAO;

import com.ems.UI.dto.EmailDTO;
import com.ems.constants.LimitConstants;
import com.ems.scheduler.SchedulerConstants;
import com.ems.tmp.datamngr.TempDataManager;

/**
 * @author sathish
 *
 */
public abstract class ConfigHelper {

	public static Properties retrieveMainConfig() {
		return retrieveTempConfig(MAIN_CONFIG);
	}

	public static int getDefaultDevices() {
		Properties config = retrieveMainConfig();
		String count = config.getProperty(NUMBER_OF_DEVICES_KEY, String.valueOf(DEFAULT_NUMBER_OF_DEVICES));
		return Integer.parseInt(count);
	}

	public static String getCompanyName() {
		Properties config = retrieveMainConfig();
		return config.getProperty(COMPANYNAME_KEY, DEFAULT_COMPANY_NAME);
	}

	public static String getGroupingDetails() {
		return getPropertyWithPut(DEVICES_GROUPING_KEY, LimitConstants.DEFAULT_GROUP_VALUE); 
	}

	public static Properties setGroupingDetails(String value) {
		Properties config = retrieveMainConfig();
		config.setProperty(DEVICES_GROUPING_KEY, value);
		return config;
	}

	public static String getDashboardFrequency() {
		Properties config = retrieveMainConfig();
		return config.getProperty(DASHBOARD_REFRESHFREQUENCY_KEY, String.valueOf(DASHBOARD_REFRESH_FREQUENCY));
	}

	public static EmailDTO getEmailDetails() {
		EmailDTO dto = new EmailDTO();
		DeviceDetailsDAO dao = AppContextAware.getContext().getBean(DeviceDetailsDAO.class);
		Map<String, String> settings = dao.getSettings();
		dto.setBccEmail(settings.get(bccEmail));
		dto.setCcEmail(settings.get(ccEmail));
		dto.setFromEmail(settings.get(fromEmail));
		dto.setMailPassword(settings.get(mailPassword));
		dto.setPort(settings.get(port));
		dto.setSmtpHost(settings.get(smtpHost));
		dto.setToEmail(settings.get(toEmail));
		dto.setCompanyName(settings.get(companyName));
		dto.setSubject("EMS Report");
		
		return dto;
	}

	public static String getDailyReportCronExpr() {
		return getPropertyWithPut(DAILY_REPORT_CRON_KEY, SchedulerConstants.DAILY_REPORT_CRON);
	}

	public static String getcumulativeReportCronExpr() {
		return getPropertyWithPut(CUMULATIVE_REPORT_CRON_KEY, SchedulerConstants.CUMULATIVE_REPORT_CRON);
	}

	//Scheduler daily report path
	public static String getDailyReportDir() {
		return getPropertyWithPut(DAILY_REPORT_KEY, getDailyReportBaseDir());
	}

	//Scheduler daily summary report path
	public static String getDailyCumulativeReportDir() {
		return getPropertyWithPut(DAILY_REPORT_CUMULATIVE_KEY, getCumulativeDailyReportBaseDir());
	}

	private static String getReportBaseDir(){
		String baseReportPath = System.getProperty("user.home") + File.separator + "Reports";
		return baseReportPath;
	}

	private static String getDailyReportBaseDir(){
		String dailyReportPath = getReportBaseDir() + File.separator + "DailyReport";
		File dir = new File(dailyReportPath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		return dailyReportPath;
	}

	private static String getCumulativeDailyReportBaseDir(){
		String reportPath = getReportBaseDir() + File.separator + "DailySummaryReport";
		File dir = new File(reportPath);
		if(!dir.exists()){
			dir.mkdirs();
		}
		return reportPath;
	}


	//LiveChart refresh frequency
	public static int getLiveRefreshFrequency() {
		String value = String.valueOf(SchedulerConstants.LIVECHAR_REFRESH_FREQUENCY);
		value = getPropertyWithPut(LIVECHART_FREQUENCY_KEY, value);
		return Integer.parseInt(value);
	}

	/**
	 * @param key
	 * @param defaultValue
	 * @return
	 * 
	 * Returns value from Config file and puts if doesn't exist
	 */
	public static String getPropertyWithPut(String key, String defaultValue) {
		Properties config = retrieveMainConfig();
		String value = config.getProperty(key);

		// Put default values if doesn't exist
		if (value == null) {
			value = defaultValue;
			config.put(key, value);
			TempDataManager.writeTempConfig(config, TempDataManager.MAIN_CONFIG);
		}
		return value;
	}
}
