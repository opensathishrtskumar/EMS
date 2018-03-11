package org.ems.reports.summary;

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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.ems.config.listener.AppContextAware;
import org.ems.db.repo.PollingDetailsDAO;
import org.ems.reports.summary.finder.SummaryFinder;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.QueryConstants;
import com.ems.scheduler.FinalReportJob;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;

public class MonthlySummary extends AbstractDeviceSummary {

	private static final Logger logger = LoggerFactory.getLogger(MonthlySummary.class);

	@Override
	public void initialize(List<DeviceDetailsDTO> enabledDevices) throws Exception {
		this.enabledDevices = enabledDevices;
	}

	@Override
	public void findSummary(XSSFSheet template) throws Exception {

		LocalDateTime time = LocalDateTime.now();
		LocalDateTime modified = time.withDate(time.getYear(),
				time.getDayOfMonth() == 1 ? time.getMonthOfYear() - 1 : time.getMonthOfYear(), 1);

		long startTime = Helper.getStartOfDay(modified.toDate().getTime());
		long endTime = Helper.getEndOfDay(
				time.getDayOfMonth() == 1 ? time.minusDays(1).toDate().getTime() : time.toDate().getTime());

		/*
		 * long endTime = Helper.getEndOfDay(time.toDate().getTime());
		 */

		logger.info("monthly report start time {} : end time {}",
				EMSUtility.getFormattedTime(startTime, EMSUtility.DASHBOARD_POLLED_FMT),
				EMSUtility.getFormattedTime(endTime, EMSUtility.DASHBOARD_POLLED_FMT));

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

		String[] consumptionDevices = new String[] { BODY_SHOP[1], PAINT_SHOP[1], GA_SHOP[1], UTILITY[1], OFFICE[1],
				PRESS_SHOP[1] };

		Map<String, Integer> mapper = new HashMap<>();
		mapper.put(consumptionDevices[0], 13);
		mapper.put(consumptionDevices[1], 14);
		mapper.put(consumptionDevices[2], 15);
		mapper.put(consumptionDevices[3], 16);
		mapper.put(consumptionDevices[4], 17);
		mapper.put(consumptionDevices[5], 18);

		String[] paramRequired = new String[] { VA1, VA2, VA3, PF };

		for (String deviceSettingName : consumptionDevices) {

			List<PollingDetailDTO> consumptionList = dao.fetchMainIncomerDailySummary(
					QueryConstants.MONTHLY_CONSUMPTION,
					new Object[] { deviceSettingName, startTime, endTime, deviceSettingName, startTime, endTime });

			long mainIncomerId = 0;
			Properties valueAndAddress = null;

			if (consumptionList != null && consumptionList.size() > 1) {
				// finding min and max from two schema so take first and last reocrd for max and
				// min respectively
				PollingDetailDTO max = consumptionList.get(0);// Max value

				PollingDetailDTO min = consumptionList.get(consumptionList.size() - 1);// Min value

				List<DeviceDetailsDTO> mainIncomer = this.enabledDevices.stream()
						.filter(device -> device.getUniqueId() == min.getDeviceuniqueid()).collect(Collectors.toList());

				if (mainIncomer != null && !mainIncomer.isEmpty()) {
					mainIncomerId = mainIncomer.get(0).getUniqueId();

					Properties mappings = EMSUtility.loadProperties(mainIncomer.get(0).getMemoryMapping());
					valueAndAddress = EMSUtility.interChangeKeyValue(mappings);
					logger.trace("memory mappings of main incomer {}", valueAndAddress);
					String kwAddress = valueAndAddress.getProperty(KW);
					String vahAddress = valueAndAddress.getProperty(VAH);

					logger.trace("address of main incomer WH {} & VAH {}", kwAddress, vahAddress);
					Properties maxProperties = EMSUtility.loadProperties(max.getUnitresponse());
					Properties minProperties = EMSUtility.loadProperties(min.getUnitresponse());

					BigDecimal totalKWConsumed = new BigDecimal(maxProperties.getProperty(kwAddress))
							.subtract(new BigDecimal(minProperties.getProperty(kwAddress)));
					logger.trace("Min WH {} and Max WH {}", maxProperties.getProperty(kwAddress),
							minProperties.getProperty(kwAddress));

					BigDecimal totalVAHConsumed = new BigDecimal(maxProperties.getProperty(vahAddress))
							.subtract(new BigDecimal(minProperties.getProperty(vahAddress)));

					logger.trace("Min VAH {} and Max VAH {}", maxProperties.getProperty(vahAddress),
							minProperties.getProperty(vahAddress));

					logger.trace("Total consumption of main incomer WH {} & VAH {}", totalKWConsumed, totalVAHConsumed);

					Integer[] kwCoordinates = FinalReportJob.excelCoordinates.get(mapper.get(deviceSettingName));
					Integer[] vahCoordinates = FinalReportJob.excelCoordinates.get(mapper.get(deviceSettingName));

					template.getRow(kwCoordinates[0]).getCell(kwCoordinates[1])
							.setCellValue((long) totalKWConsumed.doubleValue() / 1000);
					template.getRow(vahCoordinates[0] + 1).getCell(vahCoordinates[1])
							.setCellValue((long) totalVAHConsumed.doubleValue() / 1000);

					// Find summary for each device
					SummaryFinder summaryFinder = new MainSummaryFinder(valueAndAddress, paramRequired);

					summaryFinder = dao.calculateSummary(QueryConstants.FETCH_DEVICE_READINGS,
							new Object[] { mainIncomerId, startTime, endTime, mainIncomerId, startTime, endTime },
							summaryFinder);
					logger.trace(
							"Summary Finding " + EMSUtility.convertObjectToJSONString(summaryFinder.getResponse()));

					Map<String, MeterSummary> summary = summaryFinder.getResponse();
					String value = EMSUtility.createReportStringMinMax(true, false, summary.get(paramRequired[3]));

					template.getRow(kwCoordinates[0] + 3).getCell(kwCoordinates[1]).setCellValue(value.trim());

					value = EMSUtility.createReportStringSingleKXXMax1(summary.get(paramRequired[0]),
							summary.get(paramRequired[1]), summary.get(paramRequired[2]));

					template.getRow(kwCoordinates[0] + 2).getCell(kwCoordinates[1]).setCellValue(value.trim());

				}
			}
		}
	}

	@Override
	public void completed() throws Exception {
		logger.trace("Monthly summary calculated");
	}

}
