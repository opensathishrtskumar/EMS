package com.ems.UI.dto;

import java.io.Serializable;

public class DeviceDetailsDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long uniqueId;
	private int deviceId;
	private String deviceName;
	private int baudRate;
	private int wordLength;
	private String parity;
	private int stopbit;
	private String memoryMapping;
	private String enabled;
	
	private String port;
	private int pollDelay;
	private int rowIndex;
	
	public DeviceDetailsDTO() {
	}

	public long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getWordLength() {
		return wordLength;
	}

	public void setWordLength(int wordLength) {
		this.wordLength = wordLength;
	}

	public String getParity() {
		return parity;
	}

	public void setParity(String parity) {
		this.parity = parity;
	}

	public int getStopbit() {
		return stopbit;
	}

	public void setStopbit(int stopbit) {
		this.stopbit = stopbit;
	}

	public String getMemoryMapping() {
		return memoryMapping;
	}

	public void setMemoryMapping(String memoryMapping) {
		this.memoryMapping = memoryMapping;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public int getPollDelay() {
		return pollDelay;
	}

	public void setPollDelay(int pollDelay) {
		this.pollDelay = pollDelay;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public String getGroupKey(){
		StringBuilder builder = new StringBuilder();
		builder.append(getBaudRate());
		builder.append(getWordLength());
		builder.append(getParity());
		builder.append(getStopbit());
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return deviceName + "(" + uniqueId + ")";
	}
}
