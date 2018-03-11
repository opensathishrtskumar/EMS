package com.ems.UI.swingworkers;

import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.response.handlers.ResponseHandler;

public class DisplayDeviceWorker extends SwingWorker<Object, Object> {

	private static final Logger logger = LoggerFactory.getLogger(DisplayDeviceWorker.class);

	private Map<String, List<ExtendedSerialParameter>> groupedDevices = null;
	private int refreshFrequency = 1000 * 15;
	private ResponseHandler responseHandler = null;

	public ResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public void setResponseHandler(ResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	public int getRefreshFrequency() {
		return refreshFrequency;
	}

	public void setRefreshFrequency(int refreshFrequency) {
		this.refreshFrequency = refreshFrequency;
	}

	public DisplayDeviceWorker(Map<String, List<ExtendedSerialParameter>> groupedDevices) {
		this.groupedDevices = groupedDevices;
	}

	public Map<String, List<ExtendedSerialParameter>> getGroupedDevices() {
		return groupedDevices;
	}

	public void setGroupedDevices(Map<String, List<ExtendedSerialParameter>> groupedDevices) {
		this.groupedDevices = groupedDevices;
	}

	@Override
	protected Object doInBackground() throws Exception {
		failIfInterrupted();

		logger.info("Grouped device worker initialized...");

		if (getResponseHandler() != null)
			getResponseHandler().preStart();

		for (; !isCancelled();) {

			if (getGroupedDevices() == null || getGroupedDevices().size() == 0) {
				logger.info("No devices configured to poll - QUIT!!!");
				break;
			}

			for (Entry<String, List<ExtendedSerialParameter>> group : getGroupedDevices().entrySet()) {
				logger.debug("Invoking group : {}", group.getKey());
				List<ExtendedSerialParameter> deviceList = group.getValue();

				failIfInterrupted();

				if (deviceList != null && deviceList.size() >= 1) {
					ExtendedSerialParameter connectionParam = deviceList.get(0);

					try {
						logger.debug("connection using parameters : {}", connectionParam);

						for (ExtendedSerialParameter device : deviceList) {

							if (getResponseHandler() != null)
								getResponseHandler().handleResponse(device);
						}
					} catch (Exception e) {
						logger.error("Dashboard worker group iteration failed : {}, : {}", group.getKey(), e);
					}
				}
			}

			failIfInterrupted();
			// Configure from properties
			Thread.sleep(refreshFrequency);
		}

		if (getResponseHandler() != null)
			getResponseHandler().postStop();

		logger.info("Grouped device worker completed...");

		return "Grouped device worker completed";
	}

	private void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted() || isCancelled()) {
			throw new InterruptedException("Dashboard worker Stopped...");
		}
	}

	@Override
	protected void finalize() throws Throwable {
	}
}
