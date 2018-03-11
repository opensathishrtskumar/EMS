package com.ems.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		preProcessing(arg0);
		work(arg0);
		postProcessing(arg0);
	}

	protected abstract void preProcessing(JobExecutionContext arg0) throws JobExecutionException;

	protected abstract void work(JobExecutionContext arg0) throws JobExecutionException;

	protected abstract void postProcessing(JobExecutionContext arg0) throws JobExecutionException;
}
