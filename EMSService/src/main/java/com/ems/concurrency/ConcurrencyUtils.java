package com.ems.concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConcurrencyUtils {
	private static final Logger logger = LoggerFactory.getLogger(ConcurrencyUtils.class);

	private static ThreadPoolExecutor workers = new ThreadPoolExecutor(30, 100, 5, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(100));

	// No task should be rejected
	static {
		workers.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public static ThreadPoolExecutor getWorkers() {
		return workers;
	}

	public static Future<Object> execute(Callable<Object> work) {
		Future<Object> result = workers.submit(work);
		logger.trace(" Task submitted for Concurrent processing...");
		return result;
	}

	@Override
	protected void finalize() throws Throwable {
		workers.shutdownNow();
		super.finalize();
	}
}
