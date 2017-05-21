package com.ems.util;

import java.util.Properties;

import static com.ems.constants.LimitConstants.DEFAULT_NUMBER_OF_DEVICES;
import static com.ems.constants.MessageConstants.NUMBER_OF_DEVICES_KEY;
import static com.ems.tmp.datamngr.TempDataManager.*;
public abstract class ConfigHelper {
	
	public static Properties retrieveMainConfig(){
		return retrieveTempConfig(MAIN_CONFIG);
	}
	
	public static int getDefaultDevices(){
		Properties config = retrieveDBConfig();
		String count = config.getProperty(NUMBER_OF_DEVICES_KEY,
				String.valueOf(DEFAULT_NUMBER_OF_DEVICES));
		return Integer.parseInt(count);
	}
	
	/*public static void main(String[] args) {
		System.out.println(getDefaultDevices());
	}*/
}
