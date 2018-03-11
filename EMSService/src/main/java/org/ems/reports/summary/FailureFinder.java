package org.ems.reports.summary;

import java.sql.ResultSet;
import java.util.Map;
import java.util.regex.Pattern;

import org.ems.reports.summary.finder.SummaryFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.util.EMSUtility;

public class FailureFinder extends SummaryFinder {

	private static final Logger logger = LoggerFactory.getLogger(FailureFinder.class);

	private long failure = 0;
	private long previousTime = 0;

	private String previousValue = null;

	private Pattern failurePattern = null;

	public FailureFinder(String failureFindingAddress) {
		this.failurePattern = EMSUtility.getValueByAddressPattern(failureFindingAddress);
	}

	public long getFailure() {
		return failure;
	}

	@Override
	public void process(ResultSet rs) throws Exception {
		long time = rs.getLong("polledon");
		String value = EMSUtility.getValueByAddress(rs.getString("unitresponse"), null, failurePattern);

		calculateFailureTime(value, time);
	}

	private void calculateFailureTime(String currentValue, long currentTime) {
		// Find continuous failure and accumulate timeframe
		
		if (previousValue == null)
			previousValue = currentValue;
		
	}

	@Override
	public Map<String, MeterSummary> getResponse() {
		return null;
	}

}
