package com.ems.UI.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ghgande.j2mod.modbus.msg.ModbusResponse;

public class MemoryMappingsDTO {
	private static final Logger logger = LoggerFactory.getLogger(MemoryMappingsDTO.class);
	private Properties properties;
	private List<Memory> registeres;
	private ModbusResponse response;
	
	
	public MemoryMappingsDTO(Properties properties, ModbusResponse response) {
		this.properties = properties;
		this.response = response;
	}
	
	public Properties getProperties() {
		return properties;
	}

	public List<Memory> getRegisteres() {
		return registeres;
	}
	
	public void process(){
		if(properties == null)
			throw new NullPointerException("Memory Mapping properties cannot be null");
		
		registeres = new ArrayList<MemoryMappingsDTO.Memory>();
		
		try {
			
			
			
		} catch (Exception e) {
			logger.error("Error Processing response : {}", e.getLocalizedMessage());
			logger.error("{}",e);
		}
		
	}
	
	class Memory{
		private long memory;
		private long value;
		private String mapping;
		public long getMemory() {
			return memory;
		}
		public void setMemory(long memory) {
			this.memory = memory;
		}
		public long getValue() {
			return value;
		}
		public void setValue(long value) {
			this.value = value;
		}
		public String getMapping() {
			return mapping;
		}
		public void setMapping(String mapping) {
			this.mapping = mapping;
		}
	}
}
