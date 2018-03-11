package com.ems.concurrency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.util.EMSUtility;
import com.ems.util.Helper;


import java.awt.Font;
import java.text.SimpleDateFormat;
import javax.swing.JFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jfree.data.time.*;
public class TestClass {


private static final Logger logger = LoggerFactory.getLogger(TestClass.class);
/*
	 * private static final byte[] secureBytes = { 68, 120, 66, 64, 50, 48, 49, 55,
	 * 48, 48, 49 };
	 */


private static final long serialVersionUID = 50L;


 public static void main(String[] args)  throws Exception{
	        String[] timeStamps = new String[]{"26.08.2010","27.08.2010","27.08.2010","28.08.2010"};
	        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
	        XYDataset dataset;
	        //method one: DefaultXYDataset
	        /*dataset = new DefaultXYDataset();
	        double[][] data = new double[2][timeStamps.length];
	        for(int i = 0; i < timeStamps.length; i++){
	            data[0][i] = df.parse(timeStamps[i]).getTime();
	            data[1][i] = i;
	        }
	        ((DefaultXYDataset)dataset).addSeries("DefaultXYDataset",data);*/     
	        //method two: XYSeriesCollection
	        dataset = new XYSeriesCollection();
	        XYSeries series = new XYSeries("XYSeriesCollection", false, true); //use the allowDuplicateValues flag!
	        for(int i = 0; i < timeStamps.length; i++){
	            series.add(df.parse(timeStamps[i]).getTime(),i);
	        }
	        ((XYSeriesCollection)dataset).addSeries(series);
	        //method three: TimeSeriesCollection, but it doesn´t accept duplicates
	        /*dataset = new TimeSeriesCollection();
	        TimeSeries series = new TimeSeries("TimeSeriesCollection");
	        for(int i = 0; i < timeStamps.length; i++){
	            series.addOrUpdate(new Day(df.parse(timeStamps[i])),i);
	        }
	        ((TimeSeriesCollection)dataset).addSeries(series);*/
	        DateAxis xAxis = new DateAxis("Date");
	        xAxis.setDateFormatOverride(df);
	        xAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 1));//show only one tick mark per day though there would be place for more tick marks
	        ValueAxis yAxis = new NumberAxis("Values");
	        XYItemRenderer renderer = new XYLineAndShapeRenderer();
	        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
	        JFreeChart chart = new JFreeChart("XYPlot Demo", new Font("Tahoma", 2, 18), plot, true);
	        JFrame frame = new JFrame("XY Plot Demo");
	        frame.setContentPane(new ChartPanel(chart));
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.pack();
	        frame.setVisible(true);
	    }

	

	public static long[] dateRange(LocalDate today) {

		int month = today.getMonthOfYear();
		LocalDate yesterday = today.minusDays(1);

		long[] timerange = new long[2];

		if (month != yesterday.getMonthOfYear()) {
			today = today.withMonthOfYear(yesterday.getMonthOfYear());
		}

		timerange[0] = Helper.getStartOfDay(today.withDayOfMonth(1).toDate().getTime());
		timerange[1] = Helper.getEndOfDay(today.withDayOfMonth(yesterday.getDayOfMonth()).toDate().getTime());

		timerange[1] += 1000000;

		return timerange;
	}

	public static void dateRangePrinter(long[] dateRange) {
		ArrayList<String> dates = new ArrayList<>();

		Date start = new Date(dateRange[0]);
		Date end = new Date(dateRange[1] - 1000000);

		for (int i = start.getDate(); i <= end.getDate(); i++) {
			start.setDate(i);
			dates.add(EMSUtility.getFormattedTime(start.getTime(), EMSUtility.DD_MM_YY));
		}

		System.out.println("thirumalai"+start);
		
		System.out.println("htuhru"+start);
	}

}
