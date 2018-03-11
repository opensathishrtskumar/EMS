package com.ems.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.custom.components.DnDJTree;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.GroupDTO;
import com.ems.UI.dto.GroupsDTO;
import com.ems.constants.QueryConstants;
import com.ems.db.DBConnectionManager;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GroupDevicesPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(GroupDevicesPanel.class);
	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public GroupDevicesPanel() {

		setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Group Devices", TitledBorder.LEADING,
						TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EtchedBorder(EtchedBorder.LOWERED, null, null)));
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		add(splitPane, BorderLayout.CENTER);

		JTree treeSource = new DnDJTree();
		splitPane.setLeftComponent(treeSource);

		final DnDJTree treeTarget = new DnDJTree();
		treeTarget.setTarget(true);
		treeTarget.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent mouse) {
        		DefaultTreeModel model = (DefaultTreeModel)treeTarget.getModel();
        		if(mouse.getClickCount() == 2){
            		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeTarget.getSelectionPath().getLastPathComponent();
            		logger.debug("Allow children : " + node.getAllowsChildren());
            		
            		if(node.isLeaf() && node.getUserObject() instanceof DeviceDetailsDTO){
            			logger.debug("Node can be removed " + node);
            			try {
            				model.removeNodeFromParent(node);
    					} catch (Exception e) {
    						logger.error(" Cannot remove device " + e.getLocalizedMessage());
    					}
            		}
        		}
        	}
        });
		
		splitPane.setRightComponent(treeTarget);

		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);

		JButton btnNewButton = new JButton("Save");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Gson gson = new GsonBuilder().create();
				String grpdDevices = gson.toJson(EMSSwingUtils.getAllGroupNodes(treeTarget));
				Properties props = ConfigHelper.setGroupingDetails(grpdDevices);
				TempDataManager.writeTempConfig(props, TempDataManager.MAIN_CONFIG);
				logger.info("Saved " + grpdDevices);
				JOptionPane.showMessageDialog(getMe(), "Saved successfully!!!", "Grouping",
						JOptionPane.WARNING_MESSAGE);
			}
		});
		panel.add(btnNewButton);
		init(treeSource, treeTarget);
	}
	
	public JPanel getMe() {
		return this;
	}
	
	public void init(JTree treeSource, JTree treeTarget){
		loadExistingDevices(treeSource);
		loadGroupedDevices(treeTarget);
	}
	
	private void loadGroupedDevices(JTree tree) {
		GroupsDTO groups = EMSUtility.fetchGroupedDevices();
		if (groups != null) {
			List<GroupDTO> groupList = groups.getGroups();
			GroupsDTO groupsDTO = new GroupsDTO();
			groupsDTO.setLabel("Groups");
			groupsDTO.setTimestamp(System.currentTimeMillis());
			DefaultMutableTreeNode rootGroup = new DefaultMutableTreeNode(groupsDTO);
			rootGroup.setAllowsChildren(true);

			if(groupList != null){
				for (GroupDTO group : groupList) {
					if (group.getDevices() != null) {
						DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
						groupNode.setAllowsChildren(true);
						
						for (DeviceDetailsDTO device : group.getDevices()) {
							groupNode.add(new DefaultMutableTreeNode(device));
						}
						rootGroup.add(groupNode);
					}
				}
			}

			tree.setModel(new DefaultTreeModel(rootGroup));
		}

		for (int i = 0; i < tree.getRowCount(); i++)
			tree.expandRow(i);
	}

	
	private void loadExistingDevices(JTree tree) {
		DefaultMutableTreeNode deviceRoot = new DefaultMutableTreeNode("Active Devices");
		List<DeviceDetailsDTO> deviceList = DBConnectionManager
				.getAvailableDevices(QueryConstants.SELECT_ENABLED_ENDEVICES);
		for (DeviceDetailsDTO device : deviceList) {
			deviceRoot.add(new DefaultMutableTreeNode(device));
		}
		tree.setModel(new DefaultTreeModel(deviceRoot));
	}
}
