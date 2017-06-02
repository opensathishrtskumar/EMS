package com.ems.db;

import static com.ems.constants.DBConstants.HOST;
import static com.ems.constants.DBConstants.PASSWORD;
import static com.ems.constants.DBConstants.PORT;
import static com.ems.constants.DBConstants.USERNAME;
import static com.ems.constants.LimitConstants.DEFAULT_COMPORT;
import static com.ems.constants.MessageConstants.DEFAULTPORT_KEY;
import static com.ems.constants.QueryConstants.INSERT_DEVICE;
import static com.ems.constants.QueryConstants.SELECT_DEVICES;
import static com.ems.constants.QueryConstants.*;

import static com.ems.tmp.datamngr.TempDataManager.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ManageDeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.UI.swingworkers.ManageDeviceTask;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.constants.QueryConstants;
import com.ems.tmp.datamngr.TempDataManager;

public class DBConnectionManager {

	private static final Logger logger = LoggerFactory
			.getLogger(DBConnectionManager.class);
	private static final BasicDataSource source = new BasicDataSource();

	public static void closeConnections(Connection connection, Statement st,
			ResultSet rs) {

		try {
			if (rs != null)
				rs.close();

			if (st != null)
				st.close();

			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			logger.error("{}",e);
			logger.error("Error closing DB Connection : {}",
					e.getLocalizedMessage());
		}
	}

	public static boolean verifyConnection(Properties props) {

		boolean connection = false;

		if (props == null)
			return connection;
		Connection dbConnection = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConnection = DriverManager.getConnection(getConnectionURL(props),
					props.getProperty(USERNAME), props.getProperty(PASSWORD));
			connection = true;
		} catch (Exception e) {
			connection = false;
			logger.error("{}",e);
			logger.error("error in establishing connection : {}",
					e.getLocalizedMessage());
		} finally {
			closeConnections(dbConnection, null, null);
		}

