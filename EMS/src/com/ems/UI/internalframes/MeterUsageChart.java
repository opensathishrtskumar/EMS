package com.ems.UI.internalframes;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.data.time.Millisecond;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ChartDTO;
import com.ems.UI.swingworkers.GroupedDeviceWorker;
import com.ems.response.handlers.ChartDataGenerator;

public class MeterUsageChart extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(MeterUsageChart.class);

	private TimeSeriesCollection dataset = null;
	private ChartDTO dto = null;;
	private XYItemRenderer renderer = null;
	private Timer timer;
	private GroupedDeviceWorker worker = null;
	private ChartDataGenerator dataGenerator;

	public MeterUsageChart(ChartDTO dto) {

		super(new BorderLayout());
		this.dto = dto;

		initialize();
	}

	public ChartDataGenerator getDataGenerator() {
		return dataGenerator;
	}

	public MeterUsageChart setDataGenerator(ChartDataGenerator dataGenerator) {
		this.dataGenerator = dataGenerator;
		return this;
	}

	public void initialize() {
		DateAxis domain = new DateAxis(dto.getyAxisName());
		NumberAxis range = new NumberAxis(dto.getxAxisName());

		domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
		
		//to set auto range not from zero
		range.setAutoRangeIncludesZero(false);
		range.setAutoRange(true);
		range.setMinorTickMarksVisible(true);
		
		//enable chart control panel
		this.dto.setControlPanelRequired(true);
		
		
		this.dataset = new TimeSeriesCollection();
		this.renderer = new XYLineAndShapeRenderer(true, false);
		Random random = new Random();

		String seriesNames[] = dto.getSeriesName();

		for (int i = 0; i < seriesNames.length; i++) {
			TimeSeries series = new TimeSeries(seriesNames[i]);
			series.setMaximumItemAge(dto.getMaxAge());
			dataset.addSeries(series);
			renderer.setSeriesStroke(i, new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
			renderer.setSeriesPaint(i,
					new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)).brighter());
		}

		XYPlot plot = new XYPlot(this.dataset, domain, range, renderer);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		domain.setAutoRange(true);
		domain.setLowerMargin(0.0);
		domain.setUpperMargin(0.0);
		domain.setTickLabelsVisible(true);

		range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		JFreeChart chart = new JFreeChart(dto.getDeviceName(), new Font("SansSerif", Font.BOLD, 24), plot, true);
		chart.setBackgroundPaint(Color.white);
		ChartPanel chartPanel = new ChartPanel(chart);
		/*chartPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
*/
		add(chartPanel, BorderLayout.CENTER);

		if (dto.isControlPanelRequired()) {
			ChartControlPanel controlPanel = new ChartControlPanel();
			add(new JScrollPane(controlPanel), BorderLayout.SOUTH);
		}
	}

	public MeterUsageChart start() {
		timer = new DataGenerator(dto.getInterval());
		timer.start();
		logger.trace("Data generator thread is started");
		return this;
	}

	public void stop() {
		if (timer != null) {
			logger.trace("Data generator thread is stopped");
			timer.stop();
		}

		if (worker != null) {
			worker.cancel(true);
			logger.debug("Device worker is stopped is stopped");
			worker = null;
		}
	}

	class ChartControlPanel extends JPanel implements ActionListener {
		private static final long serialVersionUID = 1L;

		public ChartControlPanel() {
			setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)),
					"Filter", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));

			initializeControl();
		}

		public void initializeControl() {

			int index = 0;
			for (String series : dto.getSeriesName()) {
				JCheckBox box = new JCheckBox(series);
				box.setActionCommand(String.valueOf(index++));
				box.addActionListener(this);
				box.setSelected(true);
				add(box);
			}
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			String commad = event.getActionCommand();

			if (commad != null && !commad.trim().isEmpty()) {
				int serieNumber = Integer.parseInt(commad);
				boolean visible = renderer.getItemVisible(serieNumber, 0);
				renderer.setSeriesVisible(serieNumber, new Boolean(!visible));
			}
		}
	}

	class DataGenerator extends Timer implements ActionListener {
		private static final long serialVersionUID = 1L;

		DataGenerator(int interval) {
			super(interval, null);
			Thread.currentThread().setName("DataGenerator" + UUID.randomUUID().toString());
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent event) {

			logger.trace("series names : {}", Arrays.toString(dto.getSeriesName()));

			if (dataGenerator != null) {
				Map<String, Number> readings = dataGenerator.getReadings();
				logger.trace("updating chart data.... ");
				for (String seriesName : dto.getSeriesName()) {
					dataset.getSeries(seriesName).addOrUpdate(new Second(), readings.get(seriesName));
				}
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		stop();
		logger.trace("Finalized Meter usage chart");
		super.finalize();
	}
}
