package com.ems.UI.dto;

import java.io.File;
import java.io.Serializable;

public class AttachmentDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private File file;
	private String fileName;
	
	
	public AttachmentDTO(File file, String fileName) {
		this.file = file;
		this.fileName = fileName;
	}
	
	public AttachmentDTO() {
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		if(fileName == null && file != null){
			fileName = file.getName();
		}
		
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
