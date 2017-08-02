package com.ems.UI.dto;

import java.util.Properties;

public class PollingDetailDTO {
	private long deviceuniqueid;
	private long polledon;
	private String polledTime;
	private String unitresponse;
	private String formattedDate;
	private long formattedHour;
	private Properties deviceReading;
	private boolean status;
	
	public PollingDetailDTO(long deviceuniqueid, long polledon, String unitresponse) {
		this.deviceuniqueid = deviceuniqueid;
		this.polledon = polledon;
		this.unitresponse = unitresponse;
	}
	
	public PollingDetailDTO() {
	}

	public long getDeviceuniqueid() {
		return deviceuniqueid;
	}

	public void setDeviceuniqueid(long deviceuniqueid) {
		this.deviceuniqueid = deviceuniqueid;
	}

	public long getPolledon() {
		return polledon;
	}

	public void setPolledon(long polledon) {
		this.polledon = polledon;
	}

	public String getUnitresponse() {
		return unitresponse;
	}

	public void setUnitresponse(String unitresponse) {
		this.unitresponse = unitresponse;
	}

	public String getPolledTime() {
		return polledTime;
	}

	public void setPolledTime(String polledTime) {
		this.polledTime = polledTime;
	}

	public String getFormattedDate() {
		return formattedDate;
	}

	public void setFormattedDate(String formattedDate) {
		this.formattedDate = formattedDate;
	}

	public long getFormattedHour() {
		return formattedHour;
	}

	public void setFormattedHour(long formattedHour) {
		this.formattedHour = formattedHour;
	}

	public Properties getDeviceReading() {
		return deviceReading;
	}

	public void setDeviceReading(Properties deviceReading) {
		this.deviceReading = deviceReading;
	}
	
	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "PollingDetailDTO [deviceuniqueid=" + deviceuniqueid + ", polledon=" + polledon + ", polledTime="
				+ polledTime + ", unitresponse=" + unitresponse + ", formattedDate=" + formattedDate
				+ ", formattedHour=" + formattedHour + ", deviceReading=" + deviceReading + "]";
	}
}
