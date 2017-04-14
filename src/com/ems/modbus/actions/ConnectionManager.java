package com.ems.modbus.actions;

import static com.ems.constants.EmsConstants.TIMEOUT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.util.SerialParameters;

public abstract class ConnectionManager {
	private static final Logger logger = LoggerFactory
			.getLogger(ConnectionManager.class);

	private static SerialConnection connection = null;

	public static SerialConnection getConnection(SerialParameters parameters) {
		if (connection == null) {
			connection = new SerialConnection(parameters);
			connection.setTimeout(TIMEOUT[1]);

			connection.open();
		}

		return connection;
	}

	public static ModbusResponse sendRequest(ExtendedSerialParameter params, 
			SerialConnection connection) throws ModbusException {
		ExtendedSerialParameter extendedParams = params;
		ModbusRequest request = getModbusRequest(params);
		ModbusResponse response = null;

		ModbusSerialTransaction tran = new ModbusSerialTransaction(
				connection);
		tran.setRequest(request);
		tran.setRetries(extendedParams.getRetries());

		synchronized (connection) {
			if(logger.isDebugEnabled())
				logger.debug("executing serial transaction");
			tran.execute();
			response = tran.getResponse();
		}

		return response;
	}

	private static ModbusRequest getModbusRequest(ExtendedSerialParameter params){

		ModbusRequest request = ModbusRequest.createModbusRequest(params.getPointType());
		request.setUnitID(params.getUnitId());

		if(request instanceof ReadMultipleRegistersRequest){
			ReadMultipleRegistersRequest multipleRegister = (ReadMultipleRegistersRequest)request;
			multipleRegister.setWordCount(params.getCount());
			multipleRegister.setReference(params.getReference());
		} else {
			//TODO : for future support
		}

		return request;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 * 
	 * Close the conneciton if not closed
	 */
	@Override
	protected void finalize() throws Throwable {
		if(connection != null && connection.isOpen()){
			connection.close();
		}
		super.finalize();
	}
}
