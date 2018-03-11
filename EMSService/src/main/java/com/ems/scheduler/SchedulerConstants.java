package com.ems.scheduler;

/**
 * @author Sathish Kumar
 *
 */
public abstract class SchedulerConstants {
	
	public static final String DAILY_REPORT_CRON = "0 0 9 * * ? *";//Fire at 9AM every day
	public static final String CUMULATIVE_REPORT_CRON = "0 20 9 * * ? *";//Fire at 9AM every day
	
	public static final String FORWARD_KW = "WH";
	
	public static final int LIVECHAR_REFRESH_FREQUENCY = 15;
}
