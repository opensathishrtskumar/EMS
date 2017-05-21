package com.ems.UI.custom.components;

import static com.ems.constants.LimitConstants.DASHBOARD_DEVICES_COUNT;
import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;
import static com.ems.constants.LimitConstants.DEFAULT_COMPANY_NAME;
import static com.ems.constants.LimitConstants.DEFAULT_COMPORT;
import static com.ems.constants.MessageConstants.COMPANYNAME_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_DEVICESCOUNT_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_DEVICES_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_REFRESHFREQUENCY_KEY;
import static com.ems.constants.MessageConstants.DEFAULTPORT_KEY;
import static com.ems.constants.MessageConstants.SEMICOLON;
import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.tmp.datamngr.TempDataManager.writeTempConfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.constants.MessageConstants;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.tmp.datamngr.TempDataManager;

public class PreferencesDialog extends JDialog {

	private static final long serialVersionUID = -2900310406337505075L;
	private static final Logger logger = LoggerFactory
			.getLogger(PreferencesDialog.class);
	
	private DefaultListModel<DeviceDetailsDTO> listModel = null;
	
	private Properties config = TempDataManager.retrieveTempConfig(MAIN_CONFIG);
	private JTextField textCompanyName;
	private JTextField textDefaultPort;

	/**
	 * Create the dialog.
	 */
	public PreferencesDialog(Frame parent) {
		super(parent, "Preferences", true);
		setTitle("Preferences");
		setIconImage(Toolkit.getDefaultToolkit().getImage(PreferencesDialog.class.getResource("/com/ems/resources/Service-Manager.png")));
		setBounds(100, 100, 675, 425);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOneTouchExpandable(true);
		splitPane.setBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)));
		getContentPane().add(splitPane, BorderLayout.CENTER);

		final JTree tree = new JTree();
		tree.setToggleClickCount(1);
		tree.putClientProperty("JTree.lineStyle", "Horizontal");
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouse) {
				if(mouse.getClickCount() == 2){
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
					if(node == null)
						return;
					
					if(node.getUserObject() instanceof DeviceDetailsDTO){
						DeviceDetailsDTO model = (DeviceDetailsDTO)node.getUserObject();
						String maxDashBoardDevice = config.getProperty(DASHBOARD_DEVICESCOUNT_KEY, String.valueOf(DASHBOARD_DEVICES_COUNT));
						int deviceAllowed = Integer.parseInt(maxDashBoardDevice);
						if(!listModel.contains(model) &&  listModel.size() < deviceAllowed){
							listModel.addElement(model);
							logger.debug("User object : {}" + model.getDeviceId());
							logger.debug("Clicked : {}" + tree.getSelectionPath());
						}
					}
				}
			}
		});
		
		tree.setShowsRootHandles(true);

		JScrollPane scrollPane = new JScrollPane(tree);
		splitPane.setLeftComponent(scrollPane);
		
		JPanel panel = new JPanel();
		panel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)), "Preferences", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("<html>Maximum Devices <br><center>at Dashboard</center></html>");
		lblNewLabel.setBounds(53, 66, 145, 25);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblNewLabel);
		
		String maxDashBoardDevice = config.getProperty(DASHBOARD_DEVICESCOUNT_KEY, String.valueOf(DASHBOARD_DEVICES_COUNT));
		logger.info("Maximum Dashboard devices : {}",maxDashBoardDevice);
		JLabel labelMaxDevices = new JLabel(maxDashBoardDevice);
		labelMaxDevices.setBounds(208, 66, 68, 19);
		labelMaxDevices.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		labelMaxDevices.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(labelMaxDevices);
		
		JLabel lblDevices = new JLabel("Dashboard devices");
		lblDevices.setBounds(63, 109, 114, 19);
		lblDevices.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDevices.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDevices);
		listModel = new DefaultListModel<DeviceDetailsDTO>();
        final JList list = new JList();
        list.setBounds(1, 1, 123, 65);
        list.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent mouse) {
        		if(mouse.getClickCount() == 2 && list.getSelectedValue() != null){
        			logger.debug("Selected item : {}",list.getSelectedValue());
        			listModel.removeElement(list.getSelectedValue());
        		}
        	}
        });
        list.setModel(listModel);
        panel.add(list);
        
        JScrollPane scrollPane_1 = new JScrollPane(list);
        scrollPane_1.setBounds(208, 110, 172, 92);
        scrollPane_1.setBorder(new LineBorder(new Color(130, 135, 144), 1, true));
        scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane_1);
		
		splitPane.setRightComponent(panel);
		
		JLabel lblRefreshFrequency = new JLabel("Refresh Frequency(m)");
		lblRefreshFrequency.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRefreshFrequency.setHorizontalAlignment(SwingConstants.CENTER);
		lblRefreshFrequency.setBounds(53, 214, 134, 19);
		panel.add(lblRefreshFrequency);
		
		final JComboBox<String> comboRefreshDashboard = new JComboBox<String>();
		comboRefreshDashboard.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3"}));
		comboRefreshDashboard.setBounds(208, 213, 68, 20);
		panel.add(comboRefreshDashboard);
		String refreshFreq = config.getProperty(DASHBOARD_REFRESHFREQUENCY_KEY, String.valueOf(DASHBOARD_REFRESH_FREQUENCY));
		logger.info("Dashboard refresh frequency devices : {}",refreshFreq);
		comboRefreshDashboard.setSelectedItem(refreshFreq);
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Enumeration<DeviceDetailsDTO> enums = listModel.elements();
				StringBuilder builder = new StringBuilder();
				for(;enums.hasMoreElements();){
					DeviceDetailsDTO device = enums.nextElement();
					builder.append(device.getUniqueId() +  ";");
				}
				logger.info("selected devices : {}",builder.toString());
				config.put(DASHBOARD_DEVICES_KEY, builder.toString());
				config.put(DASHBOARD_REFRESHFREQUENCY_KEY, comboRefreshDashboard.getSelectedItem());
				config.put(COMPANYNAME_KEY,textCompanyName.getText());
				config.put(DEFAULTPORT_KEY,textDefaultPort.getText());
				writeTempConfig(config, MAIN_CONFIG);
				JOptionPane.showMessageDialog(getMe(), "Saved successfully",
						"Preferences", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnSave.setBounds(104, 313, 89, 23);
		panel.add(btnSave);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getMe().dispose();
			}
		});
		btnCancel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnCancel.setBounds(208, 313, 89, 23);
		panel.add(btnCancel);
		
		JLabel lblCompanyName = new JLabel("Company Name");
		lblCompanyName.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblCompanyName.setHorizontalAlignment(SwingConstants.CENTER);
		lblCompanyName.setBounds(63, 24, 114, 14);
		panel.add(lblCompanyName);
		
		String companyName = config.getProperty(COMPANYNAME_KEY,DEFAULT_COMPANY_NAME);
		textCompanyName = new JTextField(companyName);
		textCompanyName.setBounds(208, 21, 172, 20);
		panel.add(textCompanyName);
		textCompanyName.setColumns(10);
		
		JLabel lblDefaultComPort = new JLabel("Default COM Port");
		lblDefaultComPort.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDefaultComPort.setHorizontalAlignment(SwingConstants.CENTER);
		lblDefaultComPort.setBounds(63, 257, 114, 14);
		panel.add(lblDefaultComPort);
		
		String comPort = config.getProperty(DEFAULTPORT_KEY,DEFAULT_COMPORT);
		textDefaultPort = new JTextField(comPort);
		textDefaultPort.setBounds(211, 255, 86, 20);
		panel.add(textDefaultPort);
		textDefaultPort.setColumns(10);
		
		loadExistingDevices(tree);
		
        setResizable(false);
        setLocationRelativeTo(parent);
	}
	
	public JDialog getMe(){
		return this;
	}
	
	private void loadExistingDevices(JTree tree){
		DefaultMutableTreeNode deviceRoot = new DefaultMutableTreeNode("Enabled Devices");
		List<DeviceDetailsDTO> deviceList = DBConnectionManager.getAvailableDevices(QueryConstants.SELECT_ENABLED_ENDEVICES);
		String[] configDevice = config.getProperty(MessageConstants.DASHBOARD_DEVICES_KEY,"").split(SEMICOLON);
		List<String> configDeviceList = Arrays.asList(configDevice);
		for(DeviceDetailsDTO device : deviceList){
			deviceRoot.add(new DefaultMutableTreeNode(device));
			if(configDeviceList.contains(String.valueOf(device.getUniqueId()))){
				listModel.addElement(device);
			}
		}
		tree.setModel(new DefaultTreeModel(deviceRoot));
	}
}

