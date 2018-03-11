package com.ems.UI.swingworkers;

import static com.ems.constants.EmsConstants.MUTEX;
import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.SplitJoinDTO;
import com.ems.modbus.actions.ConnectionManager;
import com.ems.response.handlers.ResponseHandler;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

public class GroupedDeviceWorker extends SwingWorker<Object, Object> {

	private static final Logger logger = LoggerFactory.getLogger(GroupedDeviceWorker.class);

	private Map<String, List<ExtendedSerialParameter>> groupedDevices = null;
	private SerialConnection connection = null;
	private int refreshFrequency = DASHBOARD_REFRESH_FREQUENCY * 1000 * 60;
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

	public GroupedDeviceWorker(Map<String, List<ExtendedSerialParameter>> groupedDevices) {
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

			try {
				// Iterate through each groups
				for (Entry<String, List<ExtendedSerialParameter>> group : getGroupedDevices().entrySet()) {
					logger.debug("Invoking group : {}", group.getKey());
					List<ExtendedSerialParameter> deviceList = group.getValue();

					failIfInterrupted();

					if (deviceList != null && deviceList.size() >= 1) {
						ExtendedSerialParameter connectionParam = deviceList.get(0);

						// To continue with other group even if one group fails
						try {
							/*Create connection in sync block - shares with Dashboard, Grouped devices & Poller*/ 
							synchronized (MUTEX) {
								logger.debug("connection using parameters : {}", connectionParam);
								this.connection = ConnectionManager.getConnection(connectionParam);

								/* Iterate through each devices available in group */
								for (ExtendedSerialParameter device : deviceList) {
									if(!device.isSplitJoin()){
										try {
											logger.debug("Request execution started...");
											InputRegister[] registers = ConnectionManager.executeTransaction(connection, device);
											device.setRegisteres(registers);
											device.setStatus(true);//Mark as execution success
											logger.debug("Request execution completed...");
										} catch (Exception e) {
											logger.error("Device polling failed : {} error : {}", device, e);
										}
									} else {
										try {
											logger.debug("Split JOIN execution started...");
											executeSplitJoin(connection, device);
											logger.debug("Split JOIN execution completed...");
										} catch (Exception e) {
											logger.error("splitJoin polling failed : {} error : {}", device, e);
										}
									}

									if (getResponseHandler() != null)
										getResponseHandler().handleResponse(device);
								}

								// Close connection of each group post execution
								closeSerialConnnection(this.connection);
							}
						} catch (Exception e) {
							logger.error("Dashboard worker group iteration failed : {}, : {}", group.getKey(), e);
						} finally {
							closeSerialConnnection(this.connection);
							logger.debug("Closing serial connection in group");
						}
					}
				}

			} catch (Exception e) {
				logger.error("Dashboard worker failed : {}", e);
			} finally {
				closeSerialConnnection(this.connection);
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

	/**
	 * @param connection
	 * @param device
	 * @throws ModbusException
	 * 
	 * Executes split join device request
	 * 
	 */
	private void executeSplitJoin(SerialConnection connection , ExtendedSerialParameter device) throws ModbusException{
		SplitJoinDTO dto = device.getSplitJoinDTO();
		logger.trace("Executing split join request");
		for(int i=0;i<dto.getCount().size();i++){
			InputRegister[] registers = ConnectionManager.execute(connection, device.getMethod(),
					(int) dto.getReferencce().get(i), (int) dto.getCount().get(i), device.getUnitId(),
					device.getUniqueId(), device.getRetries());
			//Set response back to DTO for processing
			dto.getRegisteres().set(i, registers);
			dto.getStatus().set(i, true);
		}
	}
	
	private void closeSerialConnnection(SerialConnection connection) {
		if (connection != null && connection.isOpen()) {
			connection.close();
			connection = null;
		}
	}

	private void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted() || isCancelled()) {
			closeSerialConnnection(this.connection);
			throw new InterruptedException("Dashboard worker Stopped...");
		}
	}

	@Override
	protected void finalize() throws Throwable {
		closeSerialConnnection(this.connection);
	}
}
