package org.ems.reports.summary;
public class MeterSummary {
	private String minValue;
	private String maxValue;
	private long minTime;
	private long maxTime;
	private String paramName;
	private boolean first = true;

	public MeterSummary(String minValue, String maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public MeterSummary(String minValue, String maxValue, long minTime, long maxTime) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}

	public String getMinValue() {
		return minValue;
	}
	public MeterSummary setMinValue(String minValue) {
		this.minValue = minValue;
		return this;
	}
	public String getMaxValue() {
		return maxValue;
	}
	public MeterSummary setMaxValue(String maxValue) {
		this.maxValue = maxValue;
		return this;
	}
	public long getMinTime() {
		return minTime;
	}
	public MeterSummary setMinTime(long minTime) {
		this.minTime = minTime;
		return this;
	}
	public long getMaxTime() {
		return maxTime;
	}
	public MeterSummary setMaxTime(long maxTime) {
		this.maxTime = maxTime;
		return this;
	}
	public String getParamName() {
		return paramName;
	}
	public MeterSummary setParamName(String paramName) {
		this.paramName = paramName;
		return this;
	}
	public boolean isFirst() {
		return first;
	}
	public void setFirst(boolean first) {
		this.first = first;
	}
}