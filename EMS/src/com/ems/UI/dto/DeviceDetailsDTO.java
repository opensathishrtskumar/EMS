package com.ems.UI.dto;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

/**
 * @author Sathish Kumar
 *
 */
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
	//Set default mapping as "MSRF"
	private String registerMapping;
	private String method;
	private boolean splitJoin;
	private String timeStamp;
	
	private Properties reportMapping;
	private long startTime;
	private long endTime;
	private int recordCount;
	
	private boolean allDevice;
	private boolean allMemory;
	private Map<String, String> selectedMemory;
	private String singleParamAddress;
	private int coulmnIndex;
	
	public DeviceDetailsDTO() {
	}
	
	public int getCoulmnIndex() {
		return coulmnIndex;
	}

	public void setCoulmnIndex(int coulmnIndex) {
		this.coulmnIndex = coulmnIndex;
	}

	public String getSingleParamAddress() {
		return singleParamAddress;
	}

	public void setSingleParamAddress(String singleParamAddress) {
		this.singleParamAddress = singleParamAddress;
	}

	public boolean isAllDevice() {
		return allDevice;
	}

	public DeviceDetailsDTO setAllDevice(boolean allDevice) {
		this.allDevice = allDevice;
		return this;
	}

	public boolean isAllMemory() {
		return allMemory;
	}

	public DeviceDetailsDTO setAllMemory(boolean allMemory) {
		this.allMemory = allMemory;
		return this;
	}

	public Map<String, String> getSelectedMemory() {
		return selectedMemory;
	}

	public DeviceDetailsDTO setSelectedMemory(Map<String, String> selectedMemory) {
		this.selectedMemory = selectedMemory;
		return this;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public DeviceDetailsDTO setRecordCount(int recordCount) {
		this.recordCount = recordCount;
		return this;
	}



	public long getStartTime() {
		return startTime;
	}

	public DeviceDetailsDTO setStartTime(long startTime) {
		this.startTime = startTime;
		return this;
	}

	public long getEndTime() {
		return endTime;
	}

	public DeviceDetailsDTO setEndTime(long endTime) {
		this.endTime = endTime;
		return this;
	}

	public Properties getReportMapping() {
		return reportMapping;
	}

	public DeviceDetailsDTO setReportMapping(Properties reportMapping) {
		this.reportMapping = reportMapping;
		return this;
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
	
	public String getRegisterMapping() {
		return registerMapping;
	}

	public void setRegisterMapping(String registerMapping) {
		this.registerMapping = registerMapping;
	}
	
	public boolean isSplitJoin() {
		return splitJoin;
	}

	public void setSplitJoin(boolean splitJoin) {
		this.splitJoin = splitJoin;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getGroupKey(){
		StringBuilder builder = new StringBuilder();
		builder.append(getPort());
		builder.append(getBaudRate());
		builder.append(getWordLength());
		builder.append(getParity());
		builder.append(getStopbit());
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return deviceName;
	}
}
