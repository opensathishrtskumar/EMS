package com.ems.UI.dto;

import static com.ems.util.EMSUtility.getPersistRegisters;
import static com.ems.util.EMSUtility.getRegisterCount;
import static com.ems.util.EMSUtility.getRegisterReference;

import java.util.Map;
import java.util.Properties;

import javax.swing.JScrollPane;

import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.SerialParameters;

public class ExtendedSerialParameter  extends SerialParameters{

	private int unitId;
	private int retries;
	private int timeout;
	private int reference;
	private int count;
	private int pointType;
	private int pollDelay;
	private long uniqueId;
	private int rowIndex;
	private Map<Long,String> memoryMappings;
	private int[] requiredRegisters;
	private String deviceName;
	private Register[] registeres;
	private boolean status;
	private JScrollPane panel;
	private Properties props;
	
	public ExtendedSerialParameter(String portName, int baudRate,
			int flowControlIn, int flowControlOut, int databits, int stopbits,
			int parity, boolean echo) {
		super(portName, baudRate, flowControlIn, flowControlOut, databits,
				stopbits, parity, echo);
	}
	
	public ExtendedSerialParameter() {
		super();
	}

	public int getUnitId() {
		return unitId;
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPointType() {
		return pointType;
	}

	public void setPointType(int pointType) {
		this.pointType = pointType;
	}

	public int getPollDelay() {
		return pollDelay;
	}

	public void setPollDelay(int pollDelay) {
		this.pollDelay = pollDelay;
	}

	public long getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(long uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public Map<Long,String> getMemoryMappings() {
		return memoryMappings;
	}

	public void setMemoryMappings(Map<Long,String> memoryMappings) {
		this.memoryMappings = memoryMappings;
		setReference((int)getRegisterReference(memoryMappings));
		setCount(getRegisterCount(memoryMappings));
		setRequiredRegisters(getPersistRegisters(memoryMappings.keySet()));
	}

	public String getGroupKey(){
		StringBuilder builder = new StringBuilder();
		builder.append(getBaudRate());
		builder.append(getDatabits());
		builder.append(getParity());
		builder.append(getStopbits());
		return builder.toString();
	}
	
	public int[] getRequiredRegisters() {
		return requiredRegisters;
	}

	public void setRequiredRegisters(int[] requiredRegisters) {
		this.requiredRegisters = requiredRegisters;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public Register[] getRegisteres() {
		return registeres;
	}

	public void setRegisteres(Register[] registeres) {
		this.registeres = registeres;
	}

	public JScrollPane getPanel() {
		return panel;
	}

	public void setPanel(JScrollPane panel) {
		this.panel = panel;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
	}
}

