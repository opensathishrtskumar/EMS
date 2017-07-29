package com.ems.constants;

public abstract class EmailConstants {

	public static final String DAILY_REPORT = "<html><head></head><body><h1 style='color: #5e9ca0; text-align: center;'>Welcome "
			+ EmailConstants.COMPANY_NAME_PARAM
			+ "</h1><h2 style='color: #2e6c80;'>Please find attached report for all active device(s) dated on "
			+ EmailConstants.REPORT_DATE
			+ "</h2><p>&nbsp;</p><p>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</p><h2 style='color: #2e6c80;'><br /><br /></h2><p>&nbsp;</p></body></html>";
	
	public static final String CUMULATIVE_REPORT = "<html><head></head><body><h1 style='color: #5e9ca0; text-align: enter;'>Welcome "
			+ EmailConstants.COMPANY_NAME_PARAM
			+ "</h1><h2 style='color: #2e6c80;'>Please find attached Summary report for all active device(s) dated &nbsp;"
			+ EmailConstants.REPORT_DATE
			+ "</h2><p>&nbsp;</p><p>&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</p><h2 style='color: #2e6c80;'><br /><br /></h2><p>&nbsp;</p></body></html>";

	public static final String COMPANY_NAME_PARAM = "$companyname$";
	public static final String REPORT_DATE = "$date$";
	public static final String REPORT_START_DATE = "$startdate$";
	public static final String REPORT_END_DATE = "$enddate$";

	public static final String DEFAULT_FROM_EMAIL = "ems.ses03@gmail.com";
	public static final String DEFAULT_EMAIL_PASSWORD = "kavi071215";
}
