package com.ems.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MemoryMappingParser {

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

}
