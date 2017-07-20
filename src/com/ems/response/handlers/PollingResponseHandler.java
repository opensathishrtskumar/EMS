package com.ems.response.handlers;

import static com.ems.constants.EmsConstants.GAP_BETWEEN_REQUEST;

import javax.swing.JProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.internalframes.PollingIFrame;
import com.ems.UI.swingworkers.DBPollingWorker;
import com.ems.concurrency.ConcurrencyUtils;

/**
 * @author Sathish Kumar
 *
 *         Implementation is responsible for persisting device response
 */
public class PollingResponseHandler implements ResponseHandler {

	private static final Logger logger = LoggerFactory.getLogger(PollingResponseHandler.class);

	private PollingIFrame frame = null;

	@Override
	public void preStart() {
		startPolling();
	}

	private void startPolling() {
		if (getFrame() != null) {
			JProgressBar progress = getFrame().getProgressBar();
			progress.setVisible(true);
		}
	}

	public PollingIFrame getFrame() {
		return frame;
	}

	public void setFrame(PollingIFrame frame) {
		this.frame = frame;
	}

	@Override
	public void handleResponse(ExtendedSerialParameter parameter) {
		try {
			//New worker is created to persist Device response concurrently
			DBPollingWorker pollingWorker = new DBPollingWorker(parameter);
			pollingWorker.setTable(getFrame().getTable());
			ConcurrencyUtils.execute(pollingWorker);
			//Wait before submitting new request to the same connection 
			Thread.sleep(GAP_BETWEEN_REQUEST);
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Response handler error : {}", e.getLocalizedMessage());
		}
	}

	@Override
	public void postStop() {
		stopPolling();
	}

	private void stopPolling() {
		if (getFrame() != null) {
			JProgressBar progress = getFrame().getProgressBar();
			progress.setVisible(false);
		}
	}
}
