package com.ems.util;

import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;
import static com.ems.constants.LimitConstants.DEFAULT_COMPANY_NAME;
import static com.ems.constants.LimitConstants.DEFAULT_NUMBER_OF_DEVICES;
import static com.ems.constants.MessageConstants.COMPANYNAME_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_REFRESHFREQUENCY_KEY;
import static com.ems.constants.MessageConstants.DEVICES_GROUPING_KEY;
import static com.ems.constants.MessageConstants.NUMBER_OF_DEVICES_KEY;
import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.tmp.datamngr.TempDataManager.retrieveTempConfig;

import java.util.Properties;

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
		return config.getProperty(COMPANYNAME_KEY,DEFAULT_COMPANY_NAME);
	}
	
	public static String getGroupingDetails(){
		Properties config = retrieveMainConfig();
		return config.getProperty(DEVICES_GROUPING_KEY, "{'groups':[]}");
	}
	
	public static Properties setGroupingDetails(String value){
		Properties config = retrieveMainConfig();
		config.setProperty(DEVICES_GROUPING_KEY, value);
		return config;
	}
	
	public static String getDashboardFrequency(){
		Properties config = retrieveMainConfig();
		return config.getProperty(DASHBOARD_REFRESHFREQUENCY_KEY, String.valueOf(DASHBOARD_REFRESH_FREQUENCY));
	}
	
}
