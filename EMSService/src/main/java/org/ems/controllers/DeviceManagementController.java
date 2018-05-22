package org.ems.controllers;

import javax.validation.Valid;

import org.ems.model.DeviceForm;
import org.ems.service.DeviceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.ems.constants.EmsConstants;
import com.ems.util.EMSUtility;

@Controller
public class DeviceManagementController {

	private static final Logger logger = LoggerFactory.getLogger(DeviceManagementController.class);
	
	@Autowired
	private DeviceManagementService deviceService;
	
	@RequestMapping(value = "/ems/devicemanagement", method = RequestMethod.GET)
	public ModelAndView deviceManagementPage() {
		
		return new ModelAndView("devicemanagement");
	}
	
	@RequestMapping(value = "/ems/devicemanagement/showdevice", method = RequestMethod.GET)
	public ModelAndView showDevicesPage() {
		
		ModelAndView view = new ModelAndView("devicemanagement/showdevice");
		view.addObject("DeviceNames", this.deviceService.getAllDeviceDetails());
		view.addObject("BaudRates", EmsConstants.BAUDRATES);
		view.addObject("WordLength", EmsConstants.WORDLENGTH);
		view.addObject("Parity", EmsConstants.PARITY);
		view.addObject("StopBit", EmsConstants.STOPBIT);
		view.addObject("RegMapping", EmsConstants.REG_MAPPING);
		view.addObject("Port", new String[] {"COM5", "COM6"});
		view.addObject("Method", EmsConstants.READ_METHOD);
		
		return view;
	}
	
	@RequestMapping(value = "/ems/devicemanagement/removedevice", method = RequestMethod.POST)
	public @ResponseBody String removeDevice(@RequestParam("deviceId") @Valid long deviceId) {
		
		return this.deviceService.removeDevice(deviceId);
	}
	
	@RequestMapping(value = "/ems/devicemanagement/readdevice", method = RequestMethod.GET)
	public @ResponseBody String readDeviceDetails(@RequestParam("deviceId") @Valid long deviceId) {
		
		return this.deviceService.getDeviceDetails(deviceId);
	}
	
	@RequestMapping(value = "/ems/devicemanagement/updatedevice", method = RequestMethod.POST)
	public @ResponseBody String updateDevice(@RequestParam("deviceForm") @Valid String deviceForm) {
		
		return this.deviceService.updateDeviceDetail(deviceForm);
	}
	
	@RequestMapping(value = "/ems/devicemanagement/adddevice", method = RequestMethod.GET)
	public ModelAndView deviceModal() {
		
		ModelAndView view = new ModelAndView("devicemanagement/addDeviceView", "addForm", new DeviceForm());
		view.addObject("BaudRates", EmsConstants.BAUDRATES);
		view.addObject("WordLength", EmsConstants.WORDLENGTH);
		view.addObject("Parity", EmsConstants.PARITY);
		view.addObject("StopBit", EmsConstants.STOPBIT);
		view.addObject("RegMapping", EmsConstants.REG_MAPPING);
		
		String[] ports = EMSUtility.getAvailablePort();
		if(ports.length <= 0) {
			ports = new String[] {"COM5", "COM6"};
		}
		view.addObject("Port", ports);
		view.addObject("Method", EmsConstants.READ_METHOD);
		
		return view;
	}
	
	@RequestMapping(value = "/ems/devicemanagement/savedevice", method = RequestMethod.POST)
	public @ResponseBody String saveDevice(@RequestParam("deviceForm") @Valid String deviceForm) {
		
		return this.deviceService.saveDeviceDetail(deviceForm);
	}
	
}
