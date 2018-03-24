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

	/*
	 * public static final String GET_LATEST_POLLING_DETAIL =
	 * "SELECT deviceuniqueid, DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') AS polledon, unitresponse "
	 * +
	 * "FROM polling.pollingdetails WHERE deviceuniqueid=? ORDER BY polledon DESC LIMIT 1"
	 * ;
	 */

	public static final String GET_LATEST_POLLING_DETAIL = "SELECT p.deviceuniqueid,DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d-%b-%y %h:%i%p') AS polledon,"
			+ " unitresponse FROM polling.pollingdetails p,(SELECT MAX(polledon) AS pon,deviceuniqueid "
			+ "FROM polling.pollingdetails WHERE deviceuniqueid=?) AS d "
			+ "WHERE d.deviceuniqueid=p.deviceuniqueid AND p.polledon=d.pon ORDER BY polledon DESC LIMIT 1";

	public static final String DAILY_CUMULATIVE_REPORT = "SELECT temp.timeformat, temp.polledon,temp.unitresponse FROM "
			+ "(SELECT DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d%k')  timeformat,polledon, unitresponse "
			+ "FROM polling.pollingdetails WHERE  deviceuniqueid=? AND polledon BETWEEN ? AND ? GROUP BY timeformat) "
			+ "temp ORDER BY CAST(temp.polledon AS UNSIGNED) ASC";

	public static final String RECENT_POLL_UPDATE = "UPDATE polling.recentpoll SET polledon = ?,  "
			+ "unitresponse = ?,  status = ? WHERE 	deviceuniqueid = ? ";

	public static final String RECENT_POLL_INSERT = "INSERT INTO polling.recentpoll (deviceuniqueid, polledon,unitresponse,status) VALUES (?,?,?,?)";

	public static final String RECENT_POLL_SELECT = "SELECT deviceuniqueid,polledon,unitresponse,status	FROM polling.recentpoll where deviceuniqueid = ?";

	// backup Scheduler query
	public static final String DAILY_2_MONTHLY_BACKUP_QUERY = "INSERT INTO monthly.pollingdetails (deviceuniqueid,polledon,unitresponse) "
			+ "SELECT s.deviceuniqueid,s.polledon,s.unitresponse FROM polling.pollingdetails s WHERE s.polledon <= ?";

	public static final String DAILY_2_MONTHLY_BACKUP__DELETE_QUERY = "DELETE FROM polling.pollingdetails WHERE polledon <= ?";

	public static final String MONTHLY_2_ARVHIVE_BACKUP_QUERY = "INSERT INTO archive.pollingdetails (deviceuniqueid,polledon,unitresponse) "
			+ "SELECT s.deviceuniqueid,s.polledon,s.unitresponse FROM monthly.pollingdetails s WHERE s.polledon < ?";

	public static final String MONTHLY_2_ARVHIVE_BACKUP_DELETE_QUERY = "DELETE FROM monthly.pollingdetails WHERE polledon < ?";

	public static final String ARVHIVE_BACKUP_DELETE_QUERY = "DELETE FROM archive.pollingdetails WHERE polledon < ?";

	public static final String FETCH_SETTINGS = "SELECT id,skey,svalue FROM setup.settings";

	public static final String BACKUP_ARCHIVE = "SELECT deviceuniqueid,polledon,REPLACE(unitresponse, '\r\n', ':') INTO OUTFILE ? FIELDS TERMINATED BY ',' "
			+ "LINES TERMINATED BY '#' FROM archive.pollingdetails WHERE  polledon > ?";

	// Query for Final Report begins
	public static final String MAIN_INCOMER_DAILY = "SELECT p.deviceuniqueid, p.polledon ,p.unitresponse FROM "
			+ "polling.pollingdetails p, "
			+ "(SELECT MIN(polledon) AS minimum, MAX(polledon) AS maximum, deviceuniqueid AS deviceid "
			+ "FROM polling.pollingdetails WHERE "
			+ "deviceuniqueid = (SELECT CAST(svalue AS UNSIGNED) FROM setup.settings WHERE skey=?) "
			+ "AND polledon BETWEEN ? AND ?) AS temp "
			+ "WHERE deviceuniqueid=temp.deviceid AND (p.polledon = temp.minimum  OR   p.polledon = temp.maximum) ORDER BY p.polledon ASC";

	public static final String FETCH_DEVICE_READINGS = "SELECT polledon,unitresponse  FROM polling.pollingdetails WHERE deviceuniqueid = ? AND polledon BETWEEN ? AND ? "
			+ " UNION ALL SELECT polledon,unitresponse  FROM monthly.pollingdetails WHERE deviceuniqueid = ? AND polledon BETWEEN ? AND ?";

	public static final String MONTHLY_CONSUMPTION = "SELECT polledon" + "	,unitresponse " + "	,deviceuniqueid"
			+ "	" + "	 FROM (SELECT p.polledon" + "	,p.unitresponse " + "	,p.deviceuniqueid" + "	FROM "
			+ "		polling.pollingdetails p,"
			+ "		(SELECT MIN(polledon) AS minimum, MAX(polledon) AS maximum, deviceuniqueid AS deviceid"
			+ "			FROM polling.pollingdetails " + "				WHERE "
			+ "					deviceuniqueid = (SELECT CAST(svalue AS UNSIGNED) FROM setup.settings WHERE skey=?)"
			+ "					AND polledon BETWEEN ? AND ?) AS temp" + "	WHERE "
			+ "		deviceuniqueid=temp.deviceid "
			+ "		AND (p.polledon = temp.minimum  OR   p.polledon = temp.maximum)" + " UNION ALL "
			+ "SELECT p.polledon" + "	,p.unitresponse " + "	,p.deviceuniqueid" + "	FROM "
			+ "		monthly.pollingdetails p,"
			+ "		(SELECT MIN(polledon) AS minimum, MAX(polledon) AS maximum, deviceuniqueid AS deviceid"
			+ "			FROM monthly.pollingdetails " + "				WHERE "
			+ "					deviceuniqueid = (SELECT CAST(svalue AS UNSIGNED) FROM setup.settings WHERE skey=?)"
			+ "					AND polledon BETWEEN ? AND ?) AS temp" + "	WHERE "
			+ "		deviceuniqueid=temp.deviceid "
			+ "		AND (p.polledon = temp.minimum  OR   p.polledon = temp.maximum)) AS res ORDER BY polledon DESC";

	// Query for Final Report ends
	
	public static final String DAILYSUMMARY_REPORT ="(SELECT p.deviceuniqueid, p.polledon ,p.unitresponse   \r\n" + 
			" FROM 	monthly.pollingdetails p, \r\n" + 
			"			(SELECT MIN(polledon) AS minimum, deviceuniqueid AS deviceid ,DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d%m%y') AS DT\r\n" + 
			"			FROM monthly.pollingdetails WHERE \r\n" + 
			"			deviceuniqueid = (SELECT CAST(svalue AS UNSIGNED) FROM setup.settings WHERE skey=?)\r\n" + 
			"			AND polledon BETWEEN ? AND ? GROUP BY DT )  AS temp  \r\n" + 
			"			WHERE deviceuniqueid=temp.deviceid AND (p.polledon = temp.minimum) ORDER BY DT  ASC)\r\n" + 
			"					UNION ALL\r\n" + 
			"			(SELECT p.deviceuniqueid, p.polledon ,p.unitresponse \r\n" + 
			" FROM 	polling.pollingdetails p, \r\n" + 
			"			(SELECT MIN(polledon) AS minimum, deviceuniqueid AS deviceid ,DATE_FORMAT(FROM_UNIXTIME(polledon/1000),'%d%m%y') AS DT\r\n" + 
			"			FROM polling.pollingdetails WHERE \r\n" + 
			"			deviceuniqueid = (SELECT CAST(svalue AS UNSIGNED) FROM setup.settings WHERE skey=?)\r\n" + 
			"			AND polledon BETWEEN ? AND ? GROUP BY DT ) AS temp \r\n" + 
			"			WHERE deviceuniqueid=temp.deviceid AND (p.polledon = temp.minimum) ORDER BY DT  ASC )";
	
	public static final String FAILED_DEVICES = "select d.devicealiasname from polling.recentpoll rp, setup.devicedetails d \r\n" + 
			"	where rp.status = false and d.deviceuniqueid = rp.deviceuniqueid";
}
