
package org.ems.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class DateRangeReportForm {

	private String deviceName;

	private String memoryMappingDetails;

	@NotNull
	@NotEmpty
	private String reportStartTime;

	@NotNull
	@NotEmpty
	private String reportEndTime;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getMemoryMappingDetails() {
		return memoryMappingDetails;
	}

	public void setMemoryMappingDetails(String memoryMappingDetails) {
		this.memoryMappingDetails = memoryMappingDetails;
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