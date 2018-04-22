package com.ems.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.constants.EmsConstants;

public class MemoryMappingParser {

	private static final Logger logger = LoggerFactory.getLogger(MemoryMappingParser.class);

	public static final Pattern MEMORY_MAPPING_PAT = Pattern.compile("(.*)(=)(.*)");

	public static final Pattern MARKER_MAPPPING_PAT = Pattern.compile("NoMap|split", Pattern.CASE_INSENSITIVE);

	public static Map<String, String> parseMemoryMappingReverse(String memoryMappingReverse) {
		LinkedHashMap<String, String> memoryMappingMap = new LinkedHashMap<>();

		Matcher matcher = MEMORY_MAPPING_PAT.matcher(memoryMappingReverse);
		while (matcher.find()) {
			memoryMappingMap.put(matcher.group(3), matcher.group(1));
		}

		return memoryMappingMap;
	}

	public static Map<String, String> parseMemoryMapping(String memoryMapping) {
		LinkedHashMap<String, String> memoryMappingMap = new LinkedHashMap<>();

		Matcher matcher = MEMORY_MAPPING_PAT.matcher(memoryMapping);
		while (matcher.find()) {
			memoryMappingMap.put(matcher.group(1), matcher.group(3));
		}

		return memoryMappingMap;
	}

	public static Map<String, String> removeMemoryMarkers(Map<String, String> input) {

		List<String> memoryAddress = new ArrayList<>();

		for (Entry<String, String> entry : input.entrySet()) {
			if (MARKER_MAPPPING_PAT.matcher(entry.getValue()).find()) {
				memoryAddress.add(entry.getKey());
			}
		}
		memoryAddress.forEach(address -> input.remove(address));
		return input;
	}

	public static Properties loadProperties(String propertiesString) {
		Properties mappings = new OrderedProperties();
		if (propertiesString != null) {
			Matcher matcher = MEMORY_MAPPING_PAT.matcher(propertiesString);
			while (matcher.find()) {
				mappings.put(matcher.group(1), matcher.group(3));
			}
		}
		return mappings;
	}

	public static Map<Long, String> loadMemoryMappingDetails(String mappingDetails) {
		long startingRegister = EmsConstants.DEFAULTREGISTER;
		int count = EmsConstants.REGISTERCOUNT;

		Map<Long, String> registerMapping = new TreeMap<Long, String>();

		try {

			Matcher matcher = MEMORY_MAPPING_PAT.matcher(mappingDetails);
			while (matcher.find()) {
				registerMapping.put(Long.parseLong(matcher.group(1)), matcher.group(3));
			}

		} catch (Exception e) {
			logger.error("{}", e);
			registerMapping.put(startingRegister, "start");
			registerMapping.put(startingRegister + count, "end");
		}
		logger.trace("Memory mapping details : {}", registerMapping);
		return registerMapping;
	}
}
