package com.ems.scheduler;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Sathish
 *
 */
public class SchedulerConfigurer{
	
	private static org.quartz.Scheduler scheduler = null;
	
	static {
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void scheduleJob(Trigger trigger, JobDetail job){
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void shutDown(){
		if(scheduler != null)
			try {
				scheduler.shutdown(true);
			} catch (SchedulerException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	protected void finalize() throws Throwable {
		shutDown();
		super.finalize();
	}
}