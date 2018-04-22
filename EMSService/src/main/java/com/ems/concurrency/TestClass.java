package com.ems.concurrency;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ems.util.MemoryMappingParser;

public class TestClass {

	private static final Pattern MEMORY_MAPPING_PAT = Pattern.compile("(.*)(=)(.*)");

	public static void main(String[] args) throws Exception {
		
		Pattern patter = Pattern.compile("NoMap|split", Pattern.CASE_INSENSITIVE);
		
		String data = "NoMap";
		
		Matcher matcher = patter.matcher(data);
		
		System.out.println(matcher.find());
		
	}
}
