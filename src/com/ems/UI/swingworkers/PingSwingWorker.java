package com.ems.UI.swingworkers;

import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.constants.EmsConstants;
import com.ems.modbus.actions.ConnectionManager;
import com.ghgande.j2mod.modbus.msg.ModbusResponse;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.procimg.Register;

public class PingSwingWorker extends SwingWorker<Object, Object> {

	private static final Logger logger = LoggerFactory
			.getLogger(PingSwingWorker.class);
	ExtendedSerialParameter params;
	Object responseWriter;

	public PingSwingWorker(ExtendedSerialParameter params, Object responseWriter) {
		this.params = params;
		this.responseWriter = responseWriter;
	}

	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while searching files");
		}
	}

	@Override
	protected Object doInBackground() throws Exception {
		logger.debug("Ping in progress");

		JTextArea responseArea = (JTextArea) responseWriter;

		try {
			//Run task until task cancelled
			for (;!isCancelled();) {
				failIfInterrupted();
				ReadMultipleRegistersResponse multiResponse = null;
				
				synchronized (EmsConstants.MUTEX) {
					SerialConnection connection = ConnectionManager
							.getConnection(params);
					logger.info(" Connection status " + connection.isOpen());
					failIfInterrupted();
					ModbusResponse response = ConnectionManager.sendRequest(params,
							connection);
					logger.info("Ping worker params: {}", params);
					multiResponse = (ReadMultipleRegistersResponse) response;
					connection.close();
				}
				
				logger.info(" Got Success Response from slave ",
						multiResponse.getTransactionID());
				String successResponse = prepareResponse(params,
						multiResponse.getRegisters(), null);
				responseArea.setText(successResponse);

				failIfInterrupted();
				Thread.sleep(params.getPollDelay());
			}
		} catch (Exception e) {
			logger.error("Ping worker error ", e.getLocalizedMessage());
			String errorResponse = prepareResponse(params, null,
					" Error in Pinging " + e.getLocalizedMessage());
			responseArea.setText(errorResponse);
			logger.error("{}",e);
		}

		logger.debug("Ping in completed");
		return "Ping completed";
	}

	@Override
	protected void process(List<Object> arg0) {
		super.process(arg0);
	}

	private String prepareResponse(ExtendedSerialParameter params,
			Register[] regs, String error) {
		StringBuilder builder = new StringBuilder();

		if (error == null && regs != null && params.getCount() == regs.length) {
			for (int i = 1; i <= params.getCount(); i++) {
				builder.append(params.getReference() + i + " = "
						+ String.format("%05d", regs[i - 1].getValue()) + ",\t");
				if(i % 8 == 0)
					builder.append(System.lineSeparator());
			}
		} else {
			builder.append(error);
		}
		return builder.toString();
	}
}
