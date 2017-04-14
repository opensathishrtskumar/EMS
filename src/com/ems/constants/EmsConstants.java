package com.ems.constants;

public abstract class EmsConstants {

	public static final int[] BAUDRATES = { 110, 300, 600, 1200, 2400, 4800,
			9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000 };

	public static final int[] WORDLENGTH = { 7, 8 };

	public static final String[] PARITY = { "none", "odd", "even", "mark", "space" };

	public static final int[] STOPBIT = { 1, 2 };

	public static final String[] POINTYPE = { "01 - COIL STATUS",
			"02 - INPUT STATUS", "03 - HOLDING REGISTERS",
			"04 - " + "INPUT REGISTERS" };
	
	public static final String[] ENCODING = { "ascii", "rtu", "bin"};
	
	public static final String[] DTR = {"Disable", "Enable", "Handshake"};
	
	public static final String[] RTS = {"Disable", "Enable", "Handshake", "Toggle", "Manual"};
	
	public static final int[] TIMEOUT = {500, 1000, 1500, 2000, 2500};
	
	public static int RETRYCOUNT = 1;
}
