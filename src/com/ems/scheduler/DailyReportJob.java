package com.ems.scheduler;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DailyReportJob implements Job {
	public void execute(JobExecutionContext context) throws JobExecutionException {
		
		//TODO : code to create daily report and send email
		
		System.out.println("Daily Reort Quartz!");
	}
}