package org.ems.reports.summary;

import static com.ems.constants.QueryConstants.MAIN_INCOMER_DAILY;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.BODY_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.GA_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.KW;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.OFFICE;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.PAINT_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.PF;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.PRESS_SHOP;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.UTILITY;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VA1;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VA2;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VA3;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VAH;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VOLTAGE_BR;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VOLTAGE_RY;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VOLTAGE_YB;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.ems.config.listener.AppContextAware;
import org.ems.dao.PollingDetailsDAO;
import org.ems.reports.summary.finder.SummaryFinder;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.UI.dto.SettingsDTO;
import com.ems.constants.QueryConstants;
import com.ems.scheduler.FinalReportJob;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;

public class ConfigSummary extends AbstractDeviceSummary {

	private static final Logger logger = LoggerFactory.getLogger(ConfigSummary.class);

	@Override
	public void initialize(List<DeviceDetailsDTO> enabledDevices) throws Exception {
		this.enabledDevices = enabledDevices;
	}

	@Override
	public void findSummary(XSSFSheet template) throws Exception {
		logger.trace("Config summary is starting");

		long yesterday = LocalDateTime.now().minusDays(1).toDate().getTime();
		long startTime = Helper.getStartOfDay(yesterday);
		long endTime = Helper.getEndOfDay(yesterday);

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);
		String[] consumptionDevices = new String[]{ BODY_SHOP[1],PAINT_SHOP[1],GA_SHOP[1],UTILITY[1], OFFICE[1], PRESS_SHOP[1] };
		String[] paramRequired = new String[]{VA1 , VA2 , VA3 , PF};

		Map<String, Integer> mapper = new HashMap<>();
		mapper.put(consumptionDevices[0], 7);
		mapper.put(consumptionDevices[1], 8);
		mapper.put(consumptionDevices[2], 9);
		mapper.put(consumptionDevices[3], 10);
		mapper.put(consumptionDevices[4], 11);
		mapper.put(consumptionDevices[5], 12);

		//Find total usage for configured devices
		for(String deviceSettingName : consumptionDevices){
			List<PollingDetailDTO> consumptionList = dao
					.fetchMainIncomerDailySummary(MAIN_INCOMER_DAILY, new Object[]{deviceSettingName, startTime, endTime});

			long mainIncomerId = 0;
			Properties valueAndAddress = null;

			if(consumptionList != null && consumptionList.size() == 2){
				PollingDetailDTO min = consumptionList.get(0);//Min value
				PollingDetailDTO max = consumptionList.get(1);//Max value

				List<DeviceDetailsDTO> mainIncomer = this.enabledDevices.stream()
						.filter(device -> device.getUniqueId() == min.getDeviceuniqueid()).collect(Collectors.toList());

				if(mainIncomer != null && !mainIncomer.isEmpty()){
					mainIncomerId = mainIncomer.get(0).getUniqueId();

					Properties mappings = EMSUtility.loadProperties(mainIncomer.get(0).getMemoryMapping());
					valueAndAddress = EMSUtility.interChangeKeyValue(mappings);
					logger.trace("memory mappings of main incomer {}", valueAndAddress);
					String kwAddress = valueAndAddress.getProperty(KW);
					String vahAddress = valueAndAddress.getProperty(VAH);
					logger.trace("address of main incomer WH {} & VAH {}", kwAddress,vahAddress);
					Properties maxProperties = EMSUtility.loadProperties(max.getUnitresponse());
					Properties minProperties = EMSUtility.loadProperties(min.getUnitresponse());

					BigDecimal totalKWConsumed = new BigDecimal(maxProperties.getProperty(kwAddress))
							.subtract(new BigDecimal(minProperties.getProperty(kwAddress)));
					logger.trace("Min WH {} and Max WH {}"
							, maxProperties.getProperty(kwAddress) , minProperties.getProperty(kwAddress));

					BigDecimal totalVAHConsumed = new BigDecimal(maxProperties.getProperty(vahAddress))
							.subtract(new BigDecimal(minProperties.getProperty(vahAddress)));
					logger.trace("Min VAH {} and Max VAH {}"
							, maxProperties.getProperty(vahAddress) , minProperties.getProperty(vahAddress));

					logger.trace("Total consumption of main incomer WH {} & VAH {}",totalKWConsumed,totalVAHConsumed);

					Integer[] kwCoordinates = FinalReportJob.excelCoordinates.get(mapper.get(deviceSettingName));
					Integer[] vahCoordinates = FinalReportJob.excelCoordinates.get(mapper.get(deviceSettingName));

					template.getRow(kwCoordinates[0]).getCell(kwCoordinates[1]).setCellValue((long)totalKWConsumed.doubleValue() / 1000);
					template.getRow(vahCoordinates[0] + 1).getCell(vahCoordinates[1]).setCellValue((long)totalVAHConsumed.doubleValue() / 1000);


					SummaryFinder summaryFinder = new MainSummaryFinder(valueAndAddress, paramRequired);
					summaryFinder = dao.calculateSummary(QueryConstants.FETCH_DEVICE_READINGS
							, new Object[]{mainIncomerId , startTime , endTime , 0 , 0 , 0 }, summaryFinder);
					logger.debug("Config Summary Main incommer - sub transformer" + EMSUtility.convertObjectToJSONString(summaryFinder.getResponse()));

					Map<String, MeterSummary> summary = summaryFinder.getResponse();
					Integer[] coordinates = FinalReportJob.excelCoordinates.get(mapper.get(deviceSettingName));

					String value = EMSUtility.createReportStringSingleKXXMax(summary.get(paramRequired[0]),
							summary.get(paramRequired[1]), summary.get(paramRequired[2]));
					template.getRow(coordinates[0] + 13).getCell(coordinates[1]).setCellValue(value.trim());

					//Minimum PF value
					value = EMSUtility.createReportStringMinMax(true, false,summary.get(paramRequired[3]));
					template.getRow(coordinates[0] + 14).getCell(coordinates[1]).setCellValue(value.trim());
				}
			}
		}

