package org.ems.validator;

import org.ems.model.Account;
import org.ems.model.ChangePasswordForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;

import com.ems.security.Security;

public class ChangePasswordValidator {

	private static final Logger logger = LoggerFactory.getLogger(ChangePasswordValidator.class);

	private final ChangePasswordForm form;
	private final BindingResult formBinding;
	private final Security security;

	public ChangePasswordValidator(ChangePasswordForm form, BindingResult formBinding, Security security) {
		this.form = form;
		this.formBinding = formBinding;
		this.security = security;
	}

	public BindingResult validateChangePasswordForm() {

		if (formBinding.hasErrors()) {
			return formBinding;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Account account = (Account) authentication.getPrincipal();

		form.setId(account.getId());

		String encryptedPassword = "";
		String confirmPassword = "";
		try {
			encryptedPassword = this.security.encrypt(form.getOldPassword());
			confirmPassword = this.security.encrypt(this.form.getConfirmPassword());
		} catch (Exception e) {
			logger.error(" password encryption error {}", e);
		}

		if (!encryptedPassword.equals(account.getEncryptedPassword())) {
			formBinding.rejectValue("oldPassword", "changepassword.mismatch");
		}

		if (!form.getPassword().equals(form.getConfirmPassword())) {
			formBinding.rejectValue("confirmPassword", "changepassword.mismatch");
		}

		form.setConfirmPassword(confirmPassword);

		return formBinding;
	}

}
