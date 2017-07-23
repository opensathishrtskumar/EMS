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
import java.util.Enumeration;
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
import com.ems.UI.dto.GroupsDTO;
import com.ems.UI.dto.SplitJoinDTO;
import com.ems.constants.EmsConstants;
import com.fazecast.jSerialComm.SerialPort;
import com.ghgande.j2mod.modbus.Modbus;
import com.ghgande.j2mod.modbus.msg.ModbusRequest;
import com.ghgande.j2mod.modbus.msg.ReadInputRegistersRequest;
import com.ghgande.j2mod.modbus.msg.ReadMultipleRegistersRequest;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.util.ModbusUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class EMSUtility {

	private static final Logger logger = LoggerFactory.getLogger(EMSUtility.class);
	private static Pattern pattern = Pattern.compile("(COM)([0-9]){1,}");
	public static final String hh_mma = "hh:mma";
	public static final String DASHBOARD_FMT = "dd-MMM,yyyy";
	public static final String DASHBOARD_POLLED_FMT = "dd-MMM,yy hh:mm a";
	public static final String REPORTNAME_FORMAT = "ddMMyyHHmm";
	

	/**
	 * return Available Serial ports as array
	 */
	public static String[] getAvailablePort() {
		String[] availablePorts = {};

		try {
			SerialPort[] ports = SerialPort.getCommPorts();

			if (ports != null) {
				availablePorts = new String[ports.length];
			}

			for (int i = 0; i < ports.length; i++) {
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
	public static String extractPortName(String portName) {
		Matcher matcher = pattern.matcher(portName);
		if (matcher.find())
			portName = matcher.group();
		return portName;
	}

	public static Object[] convertObjectArray(int... arg) {
		Object[] response = new Object[arg.length];
		for (int i = 0; i < arg.length; i++) {
			response[i] = String.valueOf(arg[i]);
		}
		return response;
	}

	public static Object[] convertObjectArray(String... arg) {
		Object[] response = new Object[arg.length];
		for (int i = 0; i < arg.length; i++) {
			response[i] = arg[i];
		}
		return response;
	}

	/**
	 * @param propertiesString
	 * @returns Properties with keys in order which it is loaded
	 */
	public static Properties loadProperties(String propertiesString) {
		Properties mappings = new OrderedProperties();

		try {
			mappings.load(new ByteArrayInputStream(propertiesString.getBytes()));
		} catch (Exception e) {
			logger.error("error loading memory mapping : {}", e.getLocalizedMessage());
			logger.error("{}", e);
		}

		return mappings;
	}

	public static Map<Long, String> loadMemoryMappingDetails(String mappingDetails) {
		long startingRegister = EmsConstants.DEFAULTREGISTER;
		int count = EmsConstants.REGISTERCOUNT;

		Map<Long, String> registerMapping = new TreeMap<Long, String>();

		try {
			Properties memoryMappings = loadProperties(mappingDetails);

			for (Entry<Object, Object> entry : memoryMappings.entrySet()) {
				registerMapping.put(Long.parseLong(entry.getKey().toString()), entry.getValue().toString());
			}
		} catch (Exception e) {
			logger.error("error extracting reference and count : {}", e.getLocalizedMessage());
			logger.error("{}", e);

			registerMapping.put(startingRegister, "start");
			registerMapping.put(startingRegister + count, "end");
		}
		logger.trace("Memory mapping details : {}", registerMapping);
		return registerMapping;
	}

	/**
	 * @param mappings
	 * @returns Starting register - Initial required register - 1 is the
	 *          reference
	 */
	public static long getRegisterReference(Map<Long, String> mappings) {
		long startingRegister = EmsConstants.DEFAULTREGISTER;

		try {
			TreeMap<Long, String> tMap = (TreeMap<Long, String>) mappings;
			NavigableMap<Long, String> nMap = tMap.descendingMap();
			logger.trace("{}", nMap);
			startingRegister = nMap.lastKey() - 1;
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed to get register reference : {}", e.getLocalizedMessage());
		}

		return startingRegister;
	}

	public static int getRegisterCount(Map<Long, String> mappings) {
		int count = EmsConstants.REGISTERCOUNT;
		try {
			TreeMap<Long, String> tMap = (TreeMap<Long, String>) mappings;
			NavigableMap<Long, String> nMap = tMap.descendingMap();
			long countLong = nMap.firstKey() - nMap.lastKey();
			count = (int) (countLong + 1);
			// Make count is even , so that floating point value calculated

			logger.trace("Register Map : {} - Count : {}", nMap, countLong);
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed to get register count : {}", e.getLocalizedMessage());
		}

		return count;
	}

	public static String getHHmm() {
		return getFormattedDate("hh:mm a");
	}

	public static String getFormattedDate(String format) {
		SimpleDateFormat formater = new SimpleDateFormat(format);
		return formater.format(new Date(System.currentTimeMillis()));
	}

	public static String getFormattedTime(long timeInMilli, String dateFormat) {
		SimpleDateFormat format = new SimpleDateFormat(dateFormat);
		return format.format(new Date(timeInMilli));
	}

	public static Integer[] getPersistRegisters(Set<Long> set) {
		Integer[] registers = new Integer[set.size()];
		try {
			int index = 0;
			for (Long register : set) {
				registers[index++] = register.intValue();
			}
			Arrays.sort(registers);

		} catch (Exception e) {
			logger.error("{}", e);
		}

		return registers;
	}

	/**
	 * returns memory address and its value in Map structure
	 */
	public static Map<String, String> processRegistersForDashBoard(ExtendedSerialParameter parameters) {

		Map<String, String> finalResponse = new LinkedHashMap<String, String>();

		if (parameters.isSplitJoin()) {
			finalResponse = convertSplitJoinResponse(parameters);
		} else {
			int[] requiredRegisters = parameters.getRequiredRegisters();
			int base = parameters.getReference();
			InputRegister[] registers = parameters.getRegisteres();
			finalResponse = convertRegistersToMap(base, requiredRegisters, registers, parameters.getRegisterMapping());
		}

		logger.trace("Uniqueid {}'s final Response map is {}", parameters.getUniqueId(), finalResponse);

		return finalResponse;
	}

	/**
	 * @param parameters
	 * @return merged Map of SplitJoin Response
	 */
	public static Map<String, String> convertSplitJoinResponse(ExtendedSerialParameter parameters) {
		Map<String, String> finalResponse = new LinkedHashMap<String, String>();
		SplitJoinDTO splitJoinDto = parameters.getSplitJoinDTO();
		String msrfOrLsrf = parameters.getRegisterMapping();

		if (splitJoinDto != null) {
			if (validateSplitJoinDtoValues(splitJoinDto)) {
				
				List<Integer[]> requiredRegisters = splitJoinDto.getRequiredRegisters();// All the required registers
				List<Integer> reference = splitJoinDto.getReferencce();// Base register
				List<InputRegister[]> registers = splitJoinDto.getRegisteres();// Response registers
				List<Boolean> status = splitJoinDto.getStatus();// Subset execution status
				
				int index = 0;
				for (int base : reference) {
					
					if(status.get(index)){
						Map<String, String> subSetResponse = convertRegistersToMap(base,
								convertWrapper2Int(requiredRegisters.get(index)), registers.get(index), msrfOrLsrf);
						logger.debug(" SplitJoin partial response map {} ", subSetResponse);
						finalResponse.putAll(subSetResponse);
						index++;
					}
				}
			} else {
				logger.info("Split Join DTO values size is different {} ", convertObjectToJSONString(splitJoinDto));
			}
		}

		return finalResponse;
	}

	/**
	 * @param base
	 * @param requiredRegisters
	 * @param registers
	 * @param msrfOrLsrf
	 * @return Converts InputResgister[] to Map based on base register and
	 *         MSRF/LSRF
	 */
	public static Map<String, String> convertRegistersToMap(int base, int[] requiredRegisters, InputRegister[] registers,
			String msrfOrLsrf) {
		Map<String, String> finalResponse = new LinkedHashMap<String, String>();

		for (int reg : requiredRegisters) {
			int registerIndex = reg - (base + 1);
			String value = "00.00";

			try {
				value = getRegisterValue(registerIndex, registers, msrfOrLsrf);
			} catch (Exception e) {
				logger.error("Setting default value for register {}", e);
				value = "00.00";
			}

			finalResponse.put(String.valueOf(reg), value);
		}

		return finalResponse;
	}

	public static String processRequiredRegister(ExtendedSerialParameter parameters) {
		Map<String, String> responseMap = processRegistersForDashBoard(parameters);
		String resonse = convertMapToUnitResponse(responseMap);
		logger.trace("Response for device {} is {}", parameters.getUnitId(), resonse);
		return resonse;
	}

	private static String getRegisterValue(int index, InputRegister[] registers, String registerMapping) {

		byte[] bytes = new byte[] { 0, 0, 0, 0 };
		if (registers != null && index < registers.length) {
			byte[] registerBytes = registers[index].toBytes();

			bytes[0] = registerBytes[0];
			bytes[1] = registerBytes[1];

			if (index + 1 < registers.length) {
				registerBytes = registers[index + 1].toBytes();
				bytes[2] = registerBytes[0];
				bytes[3] = registerBytes[1];
			}
		}
		// register ordering MSRF/LSRF
		return convertToFloatWithOrder(bytes, registerMapping);
	}

	public static List<ExtendedSerialParameter> mapDevicesToSerialParams(List<DeviceDetailsDTO> devices) {
		List<ExtendedSerialParameter> paramList = new ArrayList<ExtendedSerialParameter>();

		for (DeviceDetailsDTO device : devices) {
			paramList.add(mapDeviceToSerialParam(device));
		}

		return paramList;
	}

	public static ExtendedSerialParameter mapDeviceToSerialParam(DeviceDetailsDTO devices) {

		ExtendedSerialParameter parameters = new ExtendedSerialParameter(devices.getPort(), devices.getBaudRate(), 0, 0,
				devices.getWordLength(), devices.getStopbit(), 0, false);

		parameters.setParity(devices.getParity());
		parameters.setRetries(RETRYCOUNT);
		parameters.setTimeout(TIMEOUT[1]);
		parameters.setEncoding(ENCODING[1]);
		parameters.setUnitId(devices.getDeviceId());
		parameters.setUniqueId(devices.getUniqueId());
		parameters.setRowIndex(devices.getRowIndex());
		devices.setSplitJoin(EMSUtility.isSplitJoin(devices.getMemoryMapping()));
		parameters.setSplitJoin(devices.isSplitJoin());
		parameters.setDeviceName(devices.getDeviceName());
		parameters.setRegisterMapping(devices.getRegisterMapping());
		parameters.setPort(devices.getPort());// PortName property should be set
												// to create SerialConnection
		parameters.setMethod(devices.getMethod());

		if (!parameters.isSplitJoin()) {
			Properties memoryProps = loadProperties(devices.getMemoryMapping());
			parameters.setProps(memoryProps);
			parameters.setMemoryMappings(loadMemoryMappingDetails(devices.getMemoryMapping()));
		} else {
			logger.debug("Split Join device Found : {}", devices);
			SplitJoinDTO splitJoinDto = new SplitJoinDTO();
			parameters.setSplitJoinDTO(splitJoinDto);

			String[] memoryMappings = devices.getMemoryMapping().split(EmsConstants.SPLIT_JOIN);

			for (String memoryMapping : memoryMappings) {
				
				if (memoryMapping == null || memoryMapping.trim().length() == 0){
					logger.debug("SJ Memory mapping is null continue");
					continue;
				}
				
				try {
					Properties props = loadProperties(memoryMapping);
					Map<Long, String> mappings = loadMemoryMappingDetails(memoryMapping);

					splitJoinDto.getProps().add(props);
					splitJoinDto.getMemoryMappings().add(mappings);
					// Starting register from where to read
					splitJoinDto.getReferencce().add((int) getRegisterReference(mappings));
					// Total number of registers to be read from Reference register
					splitJoinDto.getCount().add(getRegisterCount(mappings));
					// Contains sorted registers to be persisted
					splitJoinDto.getRequiredRegisters().add(getPersistRegisters(mappings.keySet()));
					//Set default execution status as false
					splitJoinDto.getStatus().add(false);
					//Set default Response registers null
					splitJoinDto.getRegisteres().add(null);
				} catch (Exception e) {
					logger.error("{}",e);
				}
			}
			
			logger.trace("SplitJoin DTO for device {} is {} ", devices.getUniqueId(),
					convertObjectToJSONString(splitJoinDto));
		}

		return parameters;
	}

	/**
	 * Group device by connection param, so that single connection can be reused
	 * to set of devices
	 * 
	 * Keys are created in DeviceDetailsDTO & ExtendedSerialParameter
	 */
	public static Map<String, List<ExtendedSerialParameter>> groupDeviceForPolling(
			List<ExtendedSerialParameter> paramsList) {
		Map<String, List<ExtendedSerialParameter>> groupedDevice = new HashMap<String, List<ExtendedSerialParameter>>();

		for (ExtendedSerialParameter device : paramsList) {
			List<ExtendedSerialParameter> group = groupedDevice.get(device.getGroupKey());
			// Create group when doesn't exist
			if (group == null) {
				group = new ArrayList<ExtendedSerialParameter>();
				groupedDevice.put(device.getGroupKey(), group);
			}
			// Add device in group
			group.add(device);
		}
		return groupedDevice;
	}

	/**
	 * @param bytes
	 *            - read from Modbus slave
	 * @param order
	 *            - the order of which registeres to be processed
	 * @return
	 */
	public static String convertToFloatWithOrder(byte[] bytes, String registeOrder) {
		byte[] byteOrder = null;

		if (registeOrder.equals(EmsConstants.REG_MAPPING[0])) {
			byteOrder = bytes;
		} else {
			byteOrder = new byte[] { bytes[2], bytes[3], bytes[0], bytes[1] };
		}

		String value = String.format("%.2f", ModbusUtil.registersToFloat(byteOrder));

		return value;
	}

	public static Map<String, String> getOrderedProperties(Properties props) {
		Map<String, String> map = new LinkedHashMap<String, String>();

		if (props instanceof OrderedProperties) {
			OrderedProperties orderProp = (OrderedProperties) props;
			Enumeration<Object> list = orderProp.keys();

			while (list.hasMoreElements()) {
				Object key = list.nextElement();
				Object value = props.get(key);
				String val = null;

				if (value != null)
					val = value.toString();

				map.put(key.toString(), val);
			}

		} else {
			map.putAll((Map) props);
		}

		return map;
	}

	public static Map<String, String> getOrderedProperties(ExtendedSerialParameter device){
		
		if(device.isSplitJoin()){
			List<Properties> list = device.getSplitJoinDTO().getProps();
			Map<String, String> map = new LinkedHashMap<String, String>();
			
			for(Properties props : list){
				map.putAll(getOrderedProperties(props));
			}
			
			return map;
		} else {
			return getOrderedProperties(device.getProps());
		}
	}
	
	
	public static GroupsDTO fetchGroupedDevices() {
		String groupingDetails = ConfigHelper.getGroupingDetails();
		logger.debug("Grouped devices from prop : " + groupingDetails);
		GroupsDTO groups = (GroupsDTO)convertJson2Object(groupingDetails, GroupsDTO.class); 
		return groups;
	}

	public static Object convertJson2Object(String json, Class type) {
		Gson gson = new GsonBuilder().create();
		return (Object)gson.fromJson(json, type);
	}
	
	public static boolean isNullEmpty(String value) {
		return value == null || value.trim().isEmpty();
	}

	public static ModbusRequest getRequest(String method, int reference, int count) {
		if (method.equals(String.valueOf(Modbus.READ_MULTIPLE_REGISTERS))) {
			return new ReadMultipleRegistersRequest(reference, count);
		} else {
			return new ReadInputRegistersRequest(reference, count);
		}
	}

	public static boolean isSplitJoin(String memoryMappings) {
		return memoryMappings != null && memoryMappings.trim().length() > 0
				&& memoryMappings.contains(EmsConstants.SPLIT_JOIN);
	}

	public static Map<String, String> convertProp2Map(Properties prop) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.putAll((Map) prop);
		return map;
	}

	public static int[] convertWrapper2Int(Integer[] intList) {
		return Arrays.stream(intList).mapToInt(Integer::intValue).toArray();
	}

	public static String convertObjectToJSONString(Object obj) {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(obj);
	}
	
	/**
	 * @param splitJoinDto
	 * @return boolean
	 * Checks all the values size of SplitJoinDTO are same
	 */
	private static boolean validateSplitJoinDtoValues(SplitJoinDTO splitJoinDto) {

		return (splitJoinDto.getRequiredRegisters().size() == splitJoinDto.getReferencce().size()
				&& splitJoinDto.getRegisteres().size() == splitJoinDto.getStatus().size());
	}
	
	
	/**
	 * @param map
	 * @return String
	 * 
	 * converts Map to UnitResponse String
	 */
	public static String convertMapToUnitResponse(Map<String, String> map){
		StringBuilder builder = new StringBuilder();
		
		for(Entry<String, String> e : map.entrySet()){
			builder.append(String.valueOf(e.getKey()));
			builder.append(REPORT_KEY_SEPARATOR);
			builder.append(String.valueOf(e.getValue()));
			builder.append(REPORT_RECORD_SEPARATOR);
		}
		
		return builder.toString();
	}
	
	
	public static boolean splitJoinStatus(List<Boolean> list){
		boolean status = true;
		
		for(boolean subSet : list){
			status = status & subSet;
		}
		
		return status;
	}
}
