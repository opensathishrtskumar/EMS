package com.ems.UI.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ghgande.j2mod.modbus.procimg.InputRegister;

public class SplitJoinDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private List<Integer> count;
	private List<Integer> referencce;
	private List<Integer[]> requiredRegisters;
	private List<Map<Long,String>> memoryMappings;
	private List<InputRegister[]> registeres;
	
	
	public SplitJoinDTO() {
		this.count = new ArrayList<>();
		this.referencce = new ArrayList<>();
		this.requiredRegisters = new ArrayList<>();
		this.memoryMappings = new ArrayList<>();
	}

	public List<Integer> getCount() {
		return count;
	}

	public void setCount(List<Integer> count) {
		this.count = count;
	}

	public List<Integer> getReferencce() {
		return referencce;
	}

	public void setReferencce(List<Integer> referencce) {
		this.referencce = referencce;
	}

	public List<Integer[]> getRequiredRegisters() {
		return requiredRegisters;
	}

	public void setRequiredRegisters(List<Integer[]> requiredRegisters) {
		this.requiredRegisters = requiredRegisters;
	}

	public List<Map<Long, String>> getMemoryMappings() {
		return memoryMappings;
	}

	public void setMemoryMappings(List<Map<Long, String>> memoryMappings) {
		this.memoryMappings = memoryMappings;
	}

	public List<InputRegister[]> getRegisteres() {
		return registeres;
	}

	public void setRegisteres(List<InputRegister[]> registeres) {
		this.registeres = registeres;
	}
}
