package org.ems.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ChangePasswordForm implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	@NotNull
	@Size(min = 6, message = "must be at least 6 characters", max = 15)
	private String oldPassword;

	@NotNull
	@Size(min = 6, message = "must be at least 6 characters", max = 15)
	private String password;
	@NotNull
	@Size(min = 6, message = "must be at least 6 characters", max = 15)
	private String confirmPassword;

	public ChangePasswordForm() {
		//
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	@Override
	public String toString() {
		return "ChangePasswordForm [password=" + password + ", confirmPassword=" + confirmPassword + "]";
	}
}
