package com.ems.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

/**
 * @author Sathish
 * 
 */
public abstract class SchedulerUtil {

	public static Trigger createTrigger(String triggerName, String groupName, String cronExpression) {
		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, groupName)
				.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();
		return trigger;
	}

	public static JobDetail createJob(String jobName, String groupName, Class<? extends Job> jobImplementation){
		JobDetail job = JobBuilder.newJob(jobImplementation)
				.withIdentity(jobName, groupName).build();
		return job;
	}
}
