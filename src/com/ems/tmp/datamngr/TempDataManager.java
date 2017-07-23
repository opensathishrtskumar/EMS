package com.ems.tmp.datamngr;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.security.Security;

public abstract class TempDataManager {
	private static final Logger logger = LoggerFactory
			.getLogger(TempDataManager.class);

	private static final String TEMP_FOLDER_NAME = "/EMSV1.1";
	private static final String FRAME_LOCK = "/lock.lock";
	private static final String TEMP_KEY = getTempDir() + TEMP_FOLDER_NAME;
	private static final String DBCONFIG = TEMP_KEY + "/dbconfig.db";
	public static final String MAIN_CONFIG = TEMP_KEY + "/config.db";

	public static boolean isTempFileAvailable(String fileName){
		File tempFile = new File(fileName);
		return tempFile.exists();
	}
	
	public static File createTempFolder(String fileName) {
		File tmpDir = new File(fileName);
		logger.trace("temp path : {}, \t exist : {}", tmpDir.getAbsolutePath(),
				tmpDir.exists());
		if (!tmpDir.exists()) {
			try {
				tmpDir.createNewFile();
				logger.info("temp config file created...");
			} catch (IOException e) {
				logger.error("Application temp file creation Failed {} ", fileName);
				logger.error("{}",e);
			}
		}
		return tmpDir;
	}
	
	public static void writeTempConfig(Properties props, String fileName) {

		if (props == null)
			return;
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			props.store(byteOutputStream, "UTF8");
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
					byteOutputStream.toByteArray());
			byteOutputStream.close();

			Security security = Security.getInstance();
			security.encrypt(byteInputStream, new FileOutputStream(
					new File(fileName)), 10);
		} catch (IOException | InvalidKeyException e) {
			logger.error("Error persisting DB Configs");
			logger.error("{}",e);
		}
	}
	
	
	public static Properties retrieveTempConfig(String fileName) {
		Properties props = new Properties();

		try {
			File dbConfig = new File(fileName);
			if(dbConfig.exists()){
				Security security = Security.getInstance();
				InputStream stream = security.getDecryptedStream(new FileInputStream(
						dbConfig));
				props.load(stream);
				stream.close();
			}
		} catch (Exception e) {
			logger.error("Failed to load DB Config : {}", e.getLocalizedMessage());
			logger.error("{}",e);
		}
		return props;
	}
	
	
	private static String getTempDir() {
		String tmpDir = getSystemTempFolder();
		File file = new File(tmpDir);
		return file.getParent();
	}
	
	public static String getSystemTempFolder(){
		return System.getProperty("java.io.tmpdir");
	}

	/**
	 * Cretes temp folder if doesn't exist in temp dir of current user
	 */
	public static File getTempFolder() {
		File tmpDir = new File(TEMP_KEY);
		logger.trace("temp path : {}, \t exist : {}", tmpDir.getAbsolutePath(),
				tmpDir.exists());
		if (!tmpDir.exists()) {
			boolean status = tmpDir.mkdir();
			logger.info("Application temp dir creation is {} ", status);
		}
		return tmpDir;
	}
	
	//launch4j takes care of Single instance
	public static boolean frameLockExist() {
		/*File temp = getTempFolder();
		File frameLock = new File(temp.getAbsolutePath() + FRAME_LOCK);
		return frameLock.exists();*/
		return false;
	}

	public static boolean deleteFrameLock() {
		File temp = getTempFolder();
		File frameLock = new File(temp.getAbsolutePath() + FRAME_LOCK);
		if (frameLock.exists()) {
			frameLock.delete();
		}
		return frameLock.exists();
	}

	public static boolean createFrameLock() {
		File temp = getTempFolder();
		File frameLock = new File(temp.getAbsolutePath() + FRAME_LOCK);
		if (!frameLock.exists()) {
			try {
				frameLock.createNewFile();
			} catch (IOException e) {
				logger.error("Error creating frame lock : \n {}, \nPath {}",
						e.getLocalizedMessage(), frameLock.getAbsolutePath());
				logger.error("{}",e);
			}
		}
		return frameLock.exists();
	}

	public static boolean checkDBConfigFile() {
		File dbConfig = new File(DBCONFIG);
		
		return dbConfig.exists();
	}
	
	public static boolean deleteConfigFile() {
		File dbConfig = new File(DBCONFIG);
		
		if (dbConfig.exists()) 
			return dbConfig.delete();
		
		return dbConfig.exists();
	}

	public static File getDBConfigFile(boolean createIfNotExist) {
		File file = new File(DBCONFIG);

		if (!file.exists() && createIfNotExist) {
			try {
				boolean dbFile = file.createNewFile();
				logger.info("DBConfig file creation status : {}", dbFile);
			} catch (IOException e) {
				logger.error("DB Config file creation failed : {} ",
						e.getLocalizedMessage());
				logger.error("{}",e);
			}
		}

		return file;
	}

	public static void writeDBConfig(Properties props) {

		if (props == null)
			return;
		try {
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			props.store(byteOutputStream, "UTF8");
			ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
					byteOutputStream.toByteArray());
			byteOutputStream.close();

			Security security = Security.getInstance();
			security.encrypt(byteInputStream, new FileOutputStream(
					getDBConfigFile(true)), 10);
		} catch (IOException | InvalidKeyException e) {
			logger.error("Error persisting DB Configs");
			logger.error("{}",e);
		}
	}

	public static Properties retrieveDBConfig() {
		Properties props = new Properties();

		try {
			File dbConfig = getDBConfigFile(false);
			if(dbConfig.exists()){
				Security security = Security.getInstance();
				InputStream stream = security.getDecryptedStream(new FileInputStream(
						dbConfig));
				props.load(stream);
				stream.close();
			}
		} catch (Exception e) {
			logger.error("Failed to load DB Config : {}", e.getLocalizedMessage());
			logger.error("{}",e);
		}
		
		return props;
	}
}

