package com.ems.util;

import static com.ems.util.EMSUtility.processRegistersForDashBoard;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.GroupDTO;
import com.ems.UI.dto.GroupsDTO;
import com.ems.UI.dto.PollingDetailDTO;
import com.ems.constants.EmsConstants;
import com.ems.db.DBConnectionManager;

public abstract class EMSSwingUtils {

	private static final Logger logger = LoggerFactory.getLogger(EMSSwingUtils.class);

	/**
	 * @param combo
	 * @param arg
	 * 
	 *            Adds item to Combobox
	 */
	public static void addItemsComboBox(JComboBox<String> combo, int selectedIndex, String... arg) {
		if (arg != null && combo != null) {
			for (String port : arg) {
				combo.addItem(port);
			}
			if (arg.length == 0)
				selectedIndex = -1;

			combo.setSelectedIndex(selectedIndex);
		}
	}

	/**
	 * @param combo
	 * @param arg
	 * 
	 *            Adds item to Combobox
	 */
	public static void addItemsComboBox(JComboBox<String> combo, int selectedIndex, int... arg) {
		if (arg != null & combo != null) {
			for (int item : arg) {
				combo.addItem(String.valueOf(item));
			}
			combo.setSelectedIndex(selectedIndex);
		}
	}

	public static KeyAdapter getNumericListener() {
		return new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char vChar = e.getKeyChar();
				if (!(Character.isDigit(vChar) || (vChar == KeyEvent.VK_BACK_SPACE) || (vChar == KeyEvent.VK_DELETE))) {
					e.consume();
				}
			}
		};
	}

	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public static void setMaximizedSize(JInternalFrame frame) {
		Dimension dim = EMSSwingUtils.getScreenSize();
		frame.setBounds(0, 0, (int) dim.getWidth() - 20, (int) dim.getHeight() - 80);
	}

	public static JLabel getDeviceDetailLabel(ExtendedSerialParameter device) {
		StringBuilder builder = new StringBuilder();
		builder.append(
				"<html><head><style>body{background-color:#B3C4BC;}div{background-color:#B3C4BC;width:200px;border:3px solid darkgrey;padding:1px;");
		builder.append(
				"margin: 1px;text-align:center;}table#table{width:100%;margin-left:20%;margin-right:15%;}tr{background-color: #B3B3C4;color: black;font-family: Times New Roman, Georgia, Serif; font-size: 16pt;}</style></head><body>");
		builder.append("<div><h3>" + device.getDeviceName() + "</h3><hr><center><table id='table'>");

		Map<String, String> registerValue = null;
		
		//if request status is success then process else load from DB
		if(device.isStatus() 
				|| (device.isSplitJoin() 
						&& EMSUtility.splitJoinStatus(device.getSplitJoinDTO().getStatus()))){//Modbus Request is success
			registerValue = processRegistersForDashBoard(device);
		} else {
			//Load recent polling response from DB for the first time;
			List<PollingDetailDTO> list = DBConnectionManager.
					fetchRecentPollingDetails(device.getUniqueId());
			if(list.size() > 0){
				Properties props = EMSUtility.loadProperties(list.get(0).getUnitresponse());
				registerValue = EMSUtility.convertProp2Map(props);
			}
		}
		
		Map<String, String> mappings = EMSUtility.getOrderedProperties(device);
		
		logger.trace("Processed register : {} Mapping : {}", registerValue, mappings);

		for (Entry<String, String> memory : mappings.entrySet()) {
			// Skip memory mapping record whose value is "NoMap"
			if (!EmsConstants.NO_MAP.equalsIgnoreCase(memory.getValue().trim())) {
				builder.append("<tr><td align='center'>");
				builder.append(memory.getValue());
				String value = null;

				try {
					String lookup = String.valueOf(Integer.valueOf(memory.getKey()));
					value = registerValue.get(lookup);
					logger.trace("looked up values : {}", value);
				} catch (Exception e) {
					logger.error("{}", e);
					value = "0.00";
				}

				builder.append("</td><td align='center'>" + (value == null ? "0.00" : value) + "</td></tr>");
			}
		}
		builder.append("</table></center></div></body></html>");
		logger.trace("prepared device label : {}", builder.toString());
		JLabel label = new JLabel(builder.toString());
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}

	public static String getAvlConfigureDevicesString() {
		int configured = DBConnectionManager.getConfiguredDeviceCount();
		int total = ConfigHelper.getDefaultDevices();
		StringBuilder builder = new StringBuilder();
		builder.append(configured);
		builder.append("/");
		builder.append(total);
		builder.append(" Device Configured");

		return builder.toString();
	}

	public static GroupsDTO getAllGroupNodes(JTree tree) {
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		GroupsDTO groups = (GroupsDTO) root.getUserObject();
		groups.setGroups(new ArrayList<GroupDTO>());

		int childCount = root.getChildCount();
		for (int i = 0; i < childCount; i++) {
			DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) root.getChildAt(i);
			GroupDTO group = (GroupDTO) groupNode.getUserObject();
			group.setDevices(new ArrayList<DeviceDetailsDTO>());
			groups.getGroups().add(group);

			int leafCount = groupNode.getChildCount();
			for (int j = 0; j < leafCount; j++) {
				DefaultMutableTreeNode child = (DefaultMutableTreeNode) groupNode.getChildAt(j);
				DeviceDetailsDTO device = (DeviceDetailsDTO) child.getUserObject();
				group.getDevices().add(device);
			}
		}

		return groups;
	}

	public static void addTrayIcon(String toolTip) {
		if (!SystemTray.isSupported()) {
			logger.debug("SystemTray is not supported");
			return;
		}

		try {
			SystemTray tray = SystemTray.getSystemTray();
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			URL url = EMSSwingUtils.class.getResource("/com/ems/resources/gnome-monitor.png");
			Image image = toolkit.getImage(url);
			TrayIcon icon = new TrayIcon(image, toolTip);
			icon.setImageAutoSize(true);
			tray.add(icon);
		} catch (AWTException e) {
			logger.error("{}", e);
		}
	}
}
