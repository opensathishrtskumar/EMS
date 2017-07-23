package com.ems.scheduler;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.AttachmentDTO;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.EmailDTO;

public class CumulativeReportJob extends AbstractJob {
	private static final Logger logger = LoggerFactory.getLogger(CumulativeReportJob.class);
	
	private EmailDTO emailDTO = null;
	private List<DeviceDetailsDTO> devices = null;
	private List<AttachmentDTO> attachments = null;
	
	public CumulativeReportJob() {
		
	}
	
	@Override
	protected void preProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing required parameters...");
		//Initialize here required values
		logger.debug("Initializing required parameters completed...");
	}

	@Override
	protected void work(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Initializing report preparation...");
		//Fetch report to send
		
		
		
		logger.debug("Initializing report preparation completed...");
	}

	@Override
	protected void postProcessing(JobExecutionContext arg0) throws JobExecutionException {
		logger.debug("Trigger mail daily report..");
		//Trigger here mail
		logger.debug("mail triggered  for daily report..");
	}
}