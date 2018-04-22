package com.ems.constants;

import static com.ems.constants.DBConstants.HOST;
import static com.ems.constants.DBConstants.PASSWORD;
import static com.ems.constants.DBConstants.PORT;
import static com.ems.constants.DBConstants.USERNAME;

public abstract class MessageConstants {

	public static final String NOT_IMPLEMNENTED = "The Required function is not implementd";
	public static final String[] DBCONFIG_KEY = { HOST, PORT, USERNAME, PASSWORD };
	public static final String REPORT_KEY_SEPARATOR = "=";
	public static final String REPORT_RECORD_SEPARATOR = System.lineSeparator();
	public static final String SEMICOLON = ";";

	// Main config keys
	public static final String USERNAME_KEY = "username";
	public static final String PASSWORD_KEY = "security";
	public static final String DASHBOARD_DEVICESCOUNT_KEY = "dashboardevicecount";
	public static final String DASHBOARD_REFRESHFREQUENCY_KEY = "dashboardrefreshfrequency";
	public static final String DASHBOARD_DEVICES_KEY = "dashboardevices";
	public static final String COMPANYNAME_KEY = "companyname";
	public static final String DEFAULTPORT_KEY = "defaultcomport";

	public static final String NUMBER_OF_DEVICES_KEY = "numberofdevices";

	// Device grouping
	public static final String DEVICES_GROUPING_KEY = "deViceGroupIngKeY";

	public static final String EMAIL_DETAILS_KEY = "EmAilDetAilsKEy";

	// For Schedulers
	public static final String DAILY_REPORT_CRON_KEY = "dailyreportcronkey";
	public static final String CUMULATIVE_REPORT_CRON_KEY = "cumulativereportcronkey";

	// Report path
	public static final String DAILY_REPORT_KEY = "dailyreportkey";
	public static final String DAILY_REPORT_CUMULATIVE_KEY = "dailyreportcumulativekey";

	public static final String LIVECHART_FREQUENCY_KEY = "livechartfrequencykey";

	public static final String TIMESTAMP = "Timestamp";
}
