package com.ems.UI.chart;

import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyChartPanel extends JPanel {
	private static final long serialVersionUID = 538751291954472436L;
	private static final Logger logger = LoggerFactory.getLogger(MyChartPanel.class);
	List charts;

	public MyChartPanel(LayoutManager layout) {
		super(layout);
		this.charts = new java.util.ArrayList();
	}

	public void addChart(JFreeChart chart) {
		logger.debug(" Chart added...");
		this.charts.add(chart);
	}

	public JFreeChart[] getCharts() {
		int chartCount = this.charts.size();
		JFreeChart[] charts = new JFreeChart[chartCount];
		for (int i = 0; i < chartCount; i++) {
			charts[i] = (JFreeChart) this.charts.get(i);
		}
		return charts;
	}

}