package com.ems.UI.internalframes;

import static com.ems.constants.QueryConstants.SELECT_ENABLED_ENDEVICES;
import static com.ems.util.EMSSwingUtils.centerFrame;
import static com.ems.util.EMSUtility.groupDeviceForPolling;
import static com.ems.util.EMSUtility.mapDevicesToSerialParams;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.internalframes.renderer.JTableRenderer;
import com.ems.UI.swingworkers.GroupedDeviceWorker;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.response.handlers.PollingResponseHandler;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;

public class PollingIFrame extends JInternalFrame implements AbstractIFrame{
	private static final Logger logger = LoggerFactory.getLogger(PollingIFrame.class);
	private static final long serialVersionUID = -301006694590699442L;
	private JTable table;
	private SwingWorker<Object, Object> worker = null;
	private JComboBox<String> comboBoxPorts = null;
	private JComboBox<String> comboBox = null;
	private JProgressBar progressBar = null;
	
	/**
	 * Create the frame.
	 */
	
	public PollingIFrame() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(PollingIFrame.class.getResource("/com/ems/resources/system_16x16.gif")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setClosable(true);
		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated(InternalFrameEvent arg0) {
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(),
						"Poller will be stopped if running \nReally Close?", "Exit",
						JOptionPane.YES_NO_OPTION);  
		        if (option == JOptionPane.YES_OPTION){
		            closeWorker();
		            logger.info("Polling Frame is being clossed...");
		            dispose();
		        } else {
		        	//do nothing
		        } 
			}
			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {
				boolean dbConfig = TempDataManager.checkDBConfigFile();
				if(!dbConfig){
					JOptionPane.showMessageDialog(getMe(),
							"Database not configured", "Config",
							JOptionPane.ERROR_MESSAGE);
					
					logger.info("Database not configured : polling frame opened");
				}
				
				closeWorker();
				if(dbConfig)
					loadActiveDevices(getTable());
			}
		});
		setTitle("Poll Reading");
		setBounds(100, 100, 775, 544);
		centerFrame(getMe());
		getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)), "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(0, 0, 759, 51);
		panel.setMinimumSize(new Dimension(10, 49));
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JButton btnNewButton = new JButton("Start Polling");
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(worker != null){
					JOptionPane.showMessageDialog(getMe(),
							"Already worker is running!!!", "Running",
							JOptionPane.INFORMATION_MESSAGE);
					logger.debug("Already polling worker is running");
					return;
				}
				
				Object object = comboBoxPorts.getSelectedItem();
				if(object == null || object.toString().isEmpty()){
					JOptionPane.showMessageDialog(getMe(),
							"No port available to connect", "Port?",
							JOptionPane.INFORMATION_MESSAGE);
					logger.debug("No port connected for polling");
					return;
				}
				
				DefaultTableModel model = (DefaultTableModel)getTable().getModel();
				if(model.getRowCount() == 0){
					JOptionPane.showMessageDialog(getMe(),
							"No port available to connect", "Port?",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				
				String port = getComboBoxPorts().getSelectedItem().toString();
				int frequency = Integer.parseInt(getComboBox().getSelectedItem().toString()) * 60 * 1000;
				
				List<DeviceDetailsDTO> availableDevices = DBConnectionManager
						.getAvailableDevices(SELECT_ENABLED_ENDEVICES);
				
				for(DeviceDetailsDTO device : availableDevices){
					device.setPort(port);
				}
				
				List<ExtendedSerialParameter> devices = mapDevicesToSerialParams(availableDevices);
				Map<String, List<ExtendedSerialParameter>> groupedDevices = groupDeviceForPolling(devices);
				
				GroupedDeviceWorker groupedWorker = new GroupedDeviceWorker(groupedDevices);
				PollingResponseHandler handler = new PollingResponseHandler();
				handler.setFrame((PollingIFrame)getMe());
				groupedWorker.setResponseHandler(handler);
				groupedWorker.setRefreshFrequency(frequency);
				groupedWorker.execute();
				
				worker = groupedWorker;
				worker.execute();
			}
		});
		btnNewButton.setBounds(10, 11, 110, 23);
		panel.add(btnNewButton);
		
		JButton btnStopPolling = new JButton("Stop Polling");
		btnStopPolling.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnStopPolling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(worker == null){
					JOptionPane.showMessageDialog(getMe(),
							"Not Started!!!", "Start",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					closeWorker();
					JOptionPane.showMessageDialog(getMe(),
							"Poller Stopper!!!", "Stopped",
							JOptionPane.INFORMATION_MESSAGE);
					logger.info("Poller intentionally");
				}
			}
		});
		btnStopPolling.setBounds(141, 11, 110, 23);
		panel.add(btnStopPolling);
		
		JButton btnReloadDevices = new JButton("Reload devices");
		btnReloadDevices.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnReloadDevices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Want to reload?",
						"Poller will be stopped!!!",
						JOptionPane.OK_CANCEL_OPTION);
				
				if(option == JOptionPane.OK_OPTION){
					closeWorker();
					logger.info("Poller stopped for reloading");
					boolean dbConfig = TempDataManager.checkDBConfigFile();
					if(!dbConfig){
						JOptionPane.showMessageDialog(getMe(),
								"Database not configured", "Config",
								JOptionPane.ERROR_MESSAGE);
					} else {
						loadActiveDevices(getTable());
					}
				}
			}
		});
		btnReloadDevices.setBounds(271, 11, 122, 23);
		panel.add(btnReloadDevices);
		
		comboBoxPorts = new JComboBox<String>();
		comboBoxPorts.setBounds(458, 12, 64, 20);
		String[] ports = EMSUtility.getAvailablePort();
		EMSSwingUtils.addItemsComboBox(comboBoxPorts, 0, ports);
		panel.add(comboBoxPorts);
		
		JLabel lblPorts = new JLabel("Port(s)");
		lblPorts.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPorts.setHorizontalAlignment(SwingConstants.CENTER);
		lblPorts.setBounds(403, 13, 45, 19);
		panel.add(lblPorts);
		
		JLabel lblFrequency = new JLabel("Frequency(m)");
		lblFrequency.setHorizontalAlignment(SwingConstants.CENTER);
		lblFrequency.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblFrequency.setBounds(546, 13, 83, 19);
		panel.add(lblFrequency);
		
		comboBox = new JComboBox<String>();
		comboBox.setModel(new DefaultComboBoxModel<String>(new String[] { "1","2",
				"5", "10", "15", "30", "60" }));
		comboBox.setBounds(639, 12, 57, 20);
		panel.add(comboBox);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new CompoundBorder(null,
				new EtchedBorder(EtchedBorder.LOWERED, null, null)),
				"Active Devices", TitledBorder.LEADING, TitledBorder.TOP, null,
				new Color(0, 0, 0)));
		panel_1.setVerifyInputWhenFocusTarget(false);
		panel_1.setBounds(0, 56, 759, 407);
		getContentPane().add(panel_1);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));
		
		table = new JTable();
		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"UNIQUEID", "DEVICEID", "DEVICE NAME", "STATUS"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(106);
		table.getColumnModel().getColumn(1).setPreferredWidth(93);
		table.getColumnModel().getColumn(2).setPreferredWidth(149);
		table.getColumnModel().getColumn(3).setPreferredWidth(99);
		
		JScrollPane scrollPane = new JScrollPane(table);
		panel_1.add(scrollPane);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0), 0, true));
		panel_2.setBounds(10, 474, 739, 29);
		getContentPane().add(panel_2);
		panel_2.setLayout(null);
		
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		progressBar.setIndeterminate(true);
		progressBar.setBounds(178, 11, 367, 14);
		panel_2.add(progressBar);
		
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public JTable getTable() {
		return table;
	}

	public void closeWorker(){
		if(worker != null){
			worker.cancel(true);
			worker = null;
			progressBar.setVisible(false);
		}
	}
	
	public boolean checkDBConnection(){
		boolean dbConfig = TempDataManager.checkDBConfigFile();
		if(!dbConfig){
			JOptionPane.showMessageDialog(getMe(),
					"Database not configured", "Config",
					JOptionPane.ERROR_MESSAGE);
		}
		return dbConfig;
	}
	
	public void loadActiveDevices(final JTable table){
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.getDataVector().removeAllElements();
		model.fireTableDataChanged();
		
		List<DeviceDetailsDTO> deviceList = DBConnectionManager
				.getAvailableDevices(QueryConstants.SELECT_ENABLED_ENDEVICES);
		
		for(DeviceDetailsDTO device : deviceList){
			model.insertRow(device.getRowIndex(),
					new Object[] { device.getUniqueId(), device.getDeviceId(),
							device.getDeviceName(), "Active" });
		}
		logger.debug("Available devices are loaded");
		model.fireTableDataChanged();
		
		//Set row colour 
		table.setDefaultRenderer(Object.class, new JTableRenderer());
	}
	
	public JInternalFrame getMe(){
		return this;
	}
	
	public JComboBox<String> getComboBoxPorts() {
		return comboBoxPorts;
	}

	public JComboBox<String> getComboBox() {
		return comboBox;
	}
	
	protected void finalize() throws Throwable {
		if(worker != null)
			worker.cancel(true);
	}

	@Override
	public void releaseResource() {
		if (worker != null)
			worker.cancel(true);
	}
}
