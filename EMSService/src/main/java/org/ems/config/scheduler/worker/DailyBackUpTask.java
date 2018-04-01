package org.ems.config.scheduler.worker;

import java.util.concurrent.Callable;

import org.ems.config.listener.AppContextAware;
import org.ems.dao.PollingDetailsDAO;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.constants.QueryConstants;
import com.ems.util.Helper;

public class DailyBackUpTask implements Callable<Object> {

	private static final Logger logger = LoggerFactory.getLogger(MonthlyBackupTask.class);

	@Override
	public Object call() throws Exception {
		logger.info("Processing daily back task...");

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

		try {
			LocalDate date = LocalDate.now().plusDays(-2);
			long timeStamp = Helper.getEndOfDay(date.toDate().getTime());

			int rowsBackedUp = dao.executeQuery(QueryConstants.DAILY_2_MONTHLY_BACKUP_QUERY,
					new Object[] { timeStamp });
			logger.info("Rows backed up is {}", rowsBackedUp);

			int rowsDeleted = dao.executeQuery(QueryConstants.DAILY_2_MONTHLY_BACKUP__DELETE_QUERY,
					new Object[] { timeStamp });
			logger.info("Rows deleted after backup is {}", rowsDeleted);
		} catch (Exception e) {
			logger.error("Error", e);
		}

		logger.info("Processed daily back task...");
		return "Daily back up completed";
	}
}
