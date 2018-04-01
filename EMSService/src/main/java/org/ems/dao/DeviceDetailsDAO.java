package org.ems.dao;

import static org.ems.cache.CacheConstants.ACTIVE_DEVICES;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.ems.cache.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Repository;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.SettingsDTO;
import com.ems.constants.QueryConstants;

@Repository
@DependsOn({ "pollingDetailsDAO" })
public class DeviceDetailsDAO {

	private static final Logger logger = LoggerFactory.getLogger(DeviceDetailsDAO.class);

	@Autowired
	private CacheUtil cacheUtil;

	@Autowired
	private PollingDetailsDAO pollingDao;

	private List<DeviceDetailsDTO> deviceDetails;
	private Map<String, String> settings;

	@PostConstruct
	public void init() {
		loadDeviceDetails();
		loadSettings();
	}

	private void loadDeviceDetails() {
		logger.debug("loading active devices...");
		this.deviceDetails = pollingDao.fetchAllDeviceDetails(QueryConstants.SELECT_ENABLED_ENDEVICES, new Object[] {});
		logger.debug("loaded active devices...");
	}

	private Map<String, String> loadSettings() {
		logger.debug("loading settings...");
		List<SettingsDTO> settingsList = pollingDao.fetchSettings();
		this.settings = new HashMap<>();

		for (SettingsDTO setting : settingsList) {
			this.settings.put(setting.getSkey(), setting.getSvalue());
		}

		logger.debug("settings loaded...");
		return this.settings;
	}

	public List<DeviceDetailsDTO> getDeviceDetails() {
		return deviceDetails;
	}

	public Map<String, String> getSettings() {
		return settings;
	}

	public List<DeviceDetailsDTO> fetchActiveDeviceDetails() {
		logger.debug("loading active devices...");
		List<DeviceDetailsDTO> activeDevices = cacheUtil.getCacheEntry(ACTIVE_DEVICES, List.class);

		if (activeDevices == null) {
			activeDevices = pollingDao.fetchAllDeviceDetails(QueryConstants.SELECT_ENABLED_ENDEVICES, new Object[] {});
			cacheUtil.putCacheEntry(ACTIVE_DEVICES, activeDevices);
		}

		logger.debug("loaded active devices...");
		return deviceDetails;
	}

}