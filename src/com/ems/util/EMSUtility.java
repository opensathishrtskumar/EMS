package com.ems.util;

import static com.ems.constants.EmsConstants.ENCODING;
import static com.ems.constants.EmsConstants.RETRYCOUNT;
import static com.ems.constants.EmsConstants.TIMEOUT;
import static com.ems.constants.MessageConstants.REPORT_KEY_SEPARATOR;
import static com.ems.constants.MessageConstants.REPORT_RECORD_SEPARATOR;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.constants.EmsConstants;
import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.procimg.Register;
import com.ghgande.j2mod.modbus.util.ModbusUtil;

public abstract class EMSUtility {

	private static final Logger logger = LoggerFactory.getLogger(EMSUtility.class);
	private static Pattern pattern = Pattern.compile("(COM)([0-9]){1,}");
	public static final String hh_mma = "hh:mma";
	public static final String DASHBOARD_FMT = "dd-MMM,yyyy";
	public static final String DASHBOARD_POLLED_FMT = "dd-MMM,yy hh:mm a";
	
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
				availablePorts[i] = ports[i].getSystemPortName();
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

	public static Object[] convertObjectArray(int...arg){
		Object[] response = new Object[arg.length];
		for(int i = 0; i < arg.length; i++){
			response[i] = String.valueOf(arg[i]);
		}
		return response;
	}

	public static Object[] convertObjectArray(String...arg){
		Object[] response = new Object[arg.length];
		for(int i = 0; i < arg.length; i++){
			response[i] = arg[i];
		}
		return response;
	}

	public static Properties loadProperties(String propertiesString){
		Properties mappings = new Properties();

		try {
			mappings.load(new ByteArrayInputStream(propertiesString.getBytes()));
		} catch (Exception e) {
			logger.error("error loading memory mapping : {}", e.getLocalizedMessage());
			logger.error("{}",e);
		}

		return mappings;
	}

	public static Map<Long,String> loadMemoryMappingDetails(String mappingDetails){
		long startingRegister = EmsConstants.DEFAULTREGISTER;
		int count = EmsConstants.REGISTERCOUNT;

		Map<Long,String> registerMapping = new TreeMap<Long,String>();

		try {
			Properties memoryMappings = loadProperties(mappingDetails);

			for(Entry<Object, Object> entry : memoryMappings.entrySet()){
				registerMapping.put(Long.parseLong(entry.getKey().toString()),
						entry.getValue().toString());
			}
		} catch (Exception e) {
			logger.error("error extracting reference and count : {}", e.getLocalizedMessage());
			logger.error("{}",e);

			registerMapping.put(startingRegister, "start");
			registerMapping.put(startingRegister + count, "end");
		} 
		logger.trace("Memory mapping details : {}", registerMapping);
		return registerMapping;
	}

	public static long getRegisterReference(Map<Long,String> mappings){
		long startingRegister = EmsConstants.DEFAULTREGISTER;

		try {
			TreeMap<Long,String> tMap = (TreeMap<Long,String>)mappings;
			NavigableMap<Long,String> nMap = tMap.descendingMap();
			logger.trace("{}",nMap);
			startingRegister = nMap.lastKey() - 1;
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Failed to get register reference : {}", e.getLocalizedMessage());
		}

		return startingRegister;
	}

	public static int getRegisterCount(Map<Long,String> mappings){
		int count = EmsConstants.REGISTERCOUNT;
		try {
			TreeMap<Long,String> tMap = (TreeMap<Long,String>)mappings;
			NavigableMap<Long,String> nMap = tMap.descendingMap();
			long countLong = nMap.firstKey() - nMap.lastKey();
			count = (int)(countLong + 1);
			//Make count is even , so that floating point value calculated
			
			logger.trace("Register Map : {} - Count : {}",nMap,countLong);
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Failed to get register count : {}", e.getLocalizedMessage());
		}

		return count;
	}

	public static String getHHmm(){
		return getFormattedDate("hh:mm a");
	}

	public static String getFormattedDate(String format){
		SimpleDateFormat formater = new SimpleDateFormat(format);
		return formater.format(new Date(System.currentTimeMillis()));
	}
	
	public static String getFormattedTime(long timeInMilli, String dateFormat){
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		return format.format(new Date(timeInMilli));
	}

	public static int[] getPersistRegisters(Set<Long> set){
		int[] registers = new int[set.size()];
		try {
			int index = 0;
			for(Long register : set){
				registers[index++] = register.intValue();
			}
			Arrays.sort(registers);

		} catch (Exception e) {
			logger.error("{}",e);
		}

		return registers;
	}

