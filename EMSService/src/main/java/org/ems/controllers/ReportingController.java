package org.ems.controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.util.EMSUtility;
import com.ems.util.MemoryMappingParser;

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

	@RequestMapping(value = "/ems/reports/getmemorymapping/{deviceid}", method = { RequestMethod.GET,
			RequestMethod.POST })
	public @ResponseBody Map<String, String> getMemoryMappings(@PathVariable("deviceid") int deviceid) {
		logger.trace("Memory mapping requested for deviceuniqueid {}", deviceid);

		List<DeviceDetailsDTO> activeDevices = reportService.fetchActiveDevices();

		List<DeviceDetailsDTO> requestedDevice = activeDevices.stream()
				.filter(device -> device.getUniqueId() == deviceid).collect(Collectors.toList());

		Map<String, String> memoryMapping = new LinkedHashMap<>();

		if (!requestedDevice.isEmpty()) {
			Map<String, String> memoryMappings = MemoryMappingParser
					.parseMemoryMapping(requestedDevice.get(0).getMemoryMapping());

			memoryMapping = MemoryMappingParser.removeMemoryMarkers(memoryMappings);
		}

		return memoryMapping;
	}

	@RequestMapping(value = "/ems/reports/daterange", method = RequestMethod.POST)
	public void postDateRangeReportsPage(@Valid DateRangeReportForm form, BindingResult formBinding,
			HttpServletResponse response) {

		logger.debug("DateRange report requested with input {}", EMSUtility.convertObjectToJSONString(form));

		if (formBinding.hasErrors()) {
			logger.error("Form has error !!!");
		}

		String deviceNames = form.getDeviceName();
		String memoryMappings = form.getMemoryMappingDetails();
		form.getReportStartTime();
		form.getReportEndTime();

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
