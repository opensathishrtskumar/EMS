package com.ems.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class CustomeDateFormatter extends AbstractFormatter {
	private static final long serialVersionUID = 2360995490761904944L;
	private String datePattern = "dd-MMM-yyyy";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);
	@Override
	public String valueToString(Object value) throws ParseException {
		if (value != null) {
			Calendar cal = (Calendar) value;
			return dateFormatter.format(cal.getTime());
		}
		return "";
	}

	@Override
	public Object stringToValue(String arg0) throws ParseException {
		return dateFormatter.parseObject(arg0);
	}
}

