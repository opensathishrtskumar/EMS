package com.ems.UI.dto;

public class ChartDTO {
	private int maxAge;
	private String deviceName;
	private String[] seriesName;
	private long deviceUniqueId;
	private String xAxisName;
	private String yAxisName;
	private int interval;
	private DeviceDetailsDTO parameter;
	private boolean controlPanelRequired = true;

	public ChartDTO() {
	}

	public long getDeviceUniqueId() {
		return deviceUniqueId;
	}

	public ChartDTO setDeviceUniqueId(long deviceUniqueId) {
		this.deviceUniqueId = deviceUniqueId;
		return this;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public ChartDTO setMaxAge(int maxAge) {
		this.maxAge = maxAge;
		return this;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public ChartDTO setDeviceName(String deviceName) {
		this.deviceName = deviceName;
		return this;
	}

	public String[] getSeriesName() {
		return seriesName;
	}

	public ChartDTO setSeriesName(String[] seriesName) {
		this.seriesName = seriesName;
		return this;
	}

	public String getxAxisName() {
		return xAxisName;
	}

	public ChartDTO setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
		return this;
	}

	public String getyAxisName() {
		return yAxisName;
	}

	public ChartDTO setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
		return this;
	}

	public int getInterval() {
		return interval;
	}

	public ChartDTO setInterval(int interval) {
		this.interval = interval;
		return this;
	}

	public DeviceDetailsDTO getParameter() {
		return parameter;
	}

	public ChartDTO setParameter(DeviceDetailsDTO parameter) {
		this.parameter = parameter;
		this.deviceName = parameter.getDeviceName();
		this.deviceUniqueId = parameter.getUniqueId();
		return this;
	}

	public boolean isControlPanelRequired() {
		return controlPanelRequired;
	}

	public ChartDTO setControlPanelRequired(boolean controlPanelRequired) {
		this.controlPanelRequired = controlPanelRequired;
		return this;
	}
}
