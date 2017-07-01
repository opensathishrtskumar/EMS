package com.ems.UI.dto;

import java.io.Serializable;
import java.util.List;

public class GroupDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private String groupName;
	private String groupDescription;
	private List<DeviceDetailsDTO> devices;
	
	public GroupDTO() {
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupDescription() {
		return groupDescription;
	}

	public void setGroupDescription(String groupDescription) {
		this.groupDescription = groupDescription;
	}

	public List<DeviceDetailsDTO> getDevices() {
		return devices;
	}

	public void setDevices(List<DeviceDetailsDTO> devices) {
		this.devices = devices;
	}

	@Override
	public String toString() {
		return  groupName;
	}
}
