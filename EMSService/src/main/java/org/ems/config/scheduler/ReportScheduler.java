package org.ems.config.scheduler;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ems.scheduler.AbstractJob;
import com.ems.scheduler.CumulativeReportJob;
import com.ems.scheduler.DailyReportJob;
import com.ems.scheduler.FailedDevicesJob;
import com.ems.scheduler.FinalReportJob;
import com.ems.scheduler.MonthlySummaryReportJob;

@Configuration
@EnableAsync
@EnableScheduling
public class ReportScheduler {

	private static final Logger logger = LoggerFactory.getLogger(ReportScheduler.class);

	@Scheduled(cron = "${faileddevicescron}")
	public void failedDevices() {
		logger.debug("Failed Devices report");
		try {
			AbstractJob job = new FailedDevicesJob();
			job.execute(null);
		} catch (JobExecutionException e) {
			logger.error("error creating faild devices report", e);
		}
		logger.debug("Failed Devices report ending...");
	}

	@Scheduled(cron = "${dailyreportcron}")
	public void dailyReportTask() {
		logger.debug("daily report cron triggering...");
		try {
			AbstractJob job = new DailyReportJob();
			job.execute(null);
		} catch (JobExecutionException e) {
			logger.error("error creating daily report", e);
		}
		logger.debug("daily report cron ending...");
	}

	@Scheduled(cron = "${dailycumulativereportcron}")
	public void monthlyCumulativeReportTask() {
		logger.debug("daily cumulative report cron triggering...");
		try {
			AbstractJob job = new CumulativeReportJob();
			job.execute(null);
		} catch (JobExecutionException e) {
			logger.error("error creating daily report", e);
		}
		logger.debug("daily cumulative report cron ending...");
	}

	// Final Report only for ISUZU
	@Scheduled(cron = "${finalreportcron}")
	public void finalReportTask() {
		logger.trace("final cumulative report cron triggering...");
		try {
			AbstractJob job = new FinalReportJob();
			job.execute(null);
		} catch (JobExecutionException e) {
			logger.error("error creating final cumulative report", e);
		}
		logger.trace("final cumulative report cron ending...");
	}

	// Monthly Summary Report only for ISUZU
	@Scheduled(cron = "${monthlysummaryreportcron}")
	public void monthlySummaryReportTask() {
		logger.trace("monthly summary report cron triggering...");
		try {
			AbstractJob job = new MonthlySummaryReportJob();
			job.execute(null);
		} catch (JobExecutionException e) {
			logger.error("error creating monthly summary report", e);
		}
		logger.trace("monthly summary report cron ending...");
	}
}
