package com.ems.response.handlers;

import static com.ems.util.EMSUtility.processRegistersForDashBoard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.db.DBConnectionManager;
import com.ems.util.EMSUtility;

public class LiveResponseHandler implements ResponseHandler {

	private static final Logger logger = LoggerFactory.getLogger(LiveResponseHandler.class);
	private Map<String,Long> readings = new ConcurrentHashMap<>();
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

	@Override
	public void preStart() {
		//initialize with default values	
		for(String series : seriesNames){
			readings.put(series, 0l);
		}
		
		Properties props = EMSUtility.loadProperties(device.getMemoryMapping());
		this.memoryMapping = EMSUtility.convertProp2Map(props);
	}
	
	@Override
	public void handleResponse(ExtendedSerialParameter device) {

		if (device == null)
			return;
		
		try {
			
			Map<String, String> registerValue = null;
			
			if(device.isStatus() 
					|| (device.isSplitJoin() 
							&& EMSUtility.splitJoinStatus(device.getSplitJoinDTO().getStatus()))){//Modbus Request is success
				registerValue = processRegistersForDashBoard(device);
			} else {
				//Load recent polling response from DB for the first time;
				List<PollingDetailDTO> list = DBConnectionManager.
						fetchRecentPollingDetails(device.getUniqueId());
				if(list.size() > 0){
					Properties props = EMSUtility.loadProperties(list.get(0).getUnitresponse());
					registerValue = EMSUtility.convertProp2Map(props);
				}
			}
			
			if(registerValue != null){
				
				for(Entry<String, String> entry : registerValue.entrySet()){
					String series = memoryMapping.get(entry.getKey());
					if(series != null){
						readings.put(series, new BigDecimal(entry.getValue()).longValue());
					}
				}
			}
			
		} catch (Exception e) {
			logger.error("Live reposnse handler error : {}", e);
		} finally {
			
		}
	}
	
	public Map<String, Long> getReadings() {
		return readings;
	}

	@Override
	public void postStop() {

	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
