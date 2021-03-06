package com.ems.UI.swingworkers;

import static com.ems.util.EMSUtility.getHHmm;
import static com.ems.util.EMSUtility.processRequiredRegister;

import java.util.Vector;
import java.util.concurrent.Callable;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.db.DBConnectionManager;
import com.ems.util.EMSUtility;

public class DBPollingWorker implements Callable<Object> {
	private static final Logger logger = LoggerFactory.getLogger(DBPollingWorker.class);

	private JTable table;
	private ExtendedSerialParameter parameters;

	public DBPollingWorker(ExtendedSerialParameter parameters) {
		this.parameters = parameters;
	}

	public JTable getTable() {
		return table;
	}

	public void setTable(JTable table) {
		this.table = table;
	}

	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException(" Stopping worker...");
		}
	}

	@Override
	public Object call() throws Exception {

		boolean status = true;
		
		if(parameters.isSplitJoin()){
			status = EMSUtility.splitJoinStatus(parameters.getSplitJoinDTO().getStatus());
			logger.trace("Split Join final response Status {}", status);
		} else {
			status = parameters.isStatus();
		}

		updateWorkerStatus(status);
		
		PollingDetailDTO dto = new PollingDetailDTO(parameters.getUniqueId(), System.currentTimeMillis(),
				null);
		dto.setStatus(status);
		
		if (status) {
			failIfInterrupted();
			String finalResponse = processRequiredRegister(parameters);
			dto.setUnitresponse(finalResponse);
			int insert = DBConnectionManager.insertPollingDetails(dto);
			logger.debug(" insert poll response unit : {}, status : {}", parameters.getUniqueId(), insert);
		}

		int count = DBConnectionManager.updateRecentPolling(dto);
		
		if(count == 0){
			DBConnectionManager.insertRecentPolling(dto);
		}
		
		return "Polling completed...";
	}

	@SuppressWarnings("unchecked")
	private void updateWorkerStatus(boolean status) {
		JTable table = getTable();
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		@SuppressWarnings("rawtypes")
		Vector rowVector = model.getDataVector();

		// To update status of this worker
		try {
			@SuppressWarnings("rawtypes")
			Vector columnVector = (Vector) rowVector.get(parameters.getRowIndex());
			
			if(!status){
				columnVector.set(3, "Poll Failed - " + getHHmm());
			} else {
				columnVector.set(3, "Poll Success - " + getHHmm());
			}
			
			model.fireTableDataChanged();
		} catch (Exception e) {
			logger.error("Polling Table mode fire event failed : {} Exception {}", e.getLocalizedMessage(), e);
		}
	}
	
	
}
