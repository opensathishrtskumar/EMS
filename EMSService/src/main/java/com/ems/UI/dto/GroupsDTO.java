package com.ems.UI.dto;

import java.io.Serializable;
import java.util.List;

public class GroupsDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	private long timestamp;
	private String label;
	private List<GroupDTO> groups;
	
	public GroupsDTO() {
		
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<GroupDTO> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupDTO> groups) {
		this.groups = groups;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label ;
	}
	
	
}
