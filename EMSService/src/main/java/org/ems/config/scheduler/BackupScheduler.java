package org.ems.config.scheduler;

import javax.annotation.PostConstruct;

import org.ems.config.scheduler.worker.ArchiveCleanupTask;
import org.ems.config.scheduler.worker.DailyBackUpTask;
import org.ems.config.scheduler.worker.MonthlyBackupTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ems.concurrency.ConcurrencyUtils;

@Component
@EnableAsync
@EnableScheduling
public class BackupScheduler {

	private static final Logger logger = LoggerFactory.getLogger(BackupScheduler.class);

	@Scheduled(cron = "${dailybackupcron}")
	// Daily table to monthly table backup
	public void backUpDaily() {
		logger.debug("daily backup initializing...");
		ConcurrencyUtils.execute(new DailyBackUpTask());
		logger.debug("daily backup initialized...");
	}

	@Scheduled(cron = "${monthklybackupcron}")
	// Monthly table to archive table backup
	public void backUpMonthly() {
		logger.debug("monthly backup initializing...");
		ConcurrencyUtils.execute(new MonthlyBackupTask());
		logger.debug("monthly backup initialized...");
	}

	@Scheduled(cron="${archievebackupcron}")
	public void archieveBackup() {
		logger.debug("archive backup initializing...");
		ConcurrencyUtils.execute(new ArchiveCleanupTask());
		logger.debug("archive backup initialized...");
	}
	
	@PostConstruct
	public void init() {
		logger.info("BackUp Scheduler Initialized");
	}
}
