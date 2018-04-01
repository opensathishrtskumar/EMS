package org.ems.controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.ems.model.DateRangeReportForm;
import org.ems.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ems.util.EMSUtility;

@Controller
public class ReportingController {

	private static final Logger logger = LoggerFactory.getLogger(ReportingController.class);

	@Autowired
	private ReportService reportService;

	@RequestMapping(value = "/ems/reports", method = RequestMethod.GET)
	public ModelAndView showReportsPage() {
		return new ModelAndView("reports");
	}

	@RequestMapping(value = "/ems/reports/daterange", method = RequestMethod.GET)
	public ModelAndView getDateRangeReportsPage() {
		ModelAndView view = new ModelAndView("reports/daterange", "reportForm", new DateRangeReportForm());
		view.addObject("deviceNames", reportService.fetchActiveDevices());
		return view;
	}

	@RequestMapping(value = "/ems/reports/daterange", method = RequestMethod.POST)
	public void postDateRangeReportsPage(@Valid DateRangeReportForm form, BindingResult formBinding,
			HttpServletResponse response) {

		logger.debug("DateRange report request : {}", EMSUtility.convertObjectToJSONString(form));

		if (formBinding.hasErrors()) {
			logger.error("Form has error !!!");
		}

		File file = new File("C:\\Users\\RTS Sathish  Kumar\\Desktop\\RRB_1880091263.pdf");

		response.setContentType("application/octet-stream");
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getName()));
		response.setContentLength((int) file.length());
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file));) {
			FileCopyUtils.copy(inputStream, response.getOutputStream());
		} catch (Exception e) {
			logger.error("error downloading report file : {}", e);
		}
	}

}