		//Find min & max of configured values

		consumptionDevices = new String[]{ BODY_SHOP[2],PAINT_SHOP[2],GA_SHOP[2],UTILITY[2], OFFICE[2] };
		paramRequired = new String[]{VOLTAGE_RY , VOLTAGE_YB , VOLTAGE_BR , PF};

		List<SettingsDTO> settings = dao.fetchSettings();
		mapper.clear();
		mapper.put(consumptionDevices[0], 19);
		mapper.put(consumptionDevices[1], 20);
		mapper.put(consumptionDevices[2], 21);
		mapper.put(consumptionDevices[3], 22);
		mapper.put(consumptionDevices[4], 23);

		for(String deviceKey : consumptionDevices){
			//Get the device id against key from settings
			List<SettingsDTO> filtered = settings.stream()
					.filter(setting -> setting.getSkey().equalsIgnoreCase(deviceKey))
					.collect(Collectors.toList());
			logger.trace("Filtered settings : {}  against key : {} ", EMSUtility.convertObjectToJSONString(filtered) , deviceKey);

			if(filtered != null && !filtered.isEmpty()){

				//FIlter selected device from enabled devices - load prop and so on
				List<DeviceDetailsDTO> filteredDevice = this.enabledDevices.stream()
						.filter(device -> device.getUniqueId() == Long.parseLong(filtered.get(0).getSvalue()))
						.collect(Collectors.toList());
				logger.debug("Filtered devices from settings id - sub transformer: {} ", EMSUtility.convertObjectToJSONString(filteredDevice));

				if(filteredDevice != null && !filteredDevice.isEmpty()){

					Properties mappings = EMSUtility.loadProperties(filteredDevice.get(0).getMemoryMapping());
					Properties valueAndAddress = EMSUtility.interChangeKeyValue(mappings);

					SummaryFinder summaryFinder = new MainSummaryFinder(valueAndAddress, paramRequired);
					summaryFinder = dao.calculateSummary(QueryConstants.FETCH_DEVICE_READINGS
							, new Object[]{filteredDevice.get(0).getUniqueId() , startTime , endTime , 0 , 0 , 0 }, summaryFinder);
					logger.debug("Config Summary Main incommer - sub transformer" + EMSUtility.convertObjectToJSONString(summaryFinder.getResponse()));

					Map<String, MeterSummary> summary = summaryFinder.getResponse();
					Integer[] coordinates = FinalReportJob.excelCoordinates.get(mapper.get(deviceKey));

					String value = EMSUtility.createReportStringMax(summary.get(paramRequired[0]),
							summary.get(paramRequired[1]), summary.get(paramRequired[2]));
					template.getRow(coordinates[0]).getCell(coordinates[1]).setCellValue(value.trim());

					value = EMSUtility.createReportStringMin(summary.get(paramRequired[0]),
							summary.get(paramRequired[1]), summary.get(paramRequired[2]));
					template.getRow(coordinates[0] + 1).getCell(coordinates[1]).setCellValue(value.trim());
				}
			}
		}
	}

	@Override
	public void completed() throws Exception {
		logger.trace("Config summary worker completed");
	}
}
