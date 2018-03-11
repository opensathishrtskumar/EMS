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

	/*public static final String GET_LATEST_POLLING_DETAIL = "SELECT deviceuniqueid, DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') AS polledon, unitresponse "
			+ "FROM polling.pollingdetails WHERE deviceuniqueid=? ORDER BY polledon DESC LIMIT 1";*/
	
	public static final String GET_LATEST_POLLING_DETAIL = "SELECT p.deviceuniqueid,DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') AS polledon,"
			+ " unitresponse FROM polling.pollingdetails p,(SELECT MAX(polledon) AS pon,deviceuniqueid "
			+ "FROM polling.pollingdetails WHERE deviceuniqueid=?) AS d "
			+ "WHERE d.deviceuniqueid=p.deviceuniqueid AND p.polledon=d.pon ORDER BY polledon DESC LIMIT 1";
	
	public static final String DAILY_CUMULATIVE_REPORT = "SELECT temp.timeformat, temp.polledon,temp.unitresponse FROM "
			+ "(SELECT DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%k') timeformat,polledon, unitresponse "
			+ "FROM polling.pollingdetails WHERE  deviceuniqueid=? AND polledon "
			+ "BETWEEN ? AND ? ORDER BY timeformat DESC) temp GROUP BY timeformat";
	
	
	public static final String RECENT_POLL_UPDATE = "UPDATE polling.recentpoll SET polledon = ?,  "
			+ "unitresponse = ?,  status = ? WHERE 	deviceuniqueid = ? ";
	
	public static final String RECENT_POLL_INSERT = "INSERT INTO polling.recentpoll (deviceuniqueid, polledon,unitresponse,status) VALUES (?,?,?,?)";
	
	public static final String RECENT_POLL_SELECT = "SELECT deviceuniqueid,polledon,unitresponse,status	FROM polling.recentpoll where deviceuniqueid = ?";
	
	
	public static final String RETRIEVE_ALL_DEVICE_STATE = new StringBuilder(
			"SELECT DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') "
					+ "AS formatteddate, unitresponse FROM polling.pollingdetails "
					+ "WHERE deviceuniqueid = ? AND polledon >  ? AND polledon < ? " 
					+ "UNION "
					+ "SELECT DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') " 
					+ "AS formatteddate, unitresponse FROM monthly.pollingdetails "
					+ "WHERE deviceuniqueid = ? AND polledon >  ? AND polledon < ? ")
			.toString();

	
	public static final String FAILED_DEVICES = "SELECT p.deviceuniqueid,s.devicealiasname,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i:%s %p') as ftime,"
			+ "p.unitresponse,s.port FROM setup.devicedetails s, polling.recentpoll p "
			+ "WHERE  s.status=1 AND s.deviceuniqueid=p.deviceuniqueid AND p.status=0";
	
	//Fetch record from all three schma and combine
	public static final String NEW_EXCEL_REPORT = "SELECT * FROM (SELECT p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i%p') AS"
			+ " formatteddate FROM archive.pollingdetails p WHERE p.deviceuniqueid = ? AND p.polledon >  ? AND p.polledon < ? AND  "
			+ "DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%k%i') % ? = 0 ORDER BY p.polledon DESC) AS X UNION ALL SELECT * FROM "
			+ "(SELECT p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i%p') AS formatteddate	"
			+ "FROM monthly.pollingdetails p WHERE p.deviceuniqueid = ? AND p.polledon >  ? AND p.polledon < ? AND  "
			+ "DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%k%i') % ? = 0 ORDER BY p.polledon DESC) AS Y UNION ALL "
			+ "SELECT * FROM (SELECT a.unitresponse,DATE_FORMAT(FROM_UNIXTIME(a.polledon/1000),'%d-%b-%y %h:%i%p') AS "
			+ "formatteddate FROM polling.pollingdetails a  WHERE a.deviceuniqueid = ? AND a.polledon >  ? AND a.polledon < ? "
			+ "AND  DATE_FORMAT(FROM_UNIXTIME(a.polledon/1000),'%k%i') % ? = 0  ORDER BY a.polledon DESC) AS Z";
	
	// Fetch record from all three schma and combine for summary ONLY ONE DAY DATA ALLOWED
	public static final String NEW_SUMMARY_REPORT = "SELECT p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i:%s%p') "
			+ "AS formatteddate FROM archive.pollingdetails p WHERE p.deviceuniqueid = ? AND p.polledon BETWEEN ? AND ? UNION ALL SELECT "
			+ "p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i:%s%p') AS formatteddate FROM monthly.pollingdetails p "
			+ " WHERE p.deviceuniqueid = ? AND p.polledon BETWEEN ? AND ?  UNION ALL SELECT p.unitresponse,DATE_FORMAT(FROM_UNIXTIME(p.polledon/1000),'%d-%b-%y %h:%i:%s%p') "
			+ "AS formatteddate	 FROM polling.pollingdetails p  WHERE p.deviceuniqueid = ? AND p.polledon BETWEEN ? AND ? ";

	
	public static final String GET_SINGLE_PARAM_DEVICES = "SELECT s.deviceuniqueid,s.devicealiasname,s.memorymapping FROM setup.devicedetails s WHERE s.status=TRUE AND s.memorymapping LIKE ? ORDER BY s.deviceuniqueid ASC";
}
