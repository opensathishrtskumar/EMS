package org.ems.config.listener;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class AppContextAware implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		AppContextAware.applicationContext = applicationContext;
	}

	public static ApplicationContext getContext() {
		return applicationContext;
	}
}
