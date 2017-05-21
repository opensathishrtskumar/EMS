package com.ems.UI.dto;

import java.io.Serializable;
import java.util.List;

public class ManageDeviceDetailsDTO implements Serializable{
	private static final long serialVersionUID = -2362174882130724654L;

	private List<DeviceDetailsDTO> list;
	private String portName;
	private String pollingDelay;
	
	public ManageDeviceDetailsDTO() {
	}

	public List<DeviceDetailsDTO> getList() {
		return list;
	}

	public void setList(List<DeviceDetailsDTO> list) {
		this.list = list;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPollingDelay() {
		return pollingDelay;
	}

	public void setPollingDelay(String pollingDelay) {
		this.pollingDelay = pollingDelay;
	}
}
