package org.ems.reports.summary;

import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFSheet;

import com.ems.UI.dto.DeviceDetailsDTO;

/**
 * @author Sathish
 *
 * Abstract device summary finder for final report
 */
public abstract class AbstractDeviceSummary {
	
	protected List<DeviceDetailsDTO> enabledDevices = null;
	
	public abstract void initialize(List<DeviceDetailsDTO> enabledDevices) throws Exception;
	
	public abstract void findSummary(XSSFSheet template) throws Exception;
	
	public abstract void completed() throws Exception;

	public List<DeviceDetailsDTO> getEnabledDevices() {
		return enabledDevices;
	}

	public void setEnabledDevices(List<DeviceDetailsDTO> enabledDevices) {
		this.enabledDevices = enabledDevices;
	}
}
