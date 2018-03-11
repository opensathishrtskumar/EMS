package com.ems.UI.internalframes;

import static com.ems.util.EMSSwingUtils.centerFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ChartDTO;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.EmsConstants;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.response.handlers.LiveResponseHandler;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;

public class NewDashboardFrame extends JInternalFrame {
	private static final Logger logger = LoggerFactory.getLogger(NewDashboardFrame.class);

	private static final long serialVersionUID = 1L;
	private JTable failedDeviceTable;
	private JTable readingTable;
	private SwingWorker<Object, Object> readingWorker = null;
	private MeterUsageChart liveChart;
	private JList<String> list;

	/**
	 * Create the frame.
	 */
	public NewDashboardFrame() {
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setBackground(EMSSwingUtils.getBackGroundColor());
		setFrameIcon(new ImageIcon(NewDashboardFrame.class.getResource("/com/ems/resources/agt_home.png")));
		setClosable(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Dashboard");
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(1, 0, 0, 0));

		JSplitPane splitPane = new JSplitPane();
		panel.add(splitPane);
		splitPane.setResizeWeight(0.9d);

		JPanel chartPanel = new JPanel();
		chartPanel.setBorder(new TitledBorder(null, "Monitor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(chartPanel);
		chartPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.8d);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.setBorder(null);
		chartPanel.add(splitPane_1);

		JPanel chartPane = new JPanel();
		splitPane_1.setLeftComponent(chartPane);
		chartPane.setLayout(new BorderLayout(0, 0));

		JPanel chartImplPanel = new JPanel();
		chartPane.add(chartImplPanel, BorderLayout.CENTER);
		chartImplPanel.setLayout(new GridLayout(0, 1, 0, 0));

		ChartDTO dto = getChartDTO(new String[] {}, new DeviceDetailsDTO());
		this.liveChart = new MeterUsageChart(dto);
		chartImplPanel.add(this.liveChart);

		JPanel chartControlPanel = new JPanel();
		chartControlPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		chartPane.add(chartControlPanel, BorderLayout.NORTH);
		chartControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		List<DeviceDetailsDTO> activeDevices = loadActiveDevices();
		DeviceDetailsDTO[] deviceArr = new DeviceDetailsDTO[activeDevices.size()];
		deviceArr = activeDevices.toArray(deviceArr);

		JComboBox<DeviceDetailsDTO> comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<>(deviceArr));
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();
				model.removeAllElements();
				DeviceDetailsDTO selected = (DeviceDetailsDTO) comboBox.getSelectedItem();

				if (selected.getUniqueId() == 0) {
					JOptionPane.showMessageDialog(getMe(), "Select Valid device", "Monitor",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				ExtendedSerialParameter parameter = EMSUtility.mapDeviceToSerialParam(selected);
				if(!parameter.isSplitJoin()){
					Collection<String> memoryMap = parameter.getMemoryMappings().values();
					for (String memory : memoryMap) {

						if(!memory.trim().equalsIgnoreCase(EmsConstants.NO_MAP)){
							model.addElement(memory);
						}
					}
				} else {
					List<Map<Long, String>> list = parameter.getSplitJoinDTO().getMemoryMappings();

					for(Map<Long, String> map : list){
						for(Entry<Long, String> entry : map.entrySet()){
							if(!entry.getValue().trim().equalsIgnoreCase(EmsConstants.NO_MAP)){
								model.addElement(entry.getValue());
							}
						}
					}
				}

				logger.trace("Live chart monitoring dashboard - Memory mapping reloaded ...");
			}
		});
		chartControlPanel.add(comboBox);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		chartControlPanel.add(scrollPane_1);

		// Memory mapping details
		this.list = new JList<>();
		this.list.setModel(new DefaultListModel<>());
		this.list.setFixedCellHeight(18);
		this.list.setFixedCellWidth(140);
		this.list.setSize(new Dimension(140, 25));
		this.list.setVisibleRowCount(2);
		scrollPane_1.setViewportView(this.list);

		JButton btnNewButton = new JButton("Monitor");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				DeviceDetailsDTO selected = (DeviceDetailsDTO) comboBox.getSelectedItem();

