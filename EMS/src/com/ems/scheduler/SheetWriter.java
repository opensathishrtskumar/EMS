package com.ems.scheduler;

import static com.ems.util.EMSUtility.getOrderedProperties;
import static com.ems.util.ExcelUtils.writeResultToSheet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.db.DBConnectionManager;
import com.ems.util.ExcelUtils;

public class SheetWriter implements Callable<Object>{
		
		private static final Logger logger = LoggerFactory.getLogger(SheetWriter.class);
		
		private long reportStartTime;
		private long reportEndTime;
		private ExtendedSerialParameter device;
		private HSSFSheet sheet;
		private String query;
		private Object[] params;
		
		public SheetWriter(ExtendedSerialParameter device, HSSFSheet sheet, String query, Object[] params) {
			this.device = device;
			this.sheet = sheet;
			this.query = query;
			this.params = params;
		}
		
		@Override
		public HSSFSheet call() throws Exception {
			
			logger.trace("trying to get memory details for daily report");
			
			Connection connection = DBConnectionManager.getConnection();
			PreparedStatement ps = null;
			ResultSet rs = null;
			
			try {
				ps = connection.prepareStatement(query);
				int index = 1;
				for(Object param : params){
					ps.setObject(index++, param);
				}
				
				rs = ps.executeQuery();

				// Keep the order of properties
				Map<String, String> memoryMap = getOrderedProperties(device);
				// All the values becomes header of column
				Map<String, String> headers = new LinkedHashMap<>();
				headers.put("Polled on", "Time");
				headers.putAll(memoryMap);

				// Firt row reserved for headers so start with 1
				for (int rowIndex = 1;rs.next(); rowIndex++) {
					HSSFRow row = sheet.createRow(rowIndex);
					ExcelUtils.writeReadingsRow(row, rs.getString("formatteddate"), 
							rs.getString("unitresponse"), memoryMap);
				}

				this.sheet = writeResultToSheet(device, rs, sheet);

			} catch (Exception e) {
				logger.error("error creating sheet for daily report  for device {}  {}", this.device, e);
			} finally {
				DBConnectionManager.closeConnections(connection, ps, rs);
			}
			
			logger.trace("sheet creation completed for daily report");
			
			return this.sheet;
		}

		public long getReportStartTime() {
			return reportStartTime;
		}

		public SheetWriter setReportStartTime(long reportStartTime) {
			this.reportStartTime = reportStartTime;
			return this;
		}

		public long getReportEndTime() {
			return reportEndTime;
		}

		public SheetWriter setReportEndTime(long reportEndTime) {
			this.reportEndTime = reportEndTime;
			return this;
		}
	}