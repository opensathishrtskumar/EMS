package com.ems.UI.swingworkers;

import static com.ems.constants.EmsConstants.MUTEX;

import java.util.List;

import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.modbus.actions.ConnectionManager;
import com.ems.util.EMSUtility;
import com.ghgande.j2mod.modbus.net.SerialConnection;
import com.ghgande.j2mod.modbus.procimg.InputRegister;

public class PingSwingWorker extends SwingWorker<Object, Object> {

	private static final Logger logger = LoggerFactory.getLogger(PingSwingWorker.class);
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
			// Run task until task cancelled
			for (; !isCancelled();) {
				failIfInterrupted();
				InputRegister[] registers = null;
				
				//During pinging Split Join changes not required
				synchronized (MUTEX) {
					SerialConnection connection = ConnectionManager.getConnection(params);
					registers = ConnectionManager.executeTransaction(connection, params);
					logger.trace("Ping worker params: {}", params);
					closeSerialConnnection(connection);
				}

				logger.debug(" Got Success Response from slave ", params.getUniqueId());
				String successResponse = prepareResponse(params, registers, null);
				responseArea.setText(successResponse);

				failIfInterrupted();
				Thread.sleep(params.getPollDelay());
			}
		} catch (Exception e) {
			logger.error("Ping worker error {} Error {}", e.getLocalizedMessage(), e);
			String errorResponse = prepareResponse(params, null, " Error in Pinging " + e.getLocalizedMessage());
			responseArea.setText(errorResponse);
		}

		logger.debug("Ping in completed");
		return "Ping completed";
	}

	@Override
	protected void process(List<Object> arg0) {
		super.process(arg0);
	}

	private String prepareResponse(ExtendedSerialParameter params, InputRegister[] regs, String error) {
		StringBuilder builder = new StringBuilder();

		if (error == null && regs != null && params.getCount() == regs.length) {
			for (int i = 1; i <= params.getCount(); i++) {
				builder.append(
						params.getReference() + i + " = " + String.format("%05d", regs[i - 1].getValue()) + ",\t");
				if (i % 8 == 0)
					builder.append(System.lineSeparator());
			}
		} else {
			builder.append(error);
		}
		return builder.toString();
	}

	private void closeSerialConnnection(SerialConnection connection) {
		if (connection != null && connection.isOpen()) {
			connection.close();
			connection = null;
		}
	}
}
