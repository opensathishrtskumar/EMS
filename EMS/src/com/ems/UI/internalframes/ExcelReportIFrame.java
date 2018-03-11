package com.ems.UI.internalframes;

import static com.ems.constants.QueryConstants.SELECT_ENABLED_ENDEVICES;
import static com.ems.util.EMSSwingUtils.centerFrame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.swingworkers.ExcelReportWorker;
import com.ems.UI.swingworkers.SummaryWokerMonitor;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.constants.EmsConstants;
import com.ems.db.DBConnectionManager;
import com.ems.util.CustomeDateFormatter;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;
import com.ems.util.Helper;
import com.ems.util.MyJDateComponentFactory;

public class ExcelReportIFrame extends JInternalFrame{
	private static final Logger logger = LoggerFactory.getLogger(ExcelReportIFrame.class);
	private static final long serialVersionUID = 6606072613952247592L;
	private JDatePickerImpl datePickerStart;
	private JDatePickerImpl datePickerEnd;
	private List<DeviceDetailsDTO> deviceList;
	private Map<String, DeviceDetailsDTO> deviceMap;

	/**
	 * Create the frame.
	 */
	public ExcelReportIFrame() {
		setMaximizable(true);
		setIconifiable(true);
		setFrameIcon(new ImageIcon(ExcelReportIFrame.class.getResource("/com/ems/resources/system_16x16.gif")));
		setTitle("Reports");
		setClosable(true);
		setBackground(EMSSwingUtils.getBackGroundColor());
		setBounds(100, 100, 640, 449);
		centerFrame(getMe());
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

		addInternalFrameListener(new InternalFrameAdapter() {

			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Confirm Close?", "Exit",
						JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					dispose();
				}
			}

			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {

			}
		});
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JTabbedPane reportTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panel.add(reportTabbedPane);

		JPanel dailyReportPanel = new JPanel();
		reportTabbedPane.addTab("Report", null, dailyReportPanel, null);
		dailyReportPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel compPanel = new JPanel();
		compPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		dailyReportPanel.add(compPanel);
		compPanel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Device");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setSize(new Dimension(130, 25));
		lblNewLabel.setBounds(108, 40, 98, 24);
		compPanel.add(lblNewLabel);
		
		JLabel lblMemoryDetails = new JLabel("Memory Details");
		lblMemoryDetails.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMemoryDetails.setSize(new Dimension(130, 25));
		lblMemoryDetails.setBounds(108, 92, 98, 24);
		compPanel.add(lblMemoryDetails);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(232, 91, 126, 80);
		compPanel.add(scrollPane);
		
		JList<String> memoryMapList = new JList<>();
		memoryMapList.setToolTipText("select memory mapping details");
		scrollPane.setViewportView(memoryMapList);
		memoryMapList.setVisibleRowCount(5);
		memoryMapList.setModel(new DefaultListModel<String>() {private static final long serialVersionUID = 1L;});
		
		JComboBox<DeviceDetailsDTO> deviceCombo = new JComboBox<>();
		deviceCombo.setBounds(230, 42, 126, 20);
		deviceCombo.setModel(new DefaultComboBoxModel<>());
		deviceCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> memory = (DefaultListModel<String>)memoryMapList.getModel();
				memory.removeAllElements();
				
				try {
					DeviceDetailsDTO selected = (DeviceDetailsDTO)deviceCombo.getSelectedItem();
					Properties properties = EMSUtility.loadProperties(selected.getMemoryMapping());
					properties.remove(EmsConstants.SPLIT_JOIN.split("=")[0]);
					
					for(Entry<Object, Object> entry : properties.entrySet()){
						if(entry.getValue().toString().trim().equalsIgnoreCase(EmsConstants.NO_MAP))
							continue;
						
						memory.addElement(entry.getValue().toString());
					}
				} catch (Throwable e2) {
					logger.error("Error  : {}", e2 );
				}
				
				memoryMapList.revalidate();
				memoryMapList.repaint();
			}
		});
		compPanel.add(deviceCombo);
		
		JCheckBox chckbxAllMapping = new JCheckBox("All Values");
		chckbxAllMapping.setToolTipText("select all memory mapping for single device");
		chckbxAllMapping.setBounds(390, 89, 97, 23);
		compPanel.add(chckbxAllMapping);
		
		JCheckBox chckbxAllDevice = new JCheckBox("All Device");
		chckbxAllDevice.setToolTipText("Select all devices");
		chckbxAllDevice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(chckbxAllDevice.isSelected()){
					memoryMapList.setEnabled(false);
					chckbxAllMapping.setEnabled(false);
				} else {
					memoryMapList.setEnabled(true);
					chckbxAllMapping.setEnabled(true);
				}
			}
		});
		chckbxAllDevice.setBounds(390, 41, 97, 23);
		compPanel.add(chckbxAllDevice);
		
		JLabel lblReportStartDate = new JLabel("Report Start Date");
		lblReportStartDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblReportStartDate.setSize(new Dimension(130, 25));
		lblReportStartDate.setBounds(108, 190, 98, 24);
		compPanel.add(lblReportStartDate);
		
		JLabel lblReportEtartDate = new JLabel("Report End Date");
		lblReportEtartDate.setHorizontalAlignment(SwingConstants.RIGHT);
		lblReportEtartDate.setSize(new Dimension(130, 25));
		lblReportEtartDate.setBounds(108, 227, 98, 24);
		compPanel.add(lblReportEtartDate);
		
		MyJDateComponentFactory factory = new MyJDateComponentFactory();
		Properties props = factory.getI18nStrings(Locale.US);
		Calendar today = Calendar.getInstance();
		
		UtilDateModel modelStart = new UtilDateModel();
		modelStart.setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
		modelStart.setSelected(true);
		
		JDatePanelImpl datePanelStart = new JDatePanelImpl(modelStart, props);
		datePickerStart = new JDatePickerImpl(datePanelStart, new CustomeDateFormatter());
		datePickerStart.setToolTipText("Select report start date");
		datePickerStart.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		datePickerStart.setBounds(230, 192, 128, 26);
		compPanel.add(datePickerStart);
		
		UtilDateModel modelEnd = new UtilDateModel();
		modelEnd.setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DATE));
		modelEnd.setSelected(true);
		
		JDatePanelImpl datePanelEnd = new JDatePanelImpl(modelEnd, props);	
		datePickerEnd = new JDatePickerImpl(datePanelEnd, new CustomeDateFormatter());
		datePickerEnd.setToolTipText("Select report end date");
		datePickerEnd.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		datePickerEnd.setBounds(230, 227, 128, 26);
		compPanel.add(datePickerEnd);
		
		JLabel lblRecordsPerHour = new JLabel("Records gap(m)");
		lblRecordsPerHour.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRecordsPerHour.setBounds(108, 274, 98, 24);
		compPanel.add(lblRecordsPerHour);
		
		JComboBox<String> recordGapCombo = new JComboBox<>();
		recordGapCombo.setToolTipText("Select gap between each record to be exported. ie.\r\n1 - every min, 2 - every 2 min  and so on");
		recordGapCombo.setModel(new DefaultComboBoxModel<String>(new String[] {/*"1", "2", */"5", "10", "15", "30"}));//removing 1 & 2 since takes more memory
		recordGapCombo.setBounds(230, 276, 126, 20);
		compPanel.add(recordGapCombo);
		
		JPanel progressPanel = new JPanel();
		progressPanel.setBounds(190, 359, 214, 14);
		compPanel.add(progressPanel);
		progressPanel.setLayout(null);
		
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(0, 0, 214, 14);
		progressPanel.add(progressBar);
		progressBar.setString("Please wait...");
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		
		JButton btnExportToExcel = new JButton("Export to Excel");
		btnExportToExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					DefaultComboBoxModel<DeviceDetailsDTO> deviceModel = (DefaultComboBoxModel<DeviceDetailsDTO>)deviceCombo.getModel();
					List<String> selectedMapping = memoryMapList.getSelectedValuesList();
					int devicesSize = deviceModel.getSize();
					boolean allDevice = chckbxAllDevice.isSelected();
					boolean allMemory = chckbxAllMapping.isSelected();
					
					if(devicesSize == 0){
						JOptionPane.showMessageDialog(getMe(), "No Devices available...", "Reports", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					if(!allDevice && !allMemory && (selectedMapping == null || selectedMapping.size() == 0)){
						JOptionPane.showMessageDialog(getMe(), "No Memory mapping selected...", "Reports", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					
					long reportStartTime = Helper.getStartOfDay(modelStart.getValue().getTime());
					long reportEndTime = Helper.getEndOfDay(modelEnd.getValue().getTime());
					int recordCount = Integer.parseInt(recordGapCombo.getSelectedItem().toString());
					
					List<DeviceDetailsDTO> reportDeviceList = new ArrayList<>();
					
					if(allDevice){
						for(int i = 0;i	< deviceModel.getSize();i++){
							DeviceDetailsDTO device = deviceModel.getElementAt(i);
							device.setAllDevice(true).setAllMemory(true);
							device.setStartTime(reportStartTime).setEndTime(reportEndTime).setRecordCount(recordCount);
							setMemoryMappingDetails(device, true, null);
							reportDeviceList.add(device);
						}
					} else {
						DeviceDetailsDTO device = (DeviceDetailsDTO)deviceModel.getSelectedItem();
						device.setStartTime(reportStartTime).setEndTime(reportEndTime)
							.setRecordCount(recordCount).setAllMemory(allMemory);
						setMemoryMappingDetails(device, allMemory,selectedMapping.toArray(new String[selectedMapping.size()]));
						reportDeviceList.add(device);
					}
					
					logger.debug("starttime : {} endtime : {}",reportStartTime,reportEndTime);
					
					SwingWorker<Object, Object> swingWorker = new SwingWorker<Object, Object>() {

						@Override
						protected Object doInBackground() throws Exception {
							progressBar.setVisible(true);
							progressPanel.revalidate();
							progressPanel.repaint();
							
							logger.info("Total devices requested for  report  count : {} device details {}",
									reportDeviceList.size(), reportDeviceList);
							
							ExcelReportWorker worker = new ExcelReportWorker(reportDeviceList);
							Future<Object> excelReport = ConcurrencyUtils.execute(worker);
							logger.info("Main excel report worker submitterd");
							String reportPath = (String)excelReport.get();
							logger.info("Waiting for main excel worker");
							JOptionPane.showMessageDialog(null, reportPath, "Reports", JOptionPane.INFORMATION_MESSAGE);
							return null;
						}
						
						@Override
						protected void done() {
							progressBar.setVisible(false);
							progressPanel.revalidate();
							progressPanel.repaint();
						}
					};
					
					swingWorker.execute();
				
				} catch (Exception e2) {
					logger.error("Error : {}", e2);
					JOptionPane.showMessageDialog(getMe(), e2.getLocalizedMessage(), "Reports", JOptionPane.ERROR_MESSAGE);
				}finally{
					progressBar.setVisible(false);
					progressPanel.revalidate();
					progressPanel.repaint();
				}
			}
		});
		
		btnExportToExcel.setToolTipText("Export report as Excel");
		btnExportToExcel.setBounds(229, 325, 129, 23);
		compPanel.add(btnExportToExcel);
		
		JButton btnSummary = new JButton("Summary");
		btnSummary.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			
				long reportStartTime = Helper.getStartOfDay(modelStart.getValue().getTime());
				long reportEndTime = Helper.getEndOfDay(modelEnd.getValue().getTime());
				long checkDateRange = Helper.getStartOfDay(modelEnd.getValue().getTime());
				boolean allDevice = chckbxAllDevice.isSelected();
				DefaultComboBoxModel<DeviceDetailsDTO> deviceModel = (DefaultComboBoxModel<DeviceDetailsDTO>)deviceCombo.getModel();
				
				//Verify input for processing if any
				if(reportStartTime != checkDateRange){
					JOptionPane.showMessageDialog(getMe(), "Date range cann't be more than 1 day", "Reports", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				List<DeviceDetailsDTO> summaryDeviceList = new ArrayList<>();
				//Get All or selected device for summary preparation
				if(allDevice){
					for(int i = 0;i	< deviceModel.getSize();i++){
						DeviceDetailsDTO device = deviceModel.getElementAt(i)
								.setStartTime(reportStartTime).setEndTime(reportEndTime);
						summaryDeviceList.add(device);
					}
				} else {
					DeviceDetailsDTO device = (DeviceDetailsDTO)deviceModel.getSelectedItem();
					device.setStartTime(reportStartTime).setEndTime(reportEndTime);
					summaryDeviceList.add(device);
				}
				
				//We got required devices for processing
				logger.debug("Summary is requested for devices : {}", summaryDeviceList);
				
				try {
					SwingWorker<Object, Object> swingWorker = new SwingWorker<Object, Object>() {

						@Override
						protected Object doInBackground() throws Exception {
							progressBar.setVisible(true);
							progressPanel.revalidate();
							progressPanel.repaint();
							
							SummaryWokerMonitor worker = new SummaryWokerMonitor(summaryDeviceList);
							Future<Object> excelReport = ConcurrencyUtils.execute(worker);
							logger.info("Main excel report worker submitterd");
							String reportPath = (String)excelReport.get();
							logger.info("Waiting for main excel worker");
							JOptionPane.showMessageDialog(null, reportPath, "Reports", JOptionPane.INFORMATION_MESSAGE);
							return null;
						}
						
						@Override
						protected void done() {
							progressBar.setVisible(false);
							progressPanel.revalidate();
							progressPanel.repaint();
						}
					};
					
					swingWorker.execute();
				} catch (Exception e2) {
					logger.error("Error creating summary ",e2);
					JOptionPane.showMessageDialog(getMe(), "Summary Creation failed, Please try again", "Reports",
							JOptionPane.ERROR_MESSAGE);
				} finally{
					progressBar.setVisible(false);
					progressPanel.revalidate();
					progressPanel.repaint();
				}
			}
		});
		btnSummary.setBounds(344, 325, 77, 23);
