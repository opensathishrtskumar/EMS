package com.ems.response.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.swingworkers.DBPollingWorker;
import com.ems.concurrency.ConcurrencyUtils;

/**
 * @author Sathish Kumar
 *
 *         Implementation is responsible for persisting device response
 */
public class PollingResponseHandler implements ResponseHandler {

	private static final Logger logger = LoggerFactory.getLogger(PollingResponseHandler.class);

	@Override
	public void preStart() {
	}

	@Override
	public void handleResponse(ExtendedSerialParameter parameter) {
		try {
			//New worker is created to persist Device response concurrently
			DBPollingWorker pollingWorker = new DBPollingWorker(parameter);
			logger.trace("Persist worker triggered {}", parameter.getUniqueId());
			ConcurrencyUtils.execute(pollingWorker);
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Response handler error : {}", e.getLocalizedMessage());
		}
	}

	@Override
	public void postStop() {
	}

}
