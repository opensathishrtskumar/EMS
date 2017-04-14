package util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

public abstract class EMSUtility {

	private static final Logger logger = LoggerFactory.getLogger(EMSUtility.class);
	private static Pattern pattern = Pattern.compile("(COM)([0-9]){1,}");

	/**
	 * return Available Serial ports as array
	 */
	public static String[] getAvailablePort(){
		String[] availablePorts = {};

		try {
			SerialPort[] ports = SerialPort.getCommPorts();

			if(ports != null){
				availablePorts = new String[ports.length];
			}

			for(int i = 0;i < ports.length ; i++){
				availablePorts[i] = ports[i].getDescriptivePortName();
			}
		} catch (Exception e) {
			StringBuilder builder = new StringBuilder();
			builder.append("Failed to load Serial Ports" + e.getLocalizedMessage());
			logger.error(builder.toString());
		}

		return availablePorts;
	}

	/**
	 * @param portName
	 * @return Extract only port name from descriptive name
	 */
	public static String extractPortName(String portName){
		Matcher matcher = pattern.matcher(portName);
		if(matcher.find())
			portName = matcher.group();
		return portName;
	}

}
