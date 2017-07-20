package com.ems.constants;

public abstract class QueryConstants {
	public static final String INSERT_DEVICE = "insert into setup.devicedetails "
			+ "(unitid, devicealiasname, baudrate, wordlength, stopbit,parity,memorymapping,"
			+ "status,createdtime,modifiedtime,registermapping,port,method ) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final String UPDATE_DEVICE = "update setup.devicedetails set unitid = ?,"
			+ " devicealiasname = ?, baudrate = ?, wordlength = ?, stopbit = ?,"
			+ "parity = ?,memorymapping = ?,status = ?,modifiedtime = ?,registermapping = ?,port = ?,method = ? where deviceuniqueid = ?";

	public static final String SELECT_DEVICES = "select deviceuniqueid, unitid, devicealiasname, baudrate, "
			+ "wordlength, stopbit,parity,memorymapping,status,createdtime,modifiedtime,registermapping,method,port "
			+ "from setup.devicedetails  order by deviceuniqueid asc";

	public static final String SELECT_ENABLED_ENDEVICES = "select deviceuniqueid, unitid, devicealiasname, baudrate, "
			+ "wordlength, stopbit,parity,memorymapping,status,createdtime,modifiedtime,registermapping,method,port "
			+ "from setup.devicedetails where status = true order by deviceuniqueid asc";

	public static final String SELECT_DEVICE_BY_ID = "select deviceuniqueid, unitid, devicealiasname, baudrate, "
			+ "wordlength, stopbit,parity,memorymapping,status,createdtime,modifiedtime,registermapping,method,port "
			+ "from setup.devicedetails where deviceuniqueid = ?";

	public static final String INSERT_POLLING_DETAILS = "insert into polling.pollingdetails"
			+ "(deviceuniqueid, polledon, unitresponse) values(?,?,?)";

	public static final String RETRIEVE_DEVICE_STATE = new StringBuilder(
			"SELECT " + "p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i%p') "
					+ "AS formatteddate FROM polling.pollingdetails p "
					+ "WHERE p.deviceuniqueid = ? AND p.polledon >  ? AND p.polledon < ? ORDER BY p.polledon ASC ")
							.toString();

	public static final String RETRIEVE_DEVICE_STATE4CHART = new StringBuilder(
			"SELECT p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%h:%i%p') "
					+ "AS formatteddate,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d%m%y%h') AS hourformat FROM polling.pollingdetails p "
					+ "WHERE p.deviceuniqueid = ? AND p.polledon >  ? AND p.polledon < ? ORDER BY p.polledon ASC ")
							.toString();

	public static final String IN_PLACEHOLDER = "$IN";

	public static final String DASHBOARD_DEVICES = new StringBuilder(
			"SELECT deviceuniqueid, unitid, devicealiasname, baudrate,wordlength, "
					+ "stopbit,parity,memorymapping,status,createdtime,modifiedtime,registermapping,method,port FROM setup.devicedetails "
					+ "WHERE deviceuniqueid IN (" + IN_PLACEHOLDER + ") AND STATUS = TRUE ORDER BY deviceuniqueid ASC")
							.toString();

	public static final String CONFIGURED_DEVICE_COUNT = "SELECT COUNT(*) AS COUNT FROM SETUP.DEVICEDETAILS";

	public static final String GET_LATEST_POLLING_DETAIL = "SELECT deviceuniqueid, DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') AS polledon, unitresponse "
			+ "FROM polling.pollingdetails WHERE deviceuniqueid=? ORDER BY polledon DESC LIMIT 1";
}