		return connection;
	}

	/* jdbc:mysql://localhost:3306/?useSSL=false */
	private static String getConnectionURL(Properties props) {
		StringBuilder builder = new StringBuilder();
		builder.append("jdbc:mysql://");
		builder.append(props.getProperty(HOST));
		builder.append(":");
		builder.append(props.getProperty(PORT));
		builder.append("/?useSSL=false");
		logger.debug("connection url : {}", builder.toString());
		return builder.toString();
	}

	/**
	 * 
	 */
	public static synchronized Connection getConnections(Properties props) {
		// Initialize at very first time
		if (source.getInitialSize() == 0) {
			logger.debug("Connection initialized...");
			source.setUrl(getConnectionURL(props));
			source.setDriverClassName("com.mysql.jdbc.Driver");
			source.setUsername(props.getProperty(USERNAME));
			source.setPassword(props.getProperty(PASSWORD));
			source.setInitialSize(1);
			source.setMaxTotal(15);
			source.setMaxIdle(10);
			source.setMinIdle(5);
			source.setValidationQuery("select 1");
			source.setTestOnBorrow(true);
			source.setMinEvictableIdleTimeMillis(-1);
			source.setRemoveAbandonedOnBorrow(true);
			source.setRemoveAbandonedTimeout(10);
			source.setTimeBetweenEvictionRunsMillis(2000);
		}
		Connection connection = null;
		try {
			connection = source.getConnection();
			connection.setAutoCommit(true);
			logger.debug("Connection details Active : {} , Idle : {}",
					source.getNumActive(), source.getNumIdle());
		} catch (SQLException e) {
			logger.error("{}",e);
			logger.error("Connection creation exception : {}",
					e.getLocalizedMessage());
		}
		props = null;
		return connection;
	}

	public static List<Future<Object>> processDevices(
			List<DeviceDetailsDTO> list) throws Exception {

		List<Future<Object>> taskList = new ArrayList<Future<Object>>();

		for (final DeviceDetailsDTO device : list) {
			ManageDeviceTask task = new ManageDeviceTask(device);
			Future<Object> future = ConcurrencyUtils.execute(task);
			taskList.add(future);
		}

		return taskList;
	}

	public static Connection getConnection() {
		Properties props = TempDataManager.retrieveDBConfig();
		Connection connection = DBConnectionManager.getConnections(props);
		return connection;
	}

	public static int insertDevice(DeviceDetailsDTO device) throws SQLException {
		Properties props = TempDataManager.retrieveDBConfig();
		Connection connection = DBConnectionManager.getConnections(props);
		PreparedStatement statement = connection.prepareStatement(
				INSERT_DEVICE, Statement.RETURN_GENERATED_KEYS);
		long time = System.currentTimeMillis();

		statement.setInt(1, device.getDeviceId());
		statement.setString(2, device.getDeviceName());
		statement.setInt(3, device.getBaudRate());
		statement.setInt(4, device.getWordLength());
		statement.setInt(5, device.getStopbit());
		statement.setString(6, device.getParity());
		statement.setString(7, device.getMemoryMapping());
		statement.setBoolean(8, new Boolean(device.getEnabled()));
		statement.setLong(9, time);
		statement.setLong(10, time);

		int insert = 0;
		try {
			insert = statement.executeUpdate();
			logger.debug("Device added status : {}", insert);
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error(" device insertion failed : {}", device);
		}

		closeConnections(connection, statement, null);

		return insert;
	}

	public static int updateDevice(DeviceDetailsDTO device) throws SQLException {
		Properties props = TempDataManager.retrieveDBConfig();
		Connection connection = DBConnectionManager.getConnections(props);
		PreparedStatement statement = connection
				.prepareStatement(UPDATE_DEVICE);
		long time = System.currentTimeMillis();
		statement.setInt(1, device.getDeviceId());
		statement.setString(2, device.getDeviceName());
		statement.setInt(3, device.getBaudRate());
		statement.setInt(4, device.getWordLength());
		statement.setInt(5, device.getStopbit());
		statement.setString(6, device.getParity());
		statement.setString(7, device.getMemoryMapping());
		statement.setBoolean(8, new Boolean(device.getEnabled()));
		statement.setLong(9, time);
		statement.setLong(10, device.getUniqueId());
		int update = 0;

		try {
			update = statement.executeUpdate();
			logger.debug("Device updation status : {}", update);
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Device updation failed");
		}
		closeConnections(connection, statement, null);

		return update;
	}

	public static ManageDeviceDetailsDTO getDeviceManagerDetails() {
		ManageDeviceDetailsDTO manageDeviceDetails = new ManageDeviceDetailsDTO();
		List<DeviceDetailsDTO> deviceList = getAvailableDevices(SELECT_DEVICES);
		manageDeviceDetails.setList(deviceList);
		manageDeviceDetails.setPollingDelay(String.valueOf(3));
		manageDeviceDetails.setPortName("");
		return manageDeviceDetails;
	}

	public static List<DeviceDetailsDTO> getAvailableDevices(String query) {
		List<DeviceDetailsDTO> list = null;
		Properties props = TempDataManager.retrieveDBConfig();
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = getConnections(props);
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			logger.debug("active Device selection query executed");
			list = mapResultToDeviceDetail(resultSet);
		} catch (SQLException e) {
			logger.error("{}",e);
			logger.error(" Failed to load devices : {}",
					e.getLocalizedMessage());
			list = new ArrayList<DeviceDetailsDTO>();
		} finally {
			closeConnections(connection, statement, resultSet);
		}
		
		return list;
	}
	
	public static DeviceDetailsDTO getDeviceById(long deviceUniqueId) {
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		DeviceDetailsDTO details = null;
		try {
			connection = getConnection();
			statement = connection.prepareStatement(QueryConstants.SELECT_DEVICE_BY_ID);
			statement.setLong(1, deviceUniqueId);
			resultSet = statement.executeQuery();
			logger.debug("Device selection query executed for ID : {}", deviceUniqueId);

			int rowIndex = 0;
			while (resultSet.next()) {
				details = new DeviceDetailsDTO();
				details.setRowIndex(rowIndex++);
				details.setUniqueId(resultSet.getLong("deviceuniqueid"));
				details.setDeviceId(resultSet.getInt("unitid"));
				details.setDeviceName(resultSet.getString("devicealiasname"));
				details.setBaudRate(resultSet.getInt("baudrate"));
				details.setWordLength(resultSet.getInt("wordlength"));
				details.setStopbit(resultSet.getInt("stopbit"));
				details.setParity(resultSet.getString("parity"));
				details.setMemoryMapping(resultSet.getString("memorymapping"));
				details.setEnabled(resultSet.getBoolean("status") ? "true"
						: "false");
				logger.trace(" Device from DB : {}", details);
			}
		} catch (SQLException e) {
			logger.error("{}",e);
			logger.error(" Failed to load devices : {}",
					e.getLocalizedMessage());
		} finally {
			closeConnections(connection, statement, resultSet);
		}
		return details;
	}
	
	public static List<DeviceDetailsDTO> mapResultToDeviceDetail(ResultSet resultSet){
		List<DeviceDetailsDTO> list = new ArrayList<DeviceDetailsDTO>();
		logger.debug("Device detail mapping requested");
		try {
			Properties props = TempDataManager.retrieveTempConfig(MAIN_CONFIG);
			String port = props.getProperty(DEFAULTPORT_KEY,DEFAULT_COMPORT);
			
			int rowIndex = 0;
			while (resultSet.next()) {
				DeviceDetailsDTO details = new DeviceDetailsDTO();
				details.setRowIndex(rowIndex++);
				//Set default Port to connect with
				details.setPort(port);
				details.setUniqueId(resultSet.getLong("deviceuniqueid"));
				details.setDeviceId(resultSet.getInt("unitid"));
				details.setDeviceName(resultSet.getString("devicealiasname"));
				details.setBaudRate(resultSet.getInt("baudrate"));
				details.setWordLength(resultSet.getInt("wordlength"));
				details.setStopbit(resultSet.getInt("stopbit"));
				details.setParity(resultSet.getString("parity"));
				details.setMemoryMapping(resultSet.getString("memorymapping"));
				details.setEnabled(resultSet.getBoolean("status") ? "true"
						: "false");
				logger.trace(" Device from DB : {}", details);
				list.add(details);
			}
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Device detail mapping error : {}", e.getLocalizedMessage());
		}
		return list;
	}
	
	public static int insertPollingDetails(PollingDetailDTO detailDTO) {
		Connection connection = getConnection();
		PreparedStatement stmt = null;
		int insert = 0;
		try {
			stmt = connection
					.prepareStatement(QueryConstants.INSERT_POLLING_DETAILS);

			stmt.setLong(1, detailDTO.getDeviceuniqueid());
			stmt.setLong(2, detailDTO.getPolledon());
			stmt.setString(3, detailDTO.getUnitresponse());

			insert = stmt.executeUpdate();
			logger.debug("polling data insertion status : {}", insert);
		} catch (Exception e) {
			logger.error("polling data insertion failed : {}",
					e.getLocalizedMessage());
			logger.error("{}",e);
		} finally {
			closeConnections(connection, stmt, null);
		}

		return insert;
	}
	
	public static String nQueryParam(int paramCount){
		StringBuilder builder = new StringBuilder();
		paramCount = paramCount * 2 - 1;
		for (int i = 0; i < paramCount; i++)
			builder.append((i & 1) == 0 ? '?' : ',');
		return builder.toString();
	}
	
	public static String getDashBoardInclauseQuery(String[] array){
		if(array == null || array.length == 0){
			logger.info("Dashboard devices is null or empty ");
			return DASHBOARD_DEVICES.replace(IN_PLACEHOLDER, "0");
		}
		
		return DASHBOARD_DEVICES.replace(IN_PLACEHOLDER, nQueryParam(array.length));
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (source != null)
			source.close();

		super.finalize();
	}
}
