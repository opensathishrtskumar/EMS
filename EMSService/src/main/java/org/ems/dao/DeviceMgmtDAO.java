package org.ems.dao;

import static com.ems.constants.QueryConstants.UPDATE_DEVICE;
import static com.ems.constants.QueryConstants.INSERT_DEVICE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.ems.model.DeviceForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.db.DBConnectionManager;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.EMSUtility;

@Repository
public class DeviceMgmtDAO {

	private static final Logger logger = LoggerFactory.getLogger(PollingDetailsDAO.class);

	private JdbcTemplate jdbcTemplate;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public Map<Long, String> getAllDeviceDetails(String query) {
		
		Map<Long, String> map = new HashMap<Long, String>();
		SqlRowSet rowSet =  this.jdbcTemplate.queryForRowSet(query);
		while(rowSet.next()) {
			map.put(rowSet.getLong("deviceuniqueid"), rowSet.getString("devicealiasname"));
		}
		return map;
	}
	
	public boolean removeDeviceId(String query, long deviceId) {
		logger.debug("delete device id...");
		if(this.jdbcTemplate.update(query, deviceId) > 0) {
			logger.debug("device id is successfully deleted...");
			return true;
		}
		logger.debug("device id is fail to delete...");
		return false;
	}
	
	public List<DeviceDetailsDTO> readDeviceDetailsById(String query, long deviceId) {
		
		return this.jdbcTemplate.query(query, new RowMapper<DeviceDetailsDTO>() {

			@Override
			public DeviceDetailsDTO mapRow(ResultSet resultSet, int rowIndex) throws SQLException {
				
				DeviceDetailsDTO details = new DeviceDetailsDTO();

				details.setRowIndex(rowIndex++);
				details.setUniqueId(resultSet.getLong("deviceuniqueid"));
				details.setDeviceId(resultSet.getInt("unitid"));
				details.setDeviceName(resultSet.getString("devicealiasname"));
				details.setBaudRate(resultSet.getInt("baudrate"));
				details.setWordLength(resultSet.getInt("wordlength"));
				details.setStopbit(resultSet.getInt("stopbit"));
				details.setParity(resultSet.getString("parity"));
				details.setMemoryMapping(resultSet.getString("memorymapping"));// register_assignment
				boolean splitJoin = EMSUtility.isSplitJoin(details.getMemoryMapping());
				details.setSplitJoin(splitJoin);
				details.setEnabled(resultSet.getBoolean("status") ? "true" : "false");
				details.setRegisterMapping(resultSet.getString("registermapping"));// MSRF/LSRF
				details.setPort(resultSet.getString("port"));
				details.setMethod(resultSet.getString("method"));
				return details;
			}
		}, new Object[] {deviceId});
	}
	
	public int updateDeviceDetailsById(DeviceForm device) throws SQLException {
		
		Connection connection = this.jdbcTemplate.getDataSource().getConnection();
		PreparedStatement statement = connection.prepareStatement(UPDATE_DEVICE);
		long time = System.currentTimeMillis();
		statement.setInt(1, device.getUnitId());
		statement.setString(2, device.getDeviceName());
		statement.setInt(3, device.getBaudRates());
		statement.setInt(4, device.getWordLength());
		statement.setInt(5, device.getStopBit());
		statement.setString(6, device.getParity());
		statement.setString(7, device.getMemoryMapping());
		statement.setBoolean(8, new Boolean(device.getEnabled()));
		statement.setLong(9, time);
		statement.setString(10, device.getRegMapping());
		statement.setString(11, device.getPort());
		statement.setString(12, device.getReadMethod());
		statement.setLong(13, device.getDeviceId());

		int update = 0;

		try {
			update = statement.executeUpdate();
			logger.debug("Device updation status : {}", update);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}", e);
			logger.error("Device updation failed");
		}
		return update;
	}
	
	public int saveDevice(DeviceForm device) throws SQLException {
		
		Connection connection = this.jdbcTemplate.getDataSource().getConnection();
		PreparedStatement statement = connection.prepareStatement(INSERT_DEVICE, Statement.RETURN_GENERATED_KEYS);
		long time = System.currentTimeMillis();

		statement.setInt(1, device.getUnitId());
		statement.setString(2, device.getDeviceName());
		statement.setInt(3, device.getBaudRates());
		statement.setInt(4, device.getWordLength());
		statement.setInt(5, device.getStopBit());
		statement.setString(6, device.getParity());
		statement.setString(7, device.getMemoryMapping());
		statement.setBoolean(8, new Boolean(device.getEnabled()));
		statement.setLong(9, time);
		statement.setLong(10, time);
		statement.setString(11, device.getRegMapping());
		statement.setString(12, device.getPort());
		statement.setString(13, device.getReadMethod());

		int insert = 0;
		try {
			insert = statement.executeUpdate();
			logger.debug("Device added status : {}", insert);
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error(" device insertion failed : {}", device);
		}
		return insert;
	}
	
}
