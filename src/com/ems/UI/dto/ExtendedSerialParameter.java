package com.ems.UI.dto;

import com.ghgande.j2mod.modbus.util.SerialParameters;

public class ExtendedSerialParameter  extends SerialParameters{

	private int unitId;
	private int retries;
	private int timeout;
	private int reference;
	private int count;
	private int pointType;
	private int pollDelay;

	public ExtendedSerialParameter() {
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
}
