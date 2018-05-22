package org.ems.service;

import java.util.List;
import java.util.Map;

import org.ems.dao.DeviceMgmtDAO;
import org.ems.model.DeviceForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.constants.QueryConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeviceManagementService {

	private static final Logger logger = LoggerFactory.getLogger(DeviceManagementService.class);
	
	@Autowired
	private DeviceMgmtDAO dao;
	
	public Map<Long, String> getAllDeviceDetails() {
		
		return dao.getAllDeviceDetails(QueryConstants.SELECT_DEVICES);
	}
	
	public String removeDevice(long deviceId) {
		
		String response = null;
		try {
			if(deviceId != 0) {
				if(this.dao.removeDeviceId(QueryConstants.REMOVE_DEVICES, deviceId)) {
					response = "true";
				}
			}
		} catch (Exception ex) {
			response = ex.getMessage();
			ex.printStackTrace();
		}
		return response;
	}
	
	public String getDeviceDetails(long deviceId) {
		
		if(deviceId != 0) {
			List<DeviceDetailsDTO> detailsDTOs = this.dao.readDeviceDetailsById(QueryConstants.SELECT_DEVICE_BY_ID, deviceId);
			if(detailsDTOs != null && !detailsDTOs.isEmpty()) {
				try {
					return new ObjectMapper().writeValueAsString(detailsDTOs.get(0));
				} catch (JsonProcessingException e) {
					logger.error(e.getMessage());
				}
			}
		}
		return null;
	}
	
	public String updateDeviceDetail(String form) {
		
		try {
			DeviceForm deviceForm = new ObjectMapper().readValue(form, DeviceForm.class);
			if(this.dao.updateDeviceDetailsById(deviceForm) > 0) {
				return "success";
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "";
	}
	
	public String saveDeviceDetail(String form) {
		try {
			DeviceForm deviceForm = new ObjectMapper().readValue(form, DeviceForm.class);
			if(this.dao.saveDevice(deviceForm) > 0) {
				return "success";
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "";
	}
}
