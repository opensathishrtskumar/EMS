package org.ems.model;

public class DeviceForm {

	private long deviceId;
	private int unitId;
	private String deviceName;
	private int baudRates;
	private int wordLength;
	private String parity;
	private int stopBit;
	private String memoryMapping;
	private String enabled;
	private String regMapping;
	private String port;
	private String readMethod;

	public long getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public int getBaudRates() {
		return baudRates;
	}
	public void setBaudRates(int baudRates) {
		this.baudRates = baudRates;
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
	public int getStopBit() {
		return stopBit;
	}
	public void setStopBit(int stopBit) {
		this.stopBit = stopBit;
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
	public String getRegMapping() {
		return regMapping;
	}
	public void setRegMapping(String regMapping) {
		this.regMapping = regMapping;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getReadMethod() {
		return readMethod;
	}
	public void setReadMethod(String readMethod) {
		this.readMethod = readMethod;
	}
	public int getUnitId() {
		return unitId;
	}
	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}
	
}
