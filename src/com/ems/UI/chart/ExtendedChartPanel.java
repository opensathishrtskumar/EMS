package com.ems.UI.chart;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedChartPanel extends MyChartPanel implements ChangeListener {
	private static final Logger logger = LoggerFactory
			.getLogger(ExtendedChartPanel.class);
	private static final long serialVersionUID = 3869448791239244939L;
	JScrollBar scroller;
	SlidingCategoryDataset slidingdataset;
	private static Random random = new Random();
	private static Color[] colors = {Color.BLUE, Color.CYAN, Color.WHITE,
			Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.GREEN,
			Color.YELLOW };

	private CategoryItemRenderer categoryRenderer;
	
	public ExtendedChartPanel(String charTitle, CategoryDataset dataset) {
		super(new BorderLayout());
		this.slidingdataset = new SlidingCategoryDataset(dataset, 0, 10);

		JFreeChart chart = createChart(this.slidingdataset, charTitle);
		addChart(chart);
		
		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		categoryRenderer = plot.getRenderer();
		
		ChartPanel cp1 = new ChartPanel(chart);
		cp1.setPreferredSize(new Dimension(400, 400));
		logger.debug("dataset row count : {}", dataset.getColumnCount());
		int count = dataset.getColumnCount();

		int scrollStart = (count == 0 || count < 10) ? count : 10;
		int scrollEnd = count;

		this.scroller = new JScrollBar(SwingConstants.HORIZONTAL, 0,
				scrollStart, 0, scrollEnd);
		add(cp1);
		this.scroller.getModel().addChangeListener(this);
		JPanel scrollPanel = new JPanel(new BorderLayout());
		scrollPanel.add(this.scroller);
		scrollPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		scrollPanel.setBackground(Color.white);
		add(scrollPanel, BorderLayout.SOUTH);
	}

	private static JFreeChart createChart(CategoryDataset dataset,
			String charTitle) {

		JFreeChart chart = ChartFactory.createLineChart(charTitle, // chart
																	// title
				"Time", // domain axis label
				"Readings", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setMaximumCategoryLabelWidthRatio(0.8f);
		domainAxis.setLowerMargin(0.02);
		domainAxis.setUpperMargin(0.02);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		// range will be selected automatically by chart
		//rangeAxis.setRange(100.0, 3000.0);

		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
				.getRenderer();
		renderer.setBaseSeriesVisible(true);

		GradientPaint gp0 = new GradientPaint(0.0f, 0.0f, Color.blue, 0.0f,
				0.0f, new Color(0, 0, 64));
		renderer.setSeriesPaint(0, gp0);
		renderer.setSeriesStroke(0, new BasicStroke(3.0f));
		renderer.setBaseLegendTextFont(new Font("Helvetica", Font.BOLD, 11));

		int seriesCount = dataset.getRowCount();
		for (int i = 0; i < seriesCount; i++) {
			renderer.setSeriesStroke(i, new BasicStroke(2.0f));
			
			if(colors.length > i) 
				renderer.setSeriesPaint(i, colors[i].brighter());
			else {
				Color randColor = Color.getHSBColor(random.nextFloat(), random.nextFloat(),
						random.nextFloat());
				renderer.setSeriesPaint(i, randColor.brighter());
			}
		}

		plot.setOutlinePaint(Color.BLACK);
		plot.setOutlineStroke(new BasicStroke(1.0f));
		plot.setBackgroundPaint(Color.DARK_GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);

		return chart;
	}

	public void stateChanged(ChangeEvent e) {
		this.slidingdataset.setFirstCategoryIndex(this.scroller.getValue());
	}

	public CategoryItemRenderer getCategoryRenderer() {
		return categoryRenderer;
	}
	
}
