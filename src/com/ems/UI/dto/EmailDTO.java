package com.ems.UI.dto;

import java.io.Serializable;
import java.util.List;

import com.ems.constants.EmailConstants;

public class EmailDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String fromEmail;
	private String mailPassword;
	private String toEmail;
	private String ccEmail;
	private String bccEmail;
	private String subject;
	private String body;
	private List<AttachmentDTO> attachments;
	
	
	private String date;

	// Set default empty so that it can be updated in props file directly
	public EmailDTO() {
		this.fromEmail = "";
		this.mailPassword = "";
		this.toEmail = "";
		this.ccEmail = "";
	}

	public String getFromEmail() {
		if (fromEmail == null || fromEmail.isEmpty())
			return EmailConstants.DEFAULT_FROM_EMAIL;
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getMailPassword() {
		if (mailPassword == null || mailPassword.isEmpty())
			return EmailConstants.DEFAULT_EMAIL_PASSWORD;
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public String getCcEmail() {
		return ccEmail;
	}

	public void setCcEmail(String ccEmail) {
		this.ccEmail = ccEmail;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public List<AttachmentDTO> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<AttachmentDTO> attachments) {
		this.attachments = attachments;
	}

	public String getBccEmail() {
		return bccEmail;
	}

	public void setBccEmail(String bccEmail) {
		this.bccEmail = bccEmail;
	}
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "EmailDTO [fromEmail=" + fromEmail + ", toEmail=" + toEmail + ", ccEmail=" + ccEmail + ", subject="
				+ subject + ", body=" + body + "]";
	}
}
