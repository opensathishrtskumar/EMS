package com.ems.UI.swingworkers;

import static com.ems.constants.EmsConstants.MUTEX;
import static com.ems.util.EMSUtility.*;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.db.DBConnectionManager;
import com.ghgande.j2mod.modbus.io.ModbusSerialTransaction;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersResponse;
import com.ghgande.j2mod.modbus.net.SerialConnection;

public class PollingWorker implements Callable<Object> {
	private static final Logger logger = LoggerFactory
			.getLogger(PollingWorker.class);

	private JTable table;
	private ExtendedSerialParameter parameters;
	private ReadMultipleRegistersResponse response = null;
	private SerialConnection connection = null;

	public PollingWorker(ExtendedSerialParameter parameters) {
		this.parameters = parameters;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	private static void log(String log, Object... obj) {
		logger.debug(log, obj);
	}

	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException(" Stopping worker...");
		}
	}

	@Override
	public Object call() throws Exception {

		boolean status = false;

		// Critical section executing in Sync block
		synchronized (MUTEX) {
			try {
				connection = new SerialConnection(parameters);
				connection.setTimeout(parameters.getTimeout());
				log("Trying to obtain connection : {}, {}", Thread
						.currentThread().getName(), parameters);

				failIfInterrupted();
				connection.open();
				ModbusSerialTransaction tran = new ModbusSerialTransaction(
						connection);
				ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(
						parameters.getReference(), parameters.getCount());
				logger.debug(
						"Polling worker Hex request : {}, Ref : {}, Count : {}",
						request.getHexMessage(), parameters.getReference(),
						parameters.getCount());
				request.setUnitID(parameters.getUnitId());
				tran.setRequest(request);
				tran.setRetries(parameters.getRetries());
				tran.execute();
				response = (ReadMultipleRegistersResponse) tran.getResponse();
				log("Got response for unit {} = {}", parameters.getUnitId(),
						response.getHexMessage());
				status = true;
				//parameters.setRegisteres(response.getRegisters());
				updateWorkerStatus("Poll Success - " + getHHmm());
			} catch (Exception e) {
				log("eception in connection : {}, device info : {}, Port Name : {}",
						e.getLocalizedMessage(),
						parameters.getUniqueId() + " "
								+ parameters.getReference() + " "
								+ parameters.getCount(),parameters.getPortName());
				logger.error("{}",e);
				updateWorkerStatus("Poll Failed - " + getHHmm());
			} finally {
				if (connection != null && connection.isOpen())
					connection.close();
			}
		}

		if (status) {
			failIfInterrupted();
			String finalResponse = processRequiredRegister(
					response.getRegisters(), parameters);
			PollingDetailDTO dto = new PollingDetailDTO(
					parameters.getUniqueId(), System.currentTimeMillis(),
					finalResponse);
			int insert = DBConnectionManager.insertPollingDetails(dto);
			log(" insert poll response unit : {}, status : {}",
					parameters.getUniqueId(), insert);
		}

		return "Polling completed...";
	}

	private void updateWorkerStatus(String status) {
		JTable table = getTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Vector rowVector = model.getDataVector();

		// To update status of this worker
		try {
			Vector columnVector = (Vector) rowVector.get(parameters
					.getRowIndex());
			columnVector.set(3, status);
			synchronized (table) {
				model.fireTableDataChanged();
			}
		} catch (Exception e) {
			logger.error("{}",e);
		}
	}
}
