package com.ems.modbus.actions;

import static com.ems.constants.EmsConstants.GAP_BETWEEN_REQUEST;
import static com.ems.constants.EmsConstants.TIMEOUT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.util.EMSUtility;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.SerialParameters;

public abstract class ConnectionManager {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

	public static SerialConnection getConnection(SerialParameters parameters) {
		SerialConnection connection = new SerialConnection(parameters);
		connection.setTimeout(TIMEOUT[1]);
		connection.open();
		return connection;
	}
	
	
	public static InputRegister[] executeTransaction(SerialConnection connection, ExtendedSerialParameter device)
			throws ModbusException {

		InputRegister[] responseRegisters = execute(connection, device.getMethod(), device.getReference(),
				device.getCount(), device.getUnitId(), device.getUniqueId(), device.getRetries());
		
		return responseRegisters;
	}
	
	
	public static InputRegister[] execute(SerialConnection connection, String method, int reference, int count, int unitId,
			long uniqueId, int retries) throws ModbusException {
		//Atleast minimum gap between every request is required even in same connection
		try{ Thread.sleep(GAP_BETWEEN_REQUEST); } catch(Exception e){}
		
		ModbusSerialTransaction tran = new ModbusSerialTransaction(connection);
		ModbusRequest request = EMSUtility.getRequest(method, reference, count);
		logger.trace("Hex request : {}, Ref : {}, Count : {}, Function code {}, UniqueId {}", request.getHexMessage(),
				reference, count, request.getFunctionCode(), uniqueId);
		request.setUnitID(unitId);
		tran.setRequest(request);
		tran.setRetries(retries);
		tran.execute();
		
		ModbusResponse response = tran.getResponse();
		logger.trace("UniqueId {} : Dashboard device response : {} ", uniqueId, response.getHexMessage());
		tran = null;
		return getResponseRegisters(response);
	}
	
	public static InputRegister[] getResponseRegisters(ModbusResponse response) {
		if (response instanceof ReadMultipleRegistersResponse) {
			return ((ReadMultipleRegistersResponse) response).getRegisters();
		} else {
			return ((ReadInputRegistersResponse) response).getRegisters();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 * 
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}
