package org.ems.config.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ems.UI.swingworkers.GroupedDeviceWorker;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.response.handlers.PollingResponseHandler;

@Configuration
@EnableAsync
@EnableScheduling
public class PollingScheduler {

	private static final Logger logger = LoggerFactory.getLogger(PollingScheduler.class);

	private GroupedDeviceWorker worker;

	//@Scheduled(fixedDelay = 86400000, initialDelay = 5000)
	public void pollingScheduler() {
		logger.trace("Triggering polling to fetch Data...");

		if (this.worker == null) {
			this.worker = new GroupedDeviceWorker(null);
			this.worker.setResponseHandler(new PollingResponseHandler());
			logger.trace("Poller is started successfully....");
			ConcurrencyUtils.execute(this.worker);
		}

		logger.info("Polling triggered....");
	}
}
