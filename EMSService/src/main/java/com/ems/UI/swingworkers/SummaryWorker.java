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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.SummaryReportDTO;
import com.ems.constants.EmsConstants;
import com.ems.constants.MessageConstants;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.util.EMSUtility;
import com.ems.util.ExcelUtils;
import com.ems.util.MemoryMappingParser;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SummaryWorker implements Callable<Object> {
	private static final Logger logger = LoggerFactory.getLogger(SummaryWorker.class);

	private DeviceDetailsDTO devices[];

	public SummaryWorker(DeviceDetailsDTO devices[]) {
		this.devices = devices;
	}

	@Override
	public Map<DeviceDetailsDTO, List<SummaryReportDTO>> call() throws Exception {

		Map<DeviceDetailsDTO, List<SummaryReportDTO>> finalResponse = new HashMap<>();

		if (devices != null) {

			logger.debug("Excel report sub worker created for devices : {}", Arrays.toString(devices));

			Connection connection = DBConnectionManager.getConnection();

			for (DeviceDetailsDTO device : devices) {

				if (device == null)
					continue;

				try {

					PreparedStatement ps = connection.prepareStatement(QueryConstants.NEW_SUMMARY_REPORT,
							ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					// archive
					ps.setLong(1, device.getUniqueId());
					ps.setLong(2, device.getStartTime());
					ps.setLong(3, device.getEndTime());

					// monthly
					ps.setLong(4, device.getUniqueId());
					ps.setLong(5, device.getStartTime());
					ps.setLong(6, device.getEndTime());

					// polling
					ps.setLong(7, device.getUniqueId());
					ps.setLong(8, device.getStartTime());
					ps.setLong(9, device.getEndTime());

					ps.setFetchSize(Integer.MIN_VALUE);

					ResultSet rs = ps.executeQuery();

					ExtendedSerialParameter serialDevice = EMSUtility.mapDeviceToSerialParam(device);
					Map<String, String> headers = ExcelUtils.createReportHeaderMap(serialDevice);

					Map<String, ArrayList<Object>> responseList = extractResultSet(rs, headers);

					ArrayList<SummaryReportDTO> deviceSummaryDetails = calculateSummary(responseList, headers);

					finalResponse.put(device, deviceSummaryDetails);

					logger.debug("No of records for device {} is {}", device.getUniqueId(), responseList.size());
				} catch (Exception e) {
					logger.error("Error : {}", e);
				}
			}
		}

		logger.info("Excel report sub worker completed...");
		return finalResponse;
	}

	private Map<String, ArrayList<Object>> extractResultSet(ResultSet rs, Map<String, String> headers)
			throws SQLException {

		Map<String, ArrayList<Object>> response = new HashMap<>();

		// Add all required keys like timestamp and memory mapping in the map
		for (Entry<String, String> entry : headers.entrySet()) {
			if (!entry.getValue().trim().equalsIgnoreCase(EmsConstants.NO_MAP)) {
				response.put(entry.getKey(), new ArrayList<>());// Add memory mapping
			}
		}

		response.remove("Polled on");
		ArrayList<Object> timeStamp = new ArrayList<>();

		while (rs.next()) {
			timeStamp.add(rs.getString("formatteddate"));

			Properties unitResponse = MemoryMappingParser.loadProperties(rs.getString("unitresponse"));

			for (Entry<String, ArrayList<Object>> entry : response.entrySet()) {
				ArrayList<Object> valueList = entry.getValue();
				valueList.add(Double.parseDouble(unitResponse.getProperty(entry.getKey())));
			}
		}

		response.put(MessageConstants.TIMESTAMP, timeStamp);

		return response;
	}

	private ArrayList<SummaryReportDTO> calculateSummary(Map<String, ArrayList<Object>> responseArray,
			Map<String, String> headers) {
		ArrayList<SummaryReportDTO> summary = new ArrayList<>();

		ArrayList<Object> timeStamp = responseArray.get(MessageConstants.TIMESTAMP);

		for (Entry<String, ArrayList<Object>> entry : responseArray.entrySet()) {
			if (!entry.getKey().equals(MessageConstants.TIMESTAMP)) {
				SummaryReportDTO stats = new SummaryReportDTO();

				stats.setMemoryAddress(entry.getKey());
				stats.setMemoryAddressName(headers.get(entry.getKey()));

				ArrayList<Object> array = (ArrayList<Object>) entry.getValue();
				stats.setTotalNumberOfObservation(array.size());

				stats = calculateData(stats, array);

				int timeSize = timeStamp.size();

				if (timeSize > stats.getMinIndex())
					stats.setMinimumTimeStamp(timeStamp.get(stats.getMinIndex()).toString());

				if (timeSize > stats.getMaxIndex())
					stats.setMaximumTimeStamp(timeStamp.get(stats.getMaxIndex()).toString());

				summary.add(stats);
			}
		}

		return summary;
	}

	private SummaryReportDTO calculateData(SummaryReportDTO summary, ArrayList<Object> array) {

		double num[] = new double[array.size()];
		double average = 0;
		int i = 0;
		double sum = 0;

		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		int minIndex = 0, maxIndex = 0;

		for (i = 0; i < array.size(); i++) {
			num[i] = (Double) array.get(i);
			sum = sum + num[i];

			if (num[i] != 0 && num[i] < min) {
				min = num[i];
				minIndex = i;
			}

			if (num[i] > max) {
				max = num[i];
				maxIndex = i;
			}
		}

		if (min == Double.MAX_VALUE)
			min = 0.0;

		if (max == Double.MIN_VALUE)
			max = 0.0;

		average = sum / num.length;

		summary.setMinimum(min);
		summary.setMaximum(max);
		summary.setMinIndex(minIndex);
		summary.setMaxIndex(maxIndex);
		summary.setAverage(average);

		// logger.trace(" min index : {} max index : {} min : {} max : {} sum : {} avg :
		// {} ",minIndex,maxIndex,min,max,sum,average);

		return summary;
	}
}
