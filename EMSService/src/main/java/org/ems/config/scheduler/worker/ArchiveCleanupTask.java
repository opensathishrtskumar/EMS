package org.ems.config.scheduler.worker;

import java.util.concurrent.Callable;

import org.ems.config.listener.AppContextAware;
import org.ems.dao.PollingDetailsDAO;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.constants.QueryConstants;
import com.ems.util.Helper;

public class ArchiveCleanupTask implements Callable<Object> {

	private static final Logger logger = LoggerFactory.getLogger(MonthlyBackupTask.class);

	@Override
	public Object call() throws Exception {
		logger.info("Processing archive clean up task...");

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

		try {
			//Keep last 5 month data 
			LocalDate date = LocalDate.now().plusMonths(-5).withDayOfMonth(1);
			long timeStamp = Helper.getStartOfDay(date.toDate().getTime());
			
			String path = dao.fetchVariable("SELECT @@GLOBAL.secure_file_priv AS path");
			String backUpPath = path + System.currentTimeMillis() + ".txt";
			backUpPath = backUpPath.replaceAll("\\\\", "/");
			
			logger.info(" mysql temp path : " + backUpPath);
			
			StringBuilder builder = new StringBuilder("SELECT deviceuniqueid,polledon,REPLACE(unitresponse, '\r\n', ':') INTO OUTFILE ");
			builder.append("'");
			builder.append(backUpPath);
			builder.append("'");
			builder.append(" FIELDS TERMINATED BY ',' LINES TERMINATED BY '#' FROM archive.pollingdetails WHERE  polledon > ");
			builder.append(timeStamp);
			
			dao.execute(builder.toString());
			
			int rowsDeleted = dao.executeQuery(QueryConstants.ARVHIVE_BACKUP_DELETE_QUERY,
					new Object[] { timeStamp });
			logger.info("Rows deleted from archive database is {}", rowsDeleted);
			
		} catch (Exception e) {
			logger.error("Error", e);
		}

		logger.info("Processed daily back task...");
		return "Archive clean up completed";
	}
}
