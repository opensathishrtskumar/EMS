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
import com.ems.response.handlers.ResponseHandler;
import com.ems.util.EMSUtility;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;

public class GroupedDeviceWorker extends SwingWorker<Object, Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(GroupedDeviceWorker.class);

	private Map<String, List<ExtendedSerialParameter>> groupedDevices = null;
	private ModbusResponse response = null;
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

	public void setGroupedDevices(
			Map<String, List<ExtendedSerialParameter>> groupedDevices) {
		this.groupedDevices = groupedDevices;
	}

	@Override
	protected Object doInBackground() throws Exception {
		failIfInterrupted();

		logger.info("Dashboard worker initialized...");
		
		if(getResponseHandler() != null)
			getResponseHandler().preStart();
		
		for (;!isCancelled();) {

			if(getGroupedDevices() == null || getGroupedDevices().size() == 0){
				logger.info("NO Dashboard devices configured");
				break;
			}

			try {
				//Iterate through groups
				for (Entry<String, List<ExtendedSerialParameter>> group : getGroupedDevices()
						.entrySet()) {
					logger.debug("Invoking group : {}", group.getKey());
					List<ExtendedSerialParameter> deviceList = group.getValue();

					failIfInterrupted();

					if(deviceList != null && deviceList.size() >= 1 ){
						ExtendedSerialParameter connectionParam = deviceList.get(0);

						//To continue with other group even if one group fails
						try {
							//Create connection in sync environment
							synchronized (MUTEX) {
								logger.debug("connection using parameters : {}", connectionParam);
								connection = new SerialConnection(connectionParam);
								connection.setTimeout(connectionParam.getTimeout());
								logger.trace("Trying to obtain connection : {}, {}", Thread
										.currentThread().getName(), connectionParam);
								connection.open();

								//Iterate through each group devices
								for(ExtendedSerialParameter device : deviceList){
									device.setStatus(false);
									device.setRegisteres(null);
									try {
										ModbusSerialTransaction tran = new ModbusSerialTransaction(
												connection);
										logger.trace("Reading values for {}", device);
										ModbusRequest request = EMSUtility.getRequest(device.getMethod(),
												device.getReference(), device.getCount());
										logger.debug("Function code {},Hex Request {}", request.getFunctionCode(),
												request.getHexMessage());
										logger.trace(
												"Polling worker Hex request : {}, Ref : {}, Count : {}",
												request.getHexMessage(), device.getReference(),
												device.getCount());
										request.setUnitID(device.getUnitId());
										tran.setRequest(request);
										tran.setRetries(device.getRetries());
										tran.execute();
										response =  tran.getResponse();
										tran = null;
										logger.trace("Dashboard device response : {} ", response.getHexMessage());
										device.setRegisteres(EMSUtility.getResponseRegisters(response));
										device.setStatus(true);
									} catch (Exception e) {
										logger.error("{}",e);
										logger.error("Device polling failed : {}", device);
									}
									
									if(getResponseHandler() != null)
										getResponseHandler().handleResponse(device);
								}

								//Close connection of each group
								closeSerialConnnection(connection);
							}
						} catch (Exception e) {
							logger.error("{}",e);
							logger.error(
									"Dashboard worker group iteration failed : {}, : {}",
									group.getKey(), e.getLocalizedMessage());
						} finally {
							closeSerialConnnection(connection);
							logger.debug("Closing serial connection in group");
						}
					}
				}

			} catch (Exception e) {
				logger.error("{}",e);
				logger.error("Dashboard worker failed : {}",e.getLocalizedMessage());
			} finally {
				closeSerialConnnection(this.connection);
			}

			failIfInterrupted();
			//Configure from properties
			Thread.sleep(refreshFrequency);
		}

		if(getResponseHandler() != null)
			getResponseHandler().postStop();
		
		logger.info("Grouped device worker completed...");

		return "Grouped device worker completed";
	}

	private void closeSerialConnnection(SerialConnection connection){
		if(connection != null && connection.isOpen()){
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
