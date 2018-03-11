package org.ems.reports.summary;

import static com.ems.constants.QueryConstants.MAIN_INCOMER_DAILY;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.KW;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.PF;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VA1;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VA2;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VA3;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VAH;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VOLTAGE_BR;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VOLTAGE_RY;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.VOLTAGE_YB;

import java.math.BigDecimal;
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
import com.ems.scheduler.FinalReportJob.FinalReportConstants;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;

public class MainIncomerSummary extends AbstractDeviceSummary {

	private static final Logger logger = LoggerFactory.getLogger(MainIncomerSummary.class);
	private static final String[] paramRequired = new String[] { VOLTAGE_RY, VOLTAGE_YB, VOLTAGE_BR, VA1, VA2, VA3,
			PF };
	@Override
	public void initialize(List<DeviceDetailsDTO> enabledDevices) throws Exception {
		this.enabledDevices = enabledDevices;
	}

	@Override
	public void findSummary(XSSFSheet template) throws Exception {

		// 1. Main Incomer - total consumption values - KWH and KVAH
		long yesterday = LocalDateTime.now().minusDays(1).toDate().getTime();
		long startTime = Helper.getStartOfDay(yesterday);
		long endTime = Helper.getEndOfDay(yesterday);

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

		Integer[] kwCoordinates = FinalReportJob.excelCoordinates.get(1);
		Integer[] vahCoordinates = FinalReportJob.excelCoordinates.get(2);
		String kwhAddress = null;

		template.getRow(0).getCell(2).setCellValue("Plant Unit Consumption Data - Isuzu Motors India "
				+ EMSUtility.getFormattedTime(yesterday, "dd.MM.yy"));

		List<PollingDetailDTO> mainIncomerConsumptionList = dao.fetchMainIncomerDailySummary(MAIN_INCOMER_DAILY,
				new Object[] { FinalReportConstants.MAIN_INCOMER, startTime, endTime });
		
		logger.debug("Main incomer max min value {} ",
				EMSUtility.convertObjectToJSONString(mainIncomerConsumptionList));

		long mainIncomerId = 0;
		Properties valueAndAddress = null;

		if (mainIncomerConsumptionList != null && mainIncomerConsumptionList.size() == 2) {
			PollingDetailDTO min = mainIncomerConsumptionList.get(0);// Min value
			PollingDetailDTO max = mainIncomerConsumptionList.get(1);// Max value

			List<DeviceDetailsDTO> mainIncomer = this.enabledDevices.stream()
					.filter(device -> device.getUniqueId() == min.getDeviceuniqueid()).collect(Collectors.toList());

			if (mainIncomer != null && !mainIncomer.isEmpty()) {
				mainIncomerId = mainIncomer.get(0).getUniqueId();

				Properties mappings = EMSUtility.loadProperties(mainIncomer.get(0).getMemoryMapping());
				valueAndAddress = EMSUtility.interChangeKeyValue(mappings);
				logger.trace("memory mappings of main incomer {}", valueAndAddress);
				String kwAddress = kwhAddress = valueAndAddress.getProperty(KW);
				String vahAddress = valueAndAddress.getProperty(VAH);
				logger.trace("address of main incomer WH {} & VAH {}", kwAddress, vahAddress);
				Properties maxProperties = EMSUtility.loadProperties(max.getUnitresponse());
				Properties minProperties = EMSUtility.loadProperties(min.getUnitresponse());

				BigDecimal totalKWConsumed = new BigDecimal(maxProperties.getProperty(kwAddress))
						.subtract(new BigDecimal(minProperties.getProperty(kwAddress)));

				BigDecimal totalVAHConsumed = new BigDecimal(maxProperties.getProperty(vahAddress))
						.subtract(new BigDecimal(minProperties.getProperty(vahAddress)));
				logger.trace("Total consumption of main incomer WH {} & VAH {}", totalKWConsumed, totalVAHConsumed);

				template.getRow(kwCoordinates[0]).getCell(kwCoordinates[1])
						.setCellValue((long) totalKWConsumed.doubleValue() / 1000);
				template.getRow(vahCoordinates[0]).getCell(vahCoordinates[1])
						.setCellValue((long) totalVAHConsumed.doubleValue() / 1000);
			}
		}

		// 2. Max & Min Voltage & Maximum KVA & Minimum PF
		// Process when values are available
		if (mainIncomerId != 0) {
			SummaryFinder summaryFinder = new MainSummaryFinder(valueAndAddress, paramRequired);

			summaryFinder = dao.calculateSummary(QueryConstants.FETCH_DEVICE_READINGS,
					new Object[] { mainIncomerId, startTime, endTime, 0, 0, 0 }, summaryFinder);
			logger.trace("Summary Main incommer " + EMSUtility.convertObjectToJSONString(summaryFinder.getResponse()));

			// Abnormal PF observed - MAX only

			Map<String, MeterSummary> summary = summaryFinder.getResponse();
			String value = EMSUtility.createReportStringMinMax(true, false, summary.get(paramRequired[6]));

			Integer[] coordinates = FinalReportJob.excelCoordinates.get(4);
			template.getRow(coordinates[0]).getCell(coordinates[1]).setCellValue(value.trim());

			// Abnormal KVA observed - MAX only
			value = EMSUtility.createReportStringSingleKXXMax1(summary.get(paramRequired[3]),
					summary.get(paramRequired[4]), summary.get(paramRequired[5]));

			coordinates = FinalReportJob.excelCoordinates.get(5);
			template.getRow(coordinates[0]).getCell(coordinates[1]).setCellValue(value.trim());

			// Abnormal Voltage observed - MAX only
			value = EMSUtility.createReportStringMax(summary.get(paramRequired[0]), summary.get(paramRequired[1]),
					summary.get(paramRequired[2]));
		


			coordinates = FinalReportJob.excelCoordinates.get(6);
			template.getRow(coordinates[0]).getCell(coordinates[1]).setCellValue(value.trim());

			// Find monthly summary for main incomer
			LocalDateTime time = LocalDateTime.now();
			LocalDateTime modified = time.withDate(time.getYear(), time.getMonthOfYear(), 1);

			startTime = Helper.getStartOfDay(modified.toDate().getTime());
			endTime = Helper.getEndOfDay(time.toDate().getTime());

			List<PollingDetailDTO> consumptionList = dao.fetchMainIncomerDailySummary(
					QueryConstants.MONTHLY_CONSUMPTION, new Object[] { FinalReportConstants.MAIN_INCOMER, startTime,
							endTime, FinalReportConstants.MAIN_INCOMER, startTime, endTime });
			logger.debug("Monthly summary for main incomer {}", EMSUtility.convertObjectToJSONString(consumptionList));

			if (consumptionList != null && consumptionList.size() > 1) {
				// finding min and max from two schema so take first and last reocrd for max and
				// min respectively
				PollingDetailDTO max = consumptionList.get(0);// Max value
				PollingDetailDTO min = consumptionList.get(consumptionList.size() - 1);// Min value

				List<DeviceDetailsDTO> mainIncomer = this.enabledDevices.stream()
						.filter(device -> device.getUniqueId() == min.getDeviceuniqueid()).collect(Collectors.toList());

				Properties mappings = EMSUtility.loadProperties(mainIncomer.get(0).getMemoryMapping());
				valueAndAddress = EMSUtility.interChangeKeyValue(mappings);
				logger.debug("monthly memory mappings of main incomer {}", valueAndAddress);
				String kwAddress = valueAndAddress.getProperty(KW);
				String vahAddress = valueAndAddress.getProperty(VAH);
				logger.debug("monthly address of main incomer WH {} & VAH {}", kwAddress, vahAddress);
				Properties maxProperties = EMSUtility.loadProperties(max.getUnitresponse());
				Properties minProperties = EMSUtility.loadProperties(min.getUnitresponse());

				BigDecimal totalKWConsumed = new BigDecimal(maxProperties.getProperty(kwAddress))
						.subtract(new BigDecimal(minProperties.getProperty(kwAddress)));
				logger.debug("monthly Min WH {} and Max WH {}", maxProperties.getProperty(kwAddress),
						minProperties.getProperty(kwAddress));

				BigDecimal totalVAHConsumed = new BigDecimal(maxProperties.getProperty(vahAddress))
						.subtract(new BigDecimal(minProperties.getProperty(vahAddress)));
				logger.trace("monthly Min VAH {} and Max VAH {}", maxProperties.getProperty(vahAddress),
						minProperties.getProperty(vahAddress));

				logger.debug("Monthly consumption of main incomer WH {} & VAH {}", totalKWConsumed, totalVAHConsumed);

				kwCoordinates = FinalReportJob.excelCoordinates.get(24);
				vahCoordinates = FinalReportJob.excelCoordinates.get(25);

				template.getRow(kwCoordinates[0]).getCell(kwCoordinates[1])
						.setCellValue((long) totalKWConsumed.doubleValue() / 1000);
				template.getRow(vahCoordinates[0]).getCell(vahCoordinates[1])
						.setCellValue((long) totalVAHConsumed.doubleValue() / 1000);
			}

		} else {
			logger.trace(" Total consumption could not be calculated since no min & max found");
		}

		// Code to find Total power failure - add time difference where continuous 0
		// recorded

		if (kwhAddress != null) {
			SummaryFinder summaryFinder = new FailureFinder(kwhAddress);
			startTime = Helper.getStartOfDay(yesterday);
			endTime = Helper.getEndOfDay(yesterday);

			summaryFinder = dao.calculateSummary(QueryConstants.FETCH_DEVICE_READINGS,
					new Object[] { mainIncomerId, startTime, endTime, 0, 0, 0 }, summaryFinder);
			FailureFinder failure = (FailureFinder) summaryFinder;

			// code to write failure time in excel
			Integer[] failureCoordinates = FinalReportJob.excelCoordinates.get(26);
			template.getRow(failureCoordinates[0]).getCell(failureCoordinates[1])
					.setCellValue(failure.getFailure() + " Minutes");
		}

	}

	@Override
	public void completed() throws Exception {
		logger.trace(" Main incomer calculated ");
	}
}
