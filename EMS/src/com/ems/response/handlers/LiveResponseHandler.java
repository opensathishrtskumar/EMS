package com.ems.response.handlers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.db.DBConnectionManager;
import com.ems.util.EMSUtility;

public class LiveResponseHandler implements ChartDataGenerator {

	private static final Logger logger = LoggerFactory.getLogger(LiveResponseHandler.class);
	private Map<String, Number> readings = new ConcurrentHashMap<>();
	private String[] seriesNames = null;
	private Map<String, String> memoryMapping = null;
	private DeviceDetailsDTO device;

	public LiveResponseHandler(String[] seriesNames) {
		this.seriesNames = seriesNames;
	}

	public DeviceDetailsDTO getDevice() {
		return device;
	}

	public LiveResponseHandler setDevice(DeviceDetailsDTO device) {
		this.device = device;
		return this;
	}

	public LiveResponseHandler build() {
		preStart();
		return this;
	}

	public void preStart() {
		// initialize with default values
		for (String series : seriesNames) {
			readings.put(series, 0l);
		}

		Properties props = EMSUtility.loadProperties(device.getMemoryMapping());
		this.memoryMapping = EMSUtility.convertProp2Map(props);
	}

	public void handleResponse() {

		try {
			Map<String, String> registerValue = null;

			// Load recent polling response from DB for the first time;
			List<PollingDetailDTO> list = DBConnectionManager.fetchRecentPollingDetails(this.device.getUniqueId());
			if (list.size() > 0) {
				Properties props = EMSUtility.loadProperties(list.get(0).getUnitresponse());
				registerValue = EMSUtility.convertProp2Map(props);
			}

			if (registerValue != null) {
				for (Entry<String, String> entry : registerValue.entrySet()) {
					String series = memoryMapping.get(entry.getKey());
					if (series != null) {
						readings.put(series, new BigDecimal(entry.getValue()));
					}
				}
			}

		} catch (Exception e) {
			logger.error("Live reposnse handler error : {}", e);
		}
	}

	public Map<String, Number> getReadings() {
		handleResponse();
		return readings;
	}
}
