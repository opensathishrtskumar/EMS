package org.ems.reports.summary;

import static com.ems.constants.QueryConstants.MAIN_INCOMER_DAILY;
import static com.ems.scheduler.FinalReportJob.FinalReportConstants.KW;

import java.math.BigInteger;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.ems.config.listener.AppContextAware;
import org.ems.db.repo.PollingDetailsDAO;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.scheduler.FinalReportJob;
import com.ems.scheduler.FinalReportJob.FinalReportConstants;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;


public class DGSummary extends AbstractDeviceSummary {
	private static final Logger logger = LoggerFactory.getLogger(DGSummary.class);

	@Override
	public void initialize(List<DeviceDetailsDTO> enabledDevices) throws Exception {
		this.enabledDevices = enabledDevices;
	}

	@Override
	public void findSummary(XSSFSheet template) throws Exception {

		//1. Main Incomer - total consumption values - KWH and KVAH
		long yesterday = LocalDateTime.now().minusDays(1).toDate().getTime();
		long startTime = Helper.getStartOfDay(yesterday);
		long endTime = Helper.getEndOfDay(yesterday);

		PollingDetailsDAO dao = AppContextAware.getContext().getBean(PollingDetailsDAO.class);

		List<PollingDetailDTO> dgConsumptionList = dao
				.fetchMainIncomerDailySummary(MAIN_INCOMER_DAILY, new Object[]{FinalReportConstants.DG_INCOMER, startTime, endTime});
		logger.debug(" DG Report data from DB : {} ", EMSUtility.convertObjectToJSONString(dgConsumptionList));

		if(dgConsumptionList != null && dgConsumptionList.size() == 2){

			PollingDetailDTO min = dgConsumptionList.get(0);//Min value
			PollingDetailDTO max = dgConsumptionList.get(1);//Max value

			List<DeviceDetailsDTO> mainIncomer = getEnabledDevices().stream()
					.filter(device -> device.getUniqueId() == min.getDeviceuniqueid()).collect(Collectors.toList());

			if(mainIncomer != null && !mainIncomer.isEmpty()){
				Properties mappings = EMSUtility.loadProperties(mainIncomer.get(0).getMemoryMapping());
				Properties valueAndAddress = EMSUtility.interChangeKeyValue(mappings);
				String kwAddress = valueAndAddress.getProperty(KW);

				Properties maxProperties = EMSUtility.loadProperties(max.getUnitresponse());
				Properties minProperties = EMSUtility.loadProperties(min.getUnitresponse());

				String totalKWConsumed = new BigInteger(maxProperties.getProperty(kwAddress))
						.subtract(new BigInteger(minProperties.getProperty(kwAddress))).divide(new BigInteger("1000")).toString();
				logger.debug(" total WH value  for DG : {} ", totalKWConsumed);

				Integer[] kwCoordinates = FinalReportJob.excelCoordinates.get(3);
				template.getRow(kwCoordinates[0]).getCell(kwCoordinates[1]).setCellValue(totalKWConsumed);
			}
		}
	}

	@Override
	public void completed() throws Exception {
		logger.debug(" DG Summary completed ");
	}
}