	public static String processRequiredRegister(Register[] registeres,
			ExtendedSerialParameter parameters) {
		StringBuilder builder = new StringBuilder();
		int[] requiredRegisters = parameters.getRequiredRegisters();
		int base = parameters.getReference();
		try {
			for(int reg : requiredRegisters){
				int registerIndex = reg - (base + 1);
				logger.trace(
						"Required register : {}, Base : {}, Register : {}, Registers Count : {}",
						requiredRegisters, base, reg, registeres.length);
				
				byte[] registerBytes = registeres[registerIndex].toBytes();
				byte[] bytes = new byte[]{0, 0, 0, 0};
				bytes[0] = registerBytes[0];
				bytes[1] = registerBytes[1];
				
				if(registerIndex + 1 < registeres.length){
					registerBytes = registeres[registerIndex + 1].toBytes();
					bytes[2] = registerBytes[0];
					bytes[3] = registerBytes[1];
				}
				
				builder.append(reg + REPORT_KEY_SEPARATOR
						+ String.valueOf(ModbusUtil.registersToFloat(bytes))
						+ REPORT_RECORD_SEPARATOR);
			}
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Mapping error : {}",e.getLocalizedMessage());
		}

		return builder.toString();
	}

	/**
	 * returns memory address and its value in Map structure
	 */
	public static Map<String, String> processRegistersForDashBoard(ExtendedSerialParameter parameters) {
		int[] requiredRegisters = parameters.getRequiredRegisters();
		int base = parameters.getReference();
		Register[] registeres = parameters.getRegisteres();
		
		Map<String, String> finalResponse = new LinkedHashMap<String, String>();
		try {
			for(int reg : requiredRegisters){
				int registerIndex = reg - (base + 1);
				String value = "00.000";
				
				if(registeres != null && registerIndex < registeres.length 
						&& registeres[registerIndex] != null){
					
					byte[] registerBytes = registeres[registerIndex].toBytes();
					
					byte[] bytes = new byte[]{0, 0, 0, 0};
					bytes[0] = registerBytes[0];
					bytes[1] = registerBytes[1];
					
					if(registerIndex + 1 < registeres.length){
						registerBytes = registeres[registerIndex + 1].toBytes();
						bytes[2] = registerBytes[0];
						bytes[3] = registerBytes[1];
					}
					
					value = String.valueOf(ModbusUtil.registersToFloat(bytes));
				}
				
				finalResponse.put(String.valueOf(reg), value);
			}
		} catch (Exception e) {
			logger.error("{}",e);
			logger.error("Dashboard Mapping error : {}",e.getLocalizedMessage());
		}

		return finalResponse;
	}
	
	public static List<ExtendedSerialParameter> mapDevicesToSerialParams(List<DeviceDetailsDTO> devices){
		List<ExtendedSerialParameter> paramList = new ArrayList<ExtendedSerialParameter>();

		for(DeviceDetailsDTO device : devices){
			paramList.add(mapDeviceToSerialParam(device));
		}

		return paramList;
	}


	public static ExtendedSerialParameter mapDeviceToSerialParam(DeviceDetailsDTO devices){

		ExtendedSerialParameter parameters = new ExtendedSerialParameter(
				devices.getPort(), devices.getBaudRate(), 0, 0,
				devices.getWordLength(), devices.getStopbit(), 0, false);

		parameters.setParity(devices.getParity());
		parameters.setRetries(RETRYCOUNT);
		parameters.setTimeout(TIMEOUT[1]);
		parameters.setEncoding(ENCODING[1]);
		parameters.setUnitId(devices.getDeviceId());
		parameters.setUniqueId(devices.getUniqueId());
		parameters.setRowIndex(devices.getRowIndex());
		parameters.setMemoryMappings(loadMemoryMappingDetails(devices.getMemoryMapping()));
		parameters.setPortName(devices.getPort());
		
		parameters.setDeviceName(devices.getDeviceName());

		return parameters;
	}
	
	/**
	 * Group device by connection param, so that single connection can be reused to set of devices
	 */
	public static Map<String, List<ExtendedSerialParameter>> groupDeviceForPolling(List<ExtendedSerialParameter> paramsList){
		Map<String, List<ExtendedSerialParameter>> groupedDevice = new HashMap<String, List<ExtendedSerialParameter>>();
		
		for(ExtendedSerialParameter device : paramsList){
			List<ExtendedSerialParameter> group = groupedDevice.get(device.getGroupKey());
			
			if(group == null){
				group = new ArrayList<ExtendedSerialParameter>();
				groupedDevice.put(device.getGroupKey(), group);
			}
			
			group.add(device);
		}
		return groupedDevice;
	}
	
}
