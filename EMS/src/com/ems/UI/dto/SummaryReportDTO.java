package com.ems.UI.dto;

import java.io.Serializable;

public class SummaryReportDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Double minimum;
	private String minimumTimeStamp;
	private Double maximum;
	private String maximumTimeStamp;
	
	private String memoryAddress;
	private long deviceUniqueId;
	private Double average;
	private String memoryAddressName;
	private long startTime;
	private long endTime;
	private int totalNumberOfObservation;
	
	private int minIndex;
	private int maxIndex;
	
	public int getMinIndex() {
		return minIndex;
	}
	public void setMinIndex(int minIndex) {
		this.minIndex = minIndex;
	}
	public int getMaxIndex() {
		return maxIndex;
	}
	public void setMaxIndex(int maxIndex) {
		this.maxIndex = maxIndex;
	}
	public Double getMinimum() {
		return minimum;
	}
	public void setMinimum(Double minimum) {
		this.minimum = minimum;
	}
	public String getMinimumTimeStamp() {
		return minimumTimeStamp;
	}
	public void setMinimumTimeStamp(String minimumTimeStamp) {
		this.minimumTimeStamp = minimumTimeStamp;
	}
	public Double getMaximum() {
		return maximum;
	}
	public void setMaximum(Double maximum) {
		this.maximum = maximum;
	}
	public String getMaximumTimeStamp() {
		return maximumTimeStamp;
	}
	public void setMaximumTimeStamp(String maximumTimeStamp) {
		this.maximumTimeStamp = maximumTimeStamp;
	}
	public String getMemoryAddress() {
		return memoryAddress;
	}
	public void setMemoryAddress(String memoryAddress) {
		this.memoryAddress = memoryAddress;
	}
	public long getDeviceUniqueId() {
		return deviceUniqueId;
	}
	public void setDeviceUniqueId(long deviceUniqueId) {
		this.deviceUniqueId = deviceUniqueId;
	}
	public Double getAverage() {
		return average;
	}
	public void setAverage(Double average) {
		this.average = average;
	}
	public String getMemoryAddressName() {
		return memoryAddressName;
	}
	public void setMemoryAddressName(String memoryAddressName) {
		this.memoryAddressName = memoryAddressName;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public int getTotalNumberOfObservation() {
		return totalNumberOfObservation;
	}
	public void setTotalNumberOfObservation(int totalNumberOfObservation) {
		this.totalNumberOfObservation = totalNumberOfObservation;
	}
	@Override
	public String toString() {
		return "SummaryReportDTO [minimum=" + minimum + ", minimumTimeStamp=" + minimumTimeStamp + ", maximum="
				+ maximum + ", maximumTimeStamp=" + maximumTimeStamp + ", memoryAddress=" + memoryAddress + ", average="
				+ average + ", memoryAddressName=" + memoryAddressName + ", totalNumberOfObservation="
				+ totalNumberOfObservation + "]";
	}
}
