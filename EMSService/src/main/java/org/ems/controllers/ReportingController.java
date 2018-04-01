package org.ems.controllers;

import org.ems.model.SignupForm;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ReportingController {
	
	@RequestMapping(value = "/ems/reports", method = RequestMethod.GET)
	public ModelAndView showReportsPage() {
		return new ModelAndView("reports");
	}
	
	@RequestMapping(value = "/ems/reports/daterange", method = RequestMethod.GET)
	public ModelAndView showDateRangeReportsPage() {
		ModelAndView view = new ModelAndView("reports/daterange");
		view.addObject("reportForm", new SignupForm());
		return view;
	}
	
}
