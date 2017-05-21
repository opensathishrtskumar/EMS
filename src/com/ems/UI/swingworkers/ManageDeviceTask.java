package com.ems.UI.swingworkers;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.db.DBConnectionManager;

public class ManageDeviceTask implements Callable<Object> {
	private static final Logger logger = LoggerFactory.getLogger(ManageDeviceTask.class);
	private DeviceDetailsDTO device;
	
	public ManageDeviceTask(DeviceDetailsDTO device) {
		this.device = device;
	}
	
	public DeviceDetailsDTO getDevice() {
		return device;
	}
	
	@Override
	public Object call() throws Exception {
		
		boolean status = false;
		
		try {
			if(device.getUniqueId() == 0){
				logger.info("inserting new device");
				DBConnectionManager.insertDevice(device);
				logger.info("device inserted : {}", device);
			}else{
				logger.info("updating device : {}", device);
				DBConnectionManager.updateDevice(device);
				logger.info("device updated successfully ");
			}
			status = true;
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Device Manager task error : {}", e.getLocalizedMessage());
		}
		
		return status;
	}
}