				if (selected.getUniqueId() == 0) {
					JOptionPane.showMessageDialog(getMe(), "Select Valid device", "Monitor",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				if (liveChart != null) {
					liveChart.stop();
					chartImplPanel.remove(liveChart);
					liveChart = null;
				}

				List<String> selectedList = list.getSelectedValuesList();
				String[] selectedValues = selectedList.toArray(new String[selectedList.size()]);
				ChartDTO dto = getChartDTO(selectedValues, selected);

				liveChart = new MeterUsageChart(dto);
				liveChart.setDataGenerator(
						new LiveResponseHandler(dto.getSeriesName()).setDevice(dto.getParameter()).build());
				liveChart.start();
				chartImplPanel.add(liveChart);

				chartImplPanel.repaint();
				chartImplPanel.revalidate();
			}
		});
		chartControlPanel.add(btnNewButton);

		JPanel failedDevicePanel = new JPanel();
		failedDevicePanel.setBorder(
				new TitledBorder(null, "Failed Devices", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane_1.setRightComponent(failedDevicePanel);
		failedDevicePanel.setLayout(new GridLayout(0, 1, 0, 0));

		JScrollPane scrollPane = new JScrollPane();
		failedDevicePanel.add(scrollPane);

		failedDeviceTable = new JTable();
		failedDeviceTable
		.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Device Name", "Time", "COM Port" }) {
			private static final long serialVersionUID = 1L;
			Class<?>[] columnTypes = new Class[] { String.class, String.class, String.class };

			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, true, true };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});

		loadFailedDevices((DefaultTableModel) failedDeviceTable.getModel());
		scrollPane.setViewportView(failedDeviceTable);

		JPanel readingPanel = new JPanel();
		readingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Readings",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		splitPane.setRightComponent(readingPanel);
		readingPanel.setLayout(new BorderLayout(0, 0));

		JPanel readinControlPanel = new JPanel();
		readingPanel.add(readinControlPanel, BorderLayout.NORTH);

		JComboBox<DeviceDetailsDTO> readingCombo = new JComboBox<>();
		readingCombo.setModel(new DefaultComboBoxModel<>(deviceArr));
		readinControlPanel.add(readingCombo);

		JButton readingButton = new JButton("Monitor");
		readingButton.addActionListener((e) -> {
			killWorker(readingWorker);

			DeviceDetailsDTO selected = (DeviceDetailsDTO) readingCombo.getSelectedItem();

			if (selected.getUniqueId() == 0) {
				JOptionPane.showMessageDialog(getMe(), "Please select device", "Monitor", JOptionPane.WARNING_MESSAGE);
				return;
			}

			ExtendedSerialParameter parameter = EMSUtility.mapDeviceToSerialParam(selected);
			readingWorker = new ReadingSwingWorker((DefaultTableModel) readingTable.getModel(), parameter)
					.setFailedDeviceModel((DefaultTableModel) failedDeviceTable.getModel());
			readingWorker.execute();
		});

		readinControlPanel.add(readingButton);

		JPanel readingValuePanel = new JPanel();
		readingPanel.add(readingValuePanel, BorderLayout.CENTER);
		readingValuePanel.setLayout(new GridLayout(0, 1, 0, 0));

		JScrollPane readingScroll = new JScrollPane();
		readingValuePanel.add(readingScroll);

		readingTable = new JTable();
		readingTable.setModel(new DefaultTableModel(new Object[][] {}, new String[] { "Specification", "Value" }) {
			private static final long serialVersionUID = 1L;
			Class<?>[] columnTypes = new Class[] { String.class, String.class };

			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, false };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		readingScroll.setViewportView(readingTable);

		JPanel readingTimePanel = new JPanel();
		readingPanel.add(readingTimePanel, BorderLayout.SOUTH);
		readingTimePanel.setLayout(new BorderLayout(0, 0));

		/*
		 * JLabel readingTimeLabel = new JLabel("Time");
		 * readingTimePanel.add(readingTimeLabel);
		 */

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				killWorker(readingWorker);

				if (liveChart != null) {
					liveChart.stop();
				}

				dispose();
			}
		});

		centerFrame(getMe());
		EMSSwingUtils.setMaximizedSize(getMe());
	}

	private ChartDTO getChartDTO(String[] series, DeviceDetailsDTO device) {
		ChartDTO dto = new ChartDTO().setMaxAge(10000).setControlPanelRequired(false).setxAxisName("Readings")
				.setyAxisName("Time").setInterval(8 * 1000).setSeriesName(series).setParameter(device);
		return dto;
	}

	private List<DeviceDetailsDTO> loadActiveDevices() {
		List<DeviceDetailsDTO> devices = DBConnectionManager
				.getAvailableDevices(QueryConstants.SELECT_ENABLED_ENDEVICES);

		DeviceDetailsDTO select = new DeviceDetailsDTO();
		select.setUniqueId(0);
		select.setDeviceName("--Select--");
		devices.add(0, select);
		return devices;
	}

	public void loadFailedDevices(DefaultTableModel tableModel) {

		tableModel.getDataVector().removeAllElements();

		List<DeviceDetailsDTO> failedDevices = DBConnectionManager.getFailedDeviceDetails();

		if (!failedDevices.isEmpty()) {
			for (DeviceDetailsDTO device : failedDevices) {
				tableModel.insertRow(device.getRowIndex(),
						new Object[] { device.getDeviceName(), device.getTimeStamp(), device.getPort() });
			}
		} else {
			tableModel.insertRow(0, new Object[] { "", "", "" });
			logger.info("Empty table , empty row added");
		}

		tableModel.fireTableDataChanged();
	}

	private JInternalFrame getMe() {
		return this;
	}

	private void killWorker(SwingWorker<Object, Object> worker) {
		if (worker != null) {
			worker.cancel(true);
			worker = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		killWorker(readingWorker);
		super.finalize();
	}

	class ReadingSwingWorker extends SwingWorker<Object, Object> {
		private DefaultTableModel model;
		private ExtendedSerialParameter parameter;
		private DefaultTableModel failedDeviceModel;

		public ReadingSwingWorker(DefaultTableModel model, ExtendedSerialParameter parameter) {
			this.model = model;
			this.parameter = parameter;
		}

		public DefaultTableModel getFailedDeviceModel() {
			return failedDeviceModel;
		}

		public ReadingSwingWorker setFailedDeviceModel(DefaultTableModel failedDeviceModel) {
			this.failedDeviceModel = failedDeviceModel;
			return this;
		}

		@Override
		protected Object doInBackground() throws Exception {

			for (; !isCancelled();) {
				model.getDataVector().removeAllElements();

				try {
					List<PollingDetailDTO> result = DBConnectionManager
							.fetchRecentPollingDetails(parameter.getUniqueId());

					if (result != null && !result.isEmpty()) {
						PollingDetailDTO polling = result.get(0);
						Properties props = EMSUtility.loadProperties(polling.getUnitresponse());
						Map<String, String> registerValue = EMSUtility.convertProp2Map(props);

						if(!parameter.isSplitJoin()){
							Map<Long, String> memoryMapping = parameter.getMemoryMappings();

							for (Entry<Long, String> entry : memoryMapping.entrySet()) {
								if(!entry.getValue().trim().equalsIgnoreCase(EmsConstants.NO_MAP)){
									model.addRow(new Object[] { entry.getValue(),
											registerValue.getOrDefault(String.valueOf(entry.getKey()), "0.00") });
								}
							}
						} else {
							List<Map<Long, String>> list = parameter.getSplitJoinDTO().getMemoryMappings();

							for(Map<Long, String> map : list){
								for(Entry<Long, String> splitEntry : map.entrySet()){
									if(!splitEntry.getValue().trim().equalsIgnoreCase(EmsConstants.NO_MAP)){
										model.addRow(new Object[] { splitEntry.getValue(),
												registerValue.getOrDefault(String.valueOf(splitEntry.getKey()), "0.00") });
									}
								}
							}
						}
					}

					logger.trace("Dashboard Reading refreshing...");

					model.fireTableDataChanged();
				} catch (Exception e) {
					logger.error("Dashboard error : {}", e);
				}

				try {
					loadFailedDevices(getFailedDeviceModel());
				} catch (Exception e) {
					logger.error("Failed devices error : {}", e);
				}

				Thread.sleep(8000);
				failIfInterrupted();
			}

			return null;
		}

		private void failIfInterrupted() throws InterruptedException {
			if (Thread.currentThread().isInterrupted() || isCancelled()) {
				throw new InterruptedException("Worker interrupted...");
			}
		}
	}
}
