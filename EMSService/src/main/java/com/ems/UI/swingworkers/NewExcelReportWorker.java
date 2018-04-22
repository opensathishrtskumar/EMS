package com.ems.UI.swingworkers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;

public class NewExcelReportWorker implements Callable<Object> {
	private static final Logger logger = LoggerFactory.getLogger(NewExcelReportWorker.class);

	private DeviceDetailsDTO devices[];

	public NewExcelReportWorker(DeviceDetailsDTO devices[]) {
		this.devices = devices;
	}

	@Override
	public Map<DeviceDetailsDTO, List<PollingDetailDTO>> call() throws Exception {
		Map<DeviceDetailsDTO, List<PollingDetailDTO>> finalResponse = new HashMap<>();

		if (devices != null) {
			logger.debug("Excel report sub worker created for devices : {}", Arrays.toString(devices));
			Connection connection = DBConnectionManager.getConnection();
			
			for (DeviceDetailsDTO device : devices) {

				if (device == null)
					continue;

				try {
					PreparedStatement ps = connection.prepareStatement(QueryConstants.NEW_EXCEL_REPORT,
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					// pollingdetails
					ps.setLong(1, device.getUniqueId());
					ps.setLong(2, device.getStartTime());
					ps.setLong(3, device.getEndTime());
					ps.setLong(4, device.getRecordCount());

					// monthly
					ps.setLong(5, device.getUniqueId());
					ps.setLong(6, device.getStartTime());
					ps.setLong(7, device.getEndTime());
					ps.setLong(8, device.getRecordCount());

					// archive
					ps.setLong(9, device.getUniqueId());
					ps.setLong(10, device.getStartTime());
					ps.setLong(11, device.getEndTime());
					ps.setLong(12, device.getRecordCount());

					ps.setFetchSize(Integer.MIN_VALUE);

					ResultSet rs = ps.executeQuery();

					List<PollingDetailDTO> responseList = extractResultSet(rs);
					logger.debug("No of records for device {} is {}", device.getUniqueId(), responseList.size());
					finalResponse.put(device, responseList);
					logger.trace("Retrieved data for device : {}", device);
				} catch (Exception e) {
					logger.error("Error : {}", e);
				}
			}
		}

		logger.info("Excel report sub worker completed...");
		return finalResponse;
	}

	private List<PollingDetailDTO> extractResultSet(ResultSet rs) throws SQLException {

		List<PollingDetailDTO> responseList = new ArrayList<>();

		while (rs.next()) {
			PollingDetailDTO record = new PollingDetailDTO();
			record.setFormattedDate(rs.getString("formatteddate"));
			record.setUnitresponse(rs.getString("unitresponse"));
			responseList.add(record);
		}

		return responseList;
	}
}
