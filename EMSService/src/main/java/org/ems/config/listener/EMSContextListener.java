package org.ems.config.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EMSContextListener implements ServletContextListener{

	private final static Logger logger = LoggerFactory.getLogger(EMSContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		logger.info("context initialized...");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("context destroyed...");
	}
}
