package com.ems.response.handlers;

import static com.ems.constants.EmsConstants.GAP_BETWEEN_REQUEST;

import javax.swing.JProgressBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.internalframes.PollingIFrame;
import com.ems.UI.swingworkers.DBPollingWorker;
import com.ems.concurrency.ConcurrencyUtils;

public class PollingResponseHandler implements ResponseHandler{
	
	private static final Logger logger = LoggerFactory
			.getLogger(DBPollingWorker.class);
	
	private PollingIFrame frame = null;

	@Override
	public void preStart() {
		startPolling();
	}

	private void startPolling() {
		if(getFrame() != null){
			JProgressBar progress = getFrame().getProgressBar();
			progress.setVisible(true);
		}
	}
	
	private void stopPolling() {
		if(getFrame() != null){
			JProgressBar progress = getFrame().getProgressBar();
			progress.setVisible(false);
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
			DBPollingWorker pollingWorker = new DBPollingWorker(parameter);
			pollingWorker.setTable(getFrame().getTable());
			ConcurrencyUtils.execute(pollingWorker);
			Thread.sleep(GAP_BETWEEN_REQUEST);
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Response handler error : {}", e.getLocalizedMessage());
		}
	}

	@Override
	public void postStop() {
		stopPolling();
	}
}
