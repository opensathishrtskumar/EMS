package com.ems.UI.swingworkers;

import static com.ems.util.EMSUtility.processRequiredRegister;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.db.DBConnectionManager;
import com.ems.util.EMSUtility;

public class DBPollingWorker implements Callable<Object> {
	private static final Logger logger = LoggerFactory.getLogger(DBPollingWorker.class);

	private ExtendedSerialParameter parameters;

	public DBPollingWorker(ExtendedSerialParameter parameters) {
		this.parameters = parameters;
	}

	@Override
	public Object call() throws Exception {

		boolean status = true;
		
		if(parameters.isSplitJoin()){
			status = EMSUtility.splitJoinStatus(parameters.getSplitJoinDTO().getStatus());
			logger.trace("Split Join final response Status {}", status);
		} else {
			status = parameters.isStatus();
		}

		PollingDetailDTO dto = new PollingDetailDTO(parameters.getUniqueId(), System.currentTimeMillis(),
				null);
		dto.setStatus(status);
		
		if (status) {
			String finalResponse = processRequiredRegister(parameters);
			dto.setUnitresponse(finalResponse);
			int insert = DBConnectionManager.insertPollingDetails(dto);
			logger.debug(" insert poll response unit : {}, status : {}", parameters.getUniqueId(), insert);
		}

		int count = DBConnectionManager.updateRecentPolling(dto);
		
		if(count == 0){
			DBConnectionManager.insertRecentPolling(dto);
		}
		
		return "Polling completed...";
	}
}
