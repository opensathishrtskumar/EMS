package org.ems.reports.summary;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.ems.reports.summary.finder.SummaryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.util.EMSUtility;

public class MainSummaryFinder extends SummaryFinder {
	private static final Logger logger = LoggerFactory.getLogger(MainSummaryFinder.class);

	private Properties interchangedProp = null;
	private Pattern[] patterns = null;
	private Map<String, MeterSummary> map = new HashMap<>();
	private String[] paramRequired = null;

	public MainSummaryFinder(Properties interchangedProp, String[] paramRequired){
		this.paramRequired = paramRequired;
		this.interchangedProp = interchangedProp;
		logger.trace("Interchanged properties {} param requierd {} ", interchangedProp, paramRequired);
		init();
	}

	public MainSummaryFinder(Properties interchangedProp) {
		this(interchangedProp , new String[]{});
	}

	private void init(){
		patterns = new Pattern[paramRequired.length];
		for(int i = 0 ;i < paramRequired.length ; i++){
			patterns[i] = EMSUtility.getValueByAddressPattern(this.interchangedProp.getProperty(paramRequired[i]));
			map.put(paramRequired[i], new MeterSummary("0.0","0.0",0,0));
		}
	}

	@Override
	public void process(ResultSet rs) throws Exception {

		String response = rs.getString("unitresponse");

		for(int i = 0 ;i < paramRequired.length ; i++){
			String value = EMSUtility.getValueByAddress(response, null, patterns[i]);
			setMinMax(value, paramRequired[i], rs);
		}
	}

	@Override
	public Map<String, MeterSummary> getResponse() {
		return map;
	}

	private void setMinMax(String value, String mappingName, ResultSet rs) throws SQLException{
		MeterSummary values = map.get(mappingName);
		long time = rs.getLong("polledon");

		if(values.isFirst()){
			values.setMinValue(value)
			.setMaxValue(value)
			.setMaxTime(time)
			.setMinTime(time)
			.setParamName(mappingName);
			values.setFirst(false);
		}
		
		if(!value.equals("0.00")) {
			values.setMinValue(new BigDecimal(value).min(new BigDecimal(values.getMinValue())).toString());
			values.setMaxValue(new BigDecimal(value).max(new BigDecimal(values.getMaxValue())).toString());
			values.setMinTime( value.equals(values.getMinValue()) ? time : values.getMinTime());
			values.setMaxTime( value.equals(values.getMaxValue()) ? time : values.getMaxTime());
		}
	}
}