
package org.ems.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class DateRangeReportForm {
	
	private long deviceUniqueId;
	
	private String deviceName;

	private boolean allDevices;

	private String memoryMappingDetails;

	private boolean allMappings;
	
	@NotNull
	@NotEmpty
	private String reportStartTime;
	
	@NotNull
	@NotEmpty
	private String reportEndTime;

	public long getDeviceUniqueId() {
		return deviceUniqueId;
	}

	public void setDeviceUniqueId(long deviceUniqueId) {
		this.deviceUniqueId = deviceUniqueId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public boolean isAllDevices() {
		return allDevices;
	}

	public void setAllDevices(boolean allDevices) {
		this.allDevices = allDevices;
	}

	public String getMemoryMappingDetails() {
		return memoryMappingDetails;
	}

	public void setMemoryMappingDetails(String memoryMappingDetails) {
		this.memoryMappingDetails = memoryMappingDetails;
	}

	public boolean isAllMappings() {
		return allMappings;
	}

	public void setAllMappings(boolean allMappings) {
		this.allMappings = allMappings;
	}

	public String getReportStartTime() {
		return reportStartTime;
	}

	public void setReportStartTime(String reportStartTime) {
		this.reportStartTime = reportStartTime;
	}

	public String getReportEndTime() {
		return reportEndTime;
	}

	public void setReportEndTime(String reportEndTime) {
		this.reportEndTime = reportEndTime;
	}
}