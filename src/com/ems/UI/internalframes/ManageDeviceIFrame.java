package com.ems.UI.internalframes;

import static com.ems.constants.EmsConstants.BAUDRATES;
import static com.ems.constants.EmsConstants.PARITY;
import static com.ems.constants.EmsConstants.READ_METHOD;
import static com.ems.constants.EmsConstants.REG_MAPPING;
import static com.ems.constants.EmsConstants.STOPBIT;
import static com.ems.constants.EmsConstants.WORDLENGTH;
import static com.ems.util.EMSSwingUtils.centerFrame;
import static com.ems.util.EMSUtility.convertObjectArray;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Future;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ManageDeviceDetailsDTO;
import com.ems.UI.internalframes.renderer.JComboRenderer;
import com.ems.UI.internalframes.renderer.JcomboEditor;
import com.ems.UI.internalframes.renderer.MyComboModel;
import com.ems.UI.internalframes.renderer.TextAreaEditor;
import com.ems.UI.internalframes.renderer.TextAreaRenderer;
import com.ems.db.DBConnectionManager;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;

public class ManageDeviceIFrame extends JInternalFrame {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ManageDeviceIFrame.class);

	private JTable table;
	private JComboBox<String> comboBoxPorts;
	private JComboBox<String> comboBoxPollDelay;

	/**
	 * Create the frame.
	 */
	public ManageDeviceIFrame() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(ManageDeviceIFrame.class.getResource("/com/ems/resources/system_16x16.gif")));
		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated(InternalFrameEvent arg0) {
			}

			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {
			}
		});
		setVisible(true);
		setToolTipText("Add,Remove or Edit devices");
		setTitle("Manage Device");
		setClosable(true);
		setBounds(100, 100, 1019, 541);
		centerFrame(getMe());
		getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(null);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)),
				"Actions", TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(289, 453, 402, 42);
		panel.add(panel_1);
		panel_1.setLayout(null);

		JButton btnAddRow = new JButton("Add Device");
		btnAddRow.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnAddRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int defaultDevice = ConfigHelper.getDefaultDevices();
				if (table.getRowCount() <= defaultDevice) {
					DefaultTableModel model = (DefaultTableModel) table.getModel();
					model.insertRow(table.getRowCount(), getEmptyRow());
				} else {
					JOptionPane.showMessageDialog(getMe(),
							"Application is configured to have only " + defaultDevice + " Devices", "Manage Device",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		btnAddRow.setMnemonic('A');
		btnAddRow.setBounds(10, 11, 120, 23);
		panel_1.add(btnAddRow);

		JButton btnSaveChanges = new JButton("Save Changes");
		btnSaveChanges.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnSaveChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boolean dbConfig = TempDataManager.checkDBConfigFile();
				logger.info("DB config file availability in Save changes : {}", dbConfig);
				if (!dbConfig) {
					JOptionPane.showMessageDialog(getMe(), "DB not configured", "DB Configuration",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				List<DeviceDetailsDTO> detailsPrepared = validateAndPrepareDetails(table);
				logger.info("Details prepared for CRUD : {}", detailsPrepared);

				try {
					List<Future<Object>> taskList = DBConnectionManager.processDevices(detailsPrepared);
					// Wait for worker to complete task
					for (Future<Object> task : taskList)
						task.get();

					populateDBDeviceDetails(table);

					JOptionPane.showMessageDialog(getMe(), "Saved successfully", "Success",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					logger.error("{}", e);
					JOptionPane.showMessageDialog(getMe(), e.getLocalizedMessage(), "Failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnSaveChanges.setMnemonic('V');
		btnSaveChanges.setBounds(270, 11, 120, 23);
		panel_1.add(btnSaveChanges);

		comboBoxPorts = new JComboBox<String>();
		comboBoxPorts.setBounds(587, 20, 73, 20);
		EMSSwingUtils.addItemsComboBox(comboBoxPorts, 0, EMSUtility.getAvailablePort());
		/* panel_1.add(comboBoxPorts); */

		JLabel lblPorts = new JLabel("Ports");
		lblPorts.setBounds(519, 23, 58, 14);
		/* panel_1.add(lblPorts); */

		JLabel lblPollFrequency = new JLabel("Poll Frequency");
		lblPollFrequency.setBounds(670, 23, 98, 14);
		/* panel_1.add(lblPollFrequency); */

		comboBoxPollDelay = new JComboBox<String>();
		comboBoxPollDelay.setToolTipText("Poll at specified interval(Minutes)");
		comboBoxPollDelay.setModel(new DefaultComboBoxModel<String>(new String[] { "1", "2", "3", "4", "5", "6", "7",
				"8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20" }));
		comboBoxPollDelay.setBounds(778, 20, 98, 20);
		comboBoxPollDelay.setSelectedIndex(0);
		/* panel_1.add(comboBoxPollDelay); */

		JButton btnRemoveRow = new JButton("Remove Device");
		btnRemoveRow.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnRemoveRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				int selectedRow = table.getSelectedRow();
				if (selectedRow == -1 || table.getRowCount() == 1) {
					JOptionPane.showMessageDialog(getMe(), "Invalid Selection", "Remove device",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				// Get uniqueid value, reject if it is "0"
				Number uniqueid = (Number) table.getValueAt(table.getSelectedRow(), 0);
				logger.info("remove row selected row : {}, uniqueid : {}", table.getSelectedRow(), uniqueid);

				if (uniqueid.intValue() != 0) {
					JOptionPane.showMessageDialog(getMe(), "Cannot delete existing device, please disable it",
							"Remove device", JOptionPane.WARNING_MESSAGE);
				} else {
					model.removeRow(table.getSelectedRow());
					model.fireTableDataChanged();
				}
			}
		});
		btnRemoveRow.setBounds(140, 11, 120, 23);
		panel_1.add(btnRemoveRow);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(
				new CompoundBorder(null, new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null))),
				"Devices", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_2.setBounds(0, 0, 1003, 452);
		panel.add(panel_2);
		panel_2.setLayout(new GridLayout(1, 0, 0, 0));

		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setColumnSelectionAllowed(true);
		table.setCellSelectionEnabled(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setAutoCreateRowSorter(false);
		table.setFillsViewportHeight(true);

		table.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "UNIQUEID", "DEVICEID", "DEVICENAME", "BAUDRATE", "WORDLENGTH", "PARITY", "STOPBIT",
						"MEMORY MAPPING", "ENABLED", "MSRF/LSRF", "PORT", "METHOD" }) {
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] { Integer.class, Integer.class, String.class, Integer.class, Short.class,
					Short.class, Short.class, String.class, Boolean.class, String.class, String.class, String.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}

			boolean[] columnEditables = new boolean[] { false, true, true, true, true, true, true, true, true, true,
					true, true };

			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}

		});

		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(60);
		table.getColumnModel().getColumn(2).setPreferredWidth(130);
		table.getColumnModel().getColumn(3).setPreferredWidth(70);
		table.getColumnModel().getColumn(4).setPreferredWidth(90);
		table.getColumnModel().getColumn(5).setPreferredWidth(70);
		table.getColumnModel().getColumn(6).setPreferredWidth(70);
		table.getColumnModel().getColumn(7).setPreferredWidth(130);// Memory
																	// mapping
		table.getColumnModel().getColumn(8).setPreferredWidth(70);// Enabled
		table.getColumnModel().getColumn(9).setPreferredWidth(70);// MSRF/LSRF
		table.getColumnModel().getColumn(10).setPreferredWidth(70);// Port
		table.getColumnModel().getColumn(11).setPreferredWidth(70);// Method

		table.setRowHeight(30);

		addColumnModelsWithRenderer(table);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(null);
		panel_2.add(scrollPane);
	}

	private JInternalFrame getMe() {
		return this;
	}

	/**
	 * validates user input and prepares List of DTO
	 */
	private List<DeviceDetailsDTO> validateAndPrepareDetails(JTable table) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		List<DeviceDetailsDTO> list = new ArrayList<DeviceDetailsDTO>();

		Vector tableVector = tableModel.getDataVector();
		Iterator rowIterator = tableVector.iterator();
		int rowIndex = 0;

		while (rowIterator.hasNext()) {
			Vector<Object> columnVector = (Vector<Object>) rowIterator.next();

			DeviceDetailsDTO dto = new DeviceDetailsDTO();
			dto.setRowIndex(rowIndex);
			dto.setPollDelay(getComboBoxPollDelay());
			//dto.setPort(getComboBoxPorts());

			dto.setUniqueId(Integer.parseInt(columnVector.get(0).toString()));
			dto.setDeviceId(Integer.parseInt(columnVector.get(1).toString()));
			dto.setDeviceName(columnVector.get(2).toString());

			// Get values from MyComboModel
			dto.setBaudRate(Integer.parseInt(getSelectedValue((MyComboModel) columnVector.get(3))));
			dto.setWordLength(Integer.parseInt(getSelectedValue((MyComboModel) columnVector.get(4))));
			dto.setParity(getSelectedValue((MyComboModel) columnVector.get(5)));
			dto.setStopbit(Integer.parseInt(getSelectedValue((MyComboModel) columnVector.get(6))));
			logger.trace("Updated Mapping : {}", tableModel.getValueAt(rowIndex, 7));
			dto.setMemoryMapping(columnVector.get(7).toString());
			dto.setEnabled(columnVector.get(8).toString());
			dto.setRegisterMapping(getSelectedValue((MyComboModel) columnVector.get(9)));

			dto.setPort(getSelectedValue((MyComboModel) columnVector.get(10)));
			dto.setMethod(getSelectedValue((MyComboModel) columnVector.get(11)));

			list.add(dto);
			rowIndex += 1;
		}

		return list;
	}

	public String getSelectedValue(MyComboModel comboBoxModel) {
		return comboBoxModel.getSelectedItem().toString();
	}

	private Object[] getEmptyRow() {
		int rowCount = table.getRowCount() + 1;
		/*
		 * when uniqueid "0", means record has to be inserted update when non
		 * zero value exist in uniqueid
		 */
		return new Object[] { 0, 0, "Device Name " + rowCount,
				new MyComboModel(convertObjectArray(BAUDRATES), BAUDRATES[6]),
				new MyComboModel(convertObjectArray(WORDLENGTH), WORDLENGTH[1]),
				new MyComboModel(convertObjectArray(PARITY), PARITY[2]),
				new MyComboModel(convertObjectArray(STOPBIT), STOPBIT[0]), "", false,
				new MyComboModel(convertObjectArray(REG_MAPPING), REG_MAPPING[0]),
				new MyComboModel(convertObjectArray(EMSUtility.getAvailablePort()), ""),
				new MyComboModel(convertObjectArray(READ_METHOD), READ_METHOD[0]) };
	}

	private void populateDBDeviceDetails(final JTable table) {
		ManageDeviceDetailsDTO deviceManagerDto = DBConnectionManager.getDeviceManagerDetails();
		List<DeviceDetailsDTO> availableDevices = deviceManagerDto.getList();
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

		comboBoxPollDelay.setSelectedItem(deviceManagerDto.getPollingDelay());
		comboBoxPorts.setSelectedItem(deviceManagerDto.getPortName());

		/*
		 * for(int i=0;i<table.getRowCount();i++){ tableModel.removeRow(i); }
		 */
		tableModel.getDataVector().removeAllElements();
		tableModel.fireTableDataChanged();

		if (availableDevices != null && !availableDevices.isEmpty()) {
			for (DeviceDetailsDTO device : availableDevices) {
				Object[] deviceRow = new Object[] { device.getUniqueId(), device.getDeviceId(), device.getDeviceName(),
						new MyComboModel(convertObjectArray(BAUDRATES), device.getBaudRate()),
						new MyComboModel(convertObjectArray(WORDLENGTH), device.getWordLength()),
						new MyComboModel(convertObjectArray(PARITY), device.getParity()),
						new MyComboModel(convertObjectArray(STOPBIT), device.getStopbit()), device.getMemoryMapping(),
						new Boolean(device.getEnabled()),
						new MyComboModel(convertObjectArray(REG_MAPPING), device.getRegisterMapping()),
						new MyComboModel(convertObjectArray(EMSUtility.getAvailablePort()), device.getPort()),
						new MyComboModel(convertObjectArray(READ_METHOD), device.getMethod()) };

				tableModel.insertRow(device.getRowIndex(), deviceRow);
			}
		} else {
			tableModel.insertRow(0, getEmptyRow());
			logger.info("Empty table , empty row added");
		}

		tableModel.fireTableDataChanged();
	}

	private void addColumnModelsWithRenderer(final JTable table) {

		try {
			// To load existing details from DB
			populateDBDeviceDetails(table);
		} catch (Exception e) {
			logger.error("{}", e);
			logger.error("Failed loading existing devices...");
		}

		// Baudrate column
		TableColumn column = table.getColumnModel().getColumn(3);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		// Wordlength column
		column = table.getColumnModel().getColumn(4);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		// Parity column
		column = table.getColumnModel().getColumn(5);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		// Stopbit column
		column = table.getColumnModel().getColumn(6);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		// Memory Mapping column
		column = table.getColumnModel().getColumn(7);
		column.setCellRenderer(new TextAreaRenderer());
		column.setCellEditor(new TextAreaEditor());

		// MSRF/LSRF column
		column = table.getColumnModel().getColumn(9);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		// Port column
		column = table.getColumnModel().getColumn(10);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		// Method column
		column = table.getColumnModel().getColumn(11);
		column.setCellEditor(new JcomboEditor());
		column.setCellRenderer(new JComboRenderer());

		table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent event) {

				if (event.getType() == event.INSERT) {
					logger.info("Tabel model changed INSERT, selected row {} ", table.getSelectedRow());
				} else if (event.getType() == event.UPDATE) {
					/*
					 * Object value = table.getValueAt(table.getSelectedRow(),
					 * event.getColumn());
					 * 
					 * if (value instanceof MyComboModel) {
					 * logger.info("Selected value : {}", ((MyComboModel)
					 * value).getSelectedItem()); } else {
					 * logger.info("update column value {}", value); }
					 */
				} else if (event.getType() == event.DELETE) {

				}
			}
		});
	}

	public String getComboBoxPorts() {
		String serialPort = "";
		if (comboBoxPorts.getSelectedIndex() != -1) {
			serialPort = comboBoxPorts.getSelectedItem().toString();
		}
		return serialPort;
	}

	public int getComboBoxPollDelay() {
		int pollDelay = Integer.parseInt(comboBoxPollDelay.getSelectedItem().toString());
		return pollDelay;
	}

}
