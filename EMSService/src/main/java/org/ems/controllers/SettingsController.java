package org.ems.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.ems.dao.UserDetailsDAO;
import org.ems.model.Account;
import org.ems.model.ChangePasswordForm;
import org.ems.validator.ChangePasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ems.security.Security;

@Controller
public class SettingsController {
	private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);
	
	@Autowired
	private Security security;

	@Autowired
	private UserDetailsDAO userDetailsDAO;
	
	@RequestMapping(value = "/ems/settings", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView showSettings(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("settings");
	}
	
	@RequestMapping(value = "/ems/changePassword", method = RequestMethod.GET)
	public ModelAndView getChangePassword(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView view = new ModelAndView("reset/changePassword");
		view.addObject("changePasswordForm", new ChangePasswordForm());
		return view;
	}

	@RequestMapping(value = "/ems/changePassword", method = RequestMethod.POST)
	public ModelAndView postChangePassword(@Valid ChangePasswordForm form, BindingResult formBinding,
			HttpServletRequest request, HttpServletResponse response) {
		// throw with error if form has errors
		ModelAndView view = new ModelAndView("reset/changePassword");

		ChangePasswordValidator validator = new ChangePasswordValidator(form, formBinding, security);
		formBinding = validator.validateChangePasswordForm();

		if (formBinding.hasErrors()) {
			return view;
		}

		// Change the password and redirect to success page
		int count = userDetailsDAO.updatePassword(form);
		view.addObject("message", count);

		// Set the current password back in context
		Account account = (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		account.setEncryptedPassword(form.getConfirmPassword());

		return view;
	}
}
