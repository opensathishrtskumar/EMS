package com.ems.UI.swingworkers;

import static com.ems.constants.EmsConstants.ENCODING;
import static com.ems.constants.EmsConstants.RETRYCOUNT;
import static com.ems.constants.EmsConstants.TIMEOUT;
import static com.ems.constants.QueryConstants.SELECT_ENABLED_ENDEVICES;
import static com.ems.util.EMSUtility.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JInternalFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.internalframes.PollingIFrame;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.db.DBConnectionManager;
import com.ems.util.EMSUtility;

public class PollingSwingWorker extends SwingWorker<Object, Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(PollingSwingWorker.class);

	private PollingIFrame pollingFrame = null;
	private List<ExtendedSerialParameter> paramsList = null;

	public PollingSwingWorker(JInternalFrame pollingFrame) {
		this.pollingFrame = (PollingIFrame) pollingFrame;
	}

	private void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted() || isCancelled()) {
			throw new InterruptedException(" Stopping worker...");
		}
	}

	private void startPolling() {
		JProgressBar progress = pollingFrame.getProgressBar();
		progress.setVisible(true);
	}

	private void stopPolling() {
		JProgressBar progress = pollingFrame.getProgressBar();
		progress.setVisible(false);
	}

	@Override
	protected Object doInBackground() throws Exception {
		logger.debug("Ping in progress");

		startPolling();

		try {

			PollingIFrame frame = (PollingIFrame) pollingFrame;
			int frequency = Integer.parseInt(frame.getComboBox().getSelectedItem().toString());
			frequency = frequency * 60 * 1000;
			logger.info("Frequency : {}", frame.getComboBox().getSelectedItem());
			String port = frame.getComboBoxPorts().getSelectedItem().toString();
			
			failIfInterrupted();
			final List<DeviceDetailsDTO> availableDevices = DBConnectionManager
					.getAvailableDevices(SELECT_ENABLED_ENDEVICES);
			paramsList = new ArrayList<ExtendedSerialParameter>(
					availableDevices.size());
			
			for (DeviceDetailsDTO devices : availableDevices) {
				devices.setPort(port);
				
				ExtendedSerialParameter parameters = EMSUtility
						.mapDeviceToSerialParam(devices);
				
				paramsList.add(parameters);
			}
			
			//Run until interruped
			for (;!isCancelled();) {
				// Check for interrupt request and quit if interrupted
				failIfInterrupted();

				for (ExtendedSerialParameter request : paramsList) {
					PollingWorker pollingWorker = new PollingWorker(request);
					pollingWorker.setTable(pollingFrame.getTable());
					ConcurrencyUtils.execute(pollingWorker);
				}
				failIfInterrupted();
				Thread.sleep(frequency);
			}
		} catch (Exception e) {
			logger.error("Polling worker failed : {} ", e.getLocalizedMessage());
			logger.error("{}",e);
		}

		stopPolling();
		logger.debug("Polling completed...");
		return "Polling completed";
	}

	@Override
	protected void process(List<Object> arg0) {
		super.process(arg0);
	}
	
	@Override
	protected void finalize() throws Throwable {
		logger.info("Polling worker stopped");
		super.finalize();
	}
	
}
