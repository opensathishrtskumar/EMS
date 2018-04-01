package org.ems.service;

import java.util.List;

import org.ems.cache.CacheUtil;
import org.ems.dao.DeviceDetailsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ems.UI.dto.DeviceDetailsDTO;

@Service
public class ReportService {

	private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

	@Autowired
	private CacheUtil cacheUtil;

	@Autowired
	private DeviceDetailsDAO deviceDetailsDAO;

	public List<DeviceDetailsDTO> fetchActiveDevices() {
		return deviceDetailsDAO.fetchActiveDeviceDetails();
	}

}
