package com.ems.db;

import static com.ems.constants.DBConstants.HOST;
import static com.ems.constants.DBConstants.PASSWORD;
import static com.ems.constants.DBConstants.PORT;
import static com.ems.constants.DBConstants.USERNAME;
import static com.ems.constants.QueryConstants.DASHBOARD_DEVICES;
import static com.ems.constants.QueryConstants.INSERT_DEVICE;
import static com.ems.constants.QueryConstants.IN_PLACEHOLDER;
import static com.ems.constants.QueryConstants.SELECT_DEVICES;
import static com.ems.constants.QueryConstants.UPDATE_DEVICE;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ems.util.EMSUtility;

public class DBConnectionManager {

	private static final Logger logger = LoggerFactory.getLogger(DBConnectionManager.class);
	private static final BasicDataSource source = new BasicDataSource();

	public static void closeConnections(Connection connection, Statement st, ResultSet rs) {

		try {
			if (rs != null)
				rs.close();

			if (st != null)
				st.close();

			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			logger.error("{}", e);
			logger.error("Error closing DB Connection : {}", e.getLocalizedMessage());
		}
	}

	public static boolean verifyConnection(Properties props) {

		boolean connection = false;

		if (props == null)
			return connection;
		Connection dbConnection = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConnection = DriverManager.getConnection(getConnectionURL(props), props.getProperty(USERNAME),
					props.getProperty(PASSWORD));
			connection = true;
		} catch (Exception e) {
			connection = false;
			logger.error("{}", e);
			logger.error("error in establishing connection : {}", e.getLocalizedMessage());
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
			source.setDefaultAutoCommit(true);
			source.setUrl(getConnectionURL(props));
			source.setDriverClassName("com.mysql.jdbc.Driver");
			source.setUsername(props.getProperty(USERNAME));
			source.setPassword(props.getProperty(PASSWORD));
			source.setInitialSize(5);
			source.setMaxTotal(70);
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
			logger.debug("Connection details Active : {} , Idle : {}", source.getNumActive(), source.getNumIdle());
		} catch (SQLException e) {
			logger.error("{}", e);
			logger.error("Connection creation exception : {}", e.getLocalizedMessage());
		}
		props = null;
		return connection;
	}

	public static List<Future<Object>> processDevices(List<DeviceDetailsDTO> list) throws Exception {

		List<Future<Object>> taskList = new ArrayList<Future<Object>>();

		/*list.stream().filter(device -> device.getUniqueId() == 0).collect(Collectors.toList());
		list.stream().filter(device -> device.getUniqueId() != 0).collect(Collectors.toList());*/
		
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
		PreparedStatement statement = connection.prepareStatement(INSERT_DEVICE, Statement.RETURN_GENERATED_KEYS);
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
		statement.setString(11, device.getRegisterMapping());
		statement.setString(12, device.getPort());
		statement.setString(13, device.getMethod());

		int insert = 0;
		try {
			insert = statement.executeUpdate();
			logger.debug("Device added status : {}", insert);
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error(" device insertion failed : {}", device);
		}

		closeConnections(connection, statement, null);

		return insert;
	}

	public static int updateDevice(DeviceDetailsDTO device) throws SQLException {
		Properties props = TempDataManager.retrieveDBConfig();
		Connection connection = DBConnectionManager.getConnections(props);
		PreparedStatement statement = connection.prepareStatement(UPDATE_DEVICE);
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
		statement.setString(10, device.getRegisterMapping());
		statement.setString(11, device.getPort());
		statement.setString(12, device.getMethod());
		statement.setLong(13, device.getUniqueId());

		int update = 0;

		try {
			update = statement.executeUpdate();
			logger.debug("Device updation status : {}", update);
		} catch (Exception e) {
			logger.error("{}", e);
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
			logger.error("{}", e);
			logger.error(" Failed to load devices : {}", e.getLocalizedMessage());
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
				details = resultToDeviceDetail(resultSet);
				details.setRowIndex(rowIndex++);
				logger.trace(" Device from DB : {}", details);
			}
		} catch (SQLException e) {
			logger.error("{}", e);
			logger.error(" Failed to load devices : {}", e.getLocalizedMessage());
		} finally {
			closeConnections(connection, statement, resultSet);
		}
		return details;
	}

	public static List<DeviceDetailsDTO> mapResultToDeviceDetail(ResultSet resultSet) {
		List<DeviceDetailsDTO> list = new ArrayList<DeviceDetailsDTO>();
		logger.debug("Device detail mapping requested");
		try {

			int rowIndex = 0;
			while (resultSet.next()) {
				DeviceDetailsDTO details = resultToDeviceDetail(resultSet);
				details.setRowIndex(rowIndex++);
				logger.trace(" Device from DB : {}", details);
				list.add(details);
			}
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Device detail mapping error : {}", e.getLocalizedMessage());
		}
		return list;
	}

	public static int insertPollingDetails(PollingDetailDTO detailDTO) {
		Connection connection = getConnection();
		PreparedStatement stmt = null;
		int insert = 0;
		try {
			stmt = connection.prepareStatement(QueryConstants.INSERT_POLLING_DETAILS);

			stmt.setLong(1, detailDTO.getDeviceuniqueid());
			stmt.setLong(2, detailDTO.getPolledon());
			stmt.setString(3, detailDTO.getUnitresponse());

			insert = stmt.executeUpdate();
			logger.debug("polling data insertion status : {}", insert);
		} catch (Exception e) {
			logger.error("polling data insertion failed : {}", e.getLocalizedMessage());
			logger.error("{}", e);
		} finally {
			closeConnections(connection, stmt, null);
		}

		return insert;
	}

	public static String nQueryParam(int paramCount) {
		StringBuilder builder = new StringBuilder();
		paramCount = paramCount * 2 - 1;
		for (int i = 0; i < paramCount; i++)
			builder.append((i & 1) == 0 ? '?' : ',');
		return builder.toString();
	}

	public static String getDashBoardInclauseQuery(String[] array) {
		if (array == null || array.length == 0) {
			logger.info("Dashboard devices is null or empty ");
			return DASHBOARD_DEVICES.replace(IN_PLACEHOLDER, "0");
		}

		return DASHBOARD_DEVICES.replace(IN_PLACEHOLDER, nQueryParam(array.length));
	}

	public static int getConfiguredDeviceCount() {
		int count = 0;

		try (Connection con = getConnection();
				Statement stmt = con.createStatement();
				ResultSet result = stmt.executeQuery(QueryConstants.CONFIGURED_DEVICE_COUNT);) {
			if (result.next())
				count = result.getInt("COUNT");
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed to get available device count : {}", e.getLocalizedMessage());
		} finally {

		}
		return count;
	}

	private static DeviceDetailsDTO resultToDeviceDetail(ResultSet resultSet) throws SQLException {
		DeviceDetailsDTO details = new DeviceDetailsDTO();

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

	public static List<PollingDetailDTO> fetchPollingDetails(String query, Object[] params) {
		List<PollingDetailDTO> list = new ArrayList<PollingDetailDTO>();

		Connection connection = getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(query);
			int index = 1;
			for (Object param : params)
				ps.setObject(index++, param);

			logger.debug("executing query {} with param {}", query, Arrays.toString(params));
			rs = ps.executeQuery();

			list = mapPollingDetails(rs, list);
			logger.trace("Polling response : {}", list);
		} catch (SQLException e) {
			logger.error("{}", e);
		} finally {
			closeConnections(connection, ps, rs);
		}

		return list;
	}

	private static List<PollingDetailDTO> mapPollingDetails(ResultSet rs, List<PollingDetailDTO> list)
			throws SQLException {

		if (rs != null) {
			while (rs.next()) {
				PollingDetailDTO dto = new PollingDetailDTO();
				dto.setFormattedDate(rs.getString("polledon"));
				dto.setUnitresponse(rs.getString("unitresponse"));
				dto.setDeviceuniqueid(rs.getLong("deviceuniqueid"));
				list.add(dto);
			}
		}

		return list;
	}
	
	public static int insertRecentPolling(PollingDetailDTO dto){
		Connection connection = getConnection();
		PreparedStatement stmt = null;
		int insert = 0;
		try {
			stmt = connection.prepareStatement(QueryConstants.RECENT_POLL_INSERT);
			stmt.setLong(1, dto.getDeviceuniqueid());
			stmt.setLong(2, dto.getPolledon());
			stmt.setString(3, dto.getUnitresponse());
			stmt.setBoolean(4, dto.isStatus());

			insert = stmt.executeUpdate();
			logger.debug("polling data insertion status : {}", insert);
		} catch (Exception e) {
			logger.error("polling data insertion failed : {}", e.getLocalizedMessage());
			logger.error("{}", e);
		} finally {
			closeConnections(connection, stmt, null);
		}
		return insert;
	}
	
	public static int updateRecentPolling(PollingDetailDTO dto){
		Connection connection = getConnection();
		PreparedStatement stmt = null;
		int insert = 0;
		try {
			stmt = connection.prepareStatement(QueryConstants.RECENT_POLL_UPDATE);
			stmt.setLong(1, dto.getPolledon());
			stmt.setString(2, dto.getUnitresponse());
			stmt.setBoolean(3, dto.isStatus());
			stmt.setLong(4, dto.getDeviceuniqueid());

			insert = stmt.executeUpdate();
			logger.debug("polling data insertion status : {}", insert);
		} catch (Exception e) {
			logger.error("polling data insertion failed : {}", e.getLocalizedMessage());
			logger.error("{}", e);
		} finally {
			closeConnections(connection, stmt, null);
		}
		return insert;
	}

	public static List<PollingDetailDTO> fetchRecentPollingDetails(long deviceUniqueid) {
		List<PollingDetailDTO> list = new ArrayList<PollingDetailDTO>();

		Connection connection = getConnection();
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(QueryConstants.RECENT_POLL_SELECT);
			ps.setObject(1, deviceUniqueid);

			rs = ps.executeQuery();

			while (rs.next()) {
				PollingDetailDTO dto = new PollingDetailDTO();
				dto.setFormattedDate(rs.getString("polledon"));
				dto.setUnitresponse(rs.getString("unitresponse"));
				dto.setDeviceuniqueid(deviceUniqueid);
				dto.setStatus(rs.getBoolean("status"));
				list.add(dto);
			}
			
			logger.trace("Polling response : {}", list);
		} catch (SQLException e) {
			logger.error("{}", e);
		} finally {
			closeConnections(connection, ps, rs);
		}

		return list;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (source != null)
			source.close();

		super.finalize();
	}
}
