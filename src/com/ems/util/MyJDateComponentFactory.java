package com.ems.util;

import java.util.Locale;
import java.util.Properties;

import org.jdatepicker.JDateComponentFactory;

public class MyJDateComponentFactory extends JDateComponentFactory{
	@Override
	public Properties getI18nStrings(Locale locale) {
		return super.getI18nStrings(locale);
	}
}
