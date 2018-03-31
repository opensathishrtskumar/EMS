package org.ems.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ems.model.SignupForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserManagementController {

	private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

	@Autowired
	private Environment environment;

	@RequestMapping(value = "/signin", method = RequestMethod.GET)
	public void showSignIn() {
		//
	}

	@RequestMapping(value = "/signout", method = RequestMethod.GET)
	public void showSignout() {
		//
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public ModelAndView showRegistration(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView view = new ModelAndView("signup");
		view.addObject("signupForm", new SignupForm());
		return view;
	}

	@RequestMapping(value = "/ems/homeSignedIn", method = RequestMethod.GET)
	public ModelAndView showHomeSignedIn(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("homeSignedIn");
	}

	
}