/*		scompPanel.add(btnSummary);
*/		
		loadAvailableActiveDevices(deviceCombo);
		
		JButton btnSingleParam = new JButton("Export Single Param");
		btnSingleParam.setVisible(false);
		btnSingleParam.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// code to Export single params
				//1. validate date diff - should not be > 2
				int difference = (int)EMSUtility.getDateDiff(modelStart.getValue(), modelEnd.getValue(), TimeUnit.DAYS);
				String selectedMapping = memoryMapList.getSelectedValue();
				
				if(difference > EmsConstants.SINGLEPARAM_DATE_DIFF){
					JOptionPane.showMessageDialog(getMe(), "Date range cann't be more than " + EmsConstants.SINGLEPARAM_DATE_DIFF + " day", "Reports", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(selectedMapping == null || selectedMapping.isEmpty()){
					JOptionPane.showMessageDialog(getMe(), "Select any memory mapping ", "Reports - Single Param", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				LinkedHashMap<Long, DeviceDetailsDTO> requiredDevices = DBConnectionManager.getSingleParamDevices(selectedMapping);
				
				//2. get all devices which are having selected param
				long reportStartTime = Helper.getStartOfDay(modelStart.getValue().getTime());
				long reportEndTime = Helper.getEndOfDay(modelEnd.getValue().getTime());
				
				//3. 
			}
		});
		btnSingleParam.setBounds(431, 325, 136, 23);
		//compPanel.add(btnSingleParam);
	}

	private void setMemoryMappingDetails(DeviceDetailsDTO device, boolean allMemory, String[] selected){
		if(device != null){
			Properties props = EMSUtility.loadProperties(device.getMemoryMapping());
			props.remove(EmsConstants.SPLIT_JOIN.split("=")[0]);
			
			if(allMemory){
				device.setReportMapping(props);
			} else {
				Properties temp = new Properties();
				
				if(selected != null){
					
					ArrayList<String> list = new ArrayList<>();
					list.addAll(Arrays.asList(selected));
					
					for(Entry<Object, Object> entry : props.entrySet()){
						String value = entry.getValue().toString().trim();
						
						if(!value.equalsIgnoreCase(EmsConstants.NO_MAP) && list.contains(value)){
							temp.putIfAbsent(entry.getKey(), entry.getValue());
						}
					}
				}
				
				device.setReportMapping(temp);
			}
		}
	}
	
	public JDatePickerImpl getDatePickerStart() {
		return datePickerStart;
	}

	public JDatePickerImpl getDatePickerEnd() {
		return datePickerEnd;
	}

	public JInternalFrame getMe() {
		return this;
	}
	
	private void loadAvailableActiveDevices(JComboBox<DeviceDetailsDTO> deviceCombo) {

		try {
			DefaultComboBoxModel<DeviceDetailsDTO> deviceComboModel = (DefaultComboBoxModel<DeviceDetailsDTO>)deviceCombo.getModel();
			deviceComboModel.removeAllElements();
			
			deviceList = DBConnectionManager.getAvailableDevices(SELECT_ENABLED_ENDEVICES);
			deviceMap = new HashMap<>(deviceList.size());
			for (DeviceDetailsDTO unit : deviceList) {
				deviceComboModel.addElement(unit);
				deviceMap.put(unit.getDeviceName(), unit);
			}
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed to load active devices , Reports page : {}", e.getLocalizedMessage());
		}
	}
}
