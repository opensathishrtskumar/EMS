package com.ems.panels;

import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.tmp.datamngr.TempDataManager.writeTempConfig;
import static com.ems.util.EMSUtility.isNullEmpty;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.GroupDTO;
import com.ems.UI.dto.GroupsDTO;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSUtility;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ManageGroup extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ManageGroup.class);
	private static final long serialVersionUID = 1L;
	private JTextField txtGroup;
	private JTextField textGrpDesc;
	private JList<GroupDTO> grpList;

	/**
	 * Create the panel.
	 */
	public ManageGroup() {
		setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Manage Group", TitledBorder.LEADING,
						TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EtchedBorder(EtchedBorder.LOWERED, null, null)));
		setLayout(null);

		JLabel lblGroupName = new JLabel("Group Name");
		lblGroupName.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGroupName.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblGroupName.setBounds(42, 35, 93, 25);
		add(lblGroupName);

		txtGroup = new JTextField();
		txtGroup.setBounds(178, 34, 124, 26);
		add(txtGroup);
		txtGroup.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		lblDescription.setHorizontalAlignment(SwingConstants.RIGHT);
		lblDescription.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblDescription.setBounds(42, 83, 93, 25);
		add(lblDescription);

		textGrpDesc = new JTextField();
		textGrpDesc.setBounds(178, 82, 216, 26);
		add(textGrpDesc);
		textGrpDesc.setColumns(10);

		JLabel lblAvailableGroup = new JLabel("Available Group");
		lblAvailableGroup.setHorizontalAlignment(SwingConstants.RIGHT);
		lblAvailableGroup.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblAvailableGroup.setBounds(42, 170, 93, 23);
		add(lblAvailableGroup);

		grpList = new JList<GroupDTO>();
		grpList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				GroupDTO selectedGroup = grpList.getSelectedValue();	
				if(selectedGroup != null){
					logger.debug("Group selected " + selectedGroup.getGroupName());
					txtGroup.setText(selectedGroup.getGroupName());
					textGrpDesc.setText(selectedGroup.getGroupDescription());
				}
			}
		});
		grpList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		grpList.setModel(getDeviceGroups());
		grpList.setBounds(173, 152, 124, -41);

		JScrollPane scrollPane = new JScrollPane(grpList);
		scrollPane.setBounds(178, 172, 216, 119);
		add(scrollPane);

		JButton btnAddGroup = new JButton("Add Group");
		btnAddGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultListModel<GroupDTO> listModel = (DefaultListModel<GroupDTO>) grpList.getModel();

				String groupName = txtGroup.getText();
				String groupDesc = textGrpDesc.getText();

				logger.info("Adding group : " + groupName);
				
				if (isNullEmpty(groupName) || isNullEmpty(groupDesc)) {
					JOptionPane.showMessageDialog(getMe(), "Values can't be empty", "Grouping",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				int size = listModel.getSize();

				for (int i = 0; i < size; i++) {
					GroupDTO temp = listModel.getElementAt(i);
					if (temp.getGroupName().equalsIgnoreCase(groupName)) {
						JOptionPane.showMessageDialog(getMe(), "Group exist!!", "Grouping",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				}

				GroupDTO newGroup = new GroupDTO();
				newGroup.setGroupName(groupName);
				newGroup.setGroupDescription(groupDesc);
				newGroup.setDevices(new ArrayList<DeviceDetailsDTO>());
				logger.info("Group Added : " + groupName);
				listModel.addElement(newGroup);
			}
		});
		btnAddGroup.setBounds(456, 84, 99, 23);
		add(btnAddGroup);

		JButton btnRemoveGroup = new JButton("Remove");
		btnRemoveGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selected = grpList.getSelectedIndex();

				if (selected < 0) {
					JOptionPane.showMessageDialog(getMe(), "Please select any Group", "Grouping",
							JOptionPane.WARNING_MESSAGE);
					return;
				} else {
					GroupDTO selectedGroup = grpList.getSelectedValue();
					int option = JOptionPane.showConfirmDialog(getMe(),
							"Confirm to delete Group " + selectedGroup.getGroupName() + "?", "Grouping",
							JOptionPane.OK_CANCEL_OPTION);
					
					if(option == JOptionPane.OK_OPTION){
						DefaultListModel<GroupDTO> listModel = (DefaultListModel<GroupDTO>) grpList.getModel();
						listModel.removeElement(grpList.getSelectedValue());
						
						grpList.setModel(listModel);
						
					}
				}
			}
		});
		btnRemoveGroup.setBounds(456, 268, 99, 23);
		add(btnRemoveGroup);
		
		JButton btnEditGroup = new JButton("Edit Group");
		btnEditGroup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultListModel<GroupDTO> listModel = (DefaultListModel<GroupDTO>) grpList.getModel();
				int selected = grpList.getSelectedIndex();
				if(selected < 0){
					JOptionPane.showMessageDialog(getMe(), "Please select Group to edit!!", "Grouping",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
			
				if(validateGroupInfo()){
					JOptionPane.showMessageDialog(getMe(), "Values can't be empty", "Grouping",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				GroupDTO group = listModel.get(selected);
				group.setGroupName(txtGroup.getText());
				group.setGroupDescription(textGrpDesc.getText());
				listModel.setElementAt(group, selected);
				
				txtGroup.setText("");
				textGrpDesc.setText("");
			}
		});
		btnEditGroup.setBounds(568, 84, 93, 23);
		add(btnEditGroup);
		
		JButton btnSaveChanges = new JButton("Save Changes");
		btnSaveChanges.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultListModel<GroupDTO> listModel = (DefaultListModel<GroupDTO>) grpList.getModel();
				
				GroupsDTO groups = new GroupsDTO();
				groups.setTimestamp(System.currentTimeMillis());
				groups.setGroups(new ArrayList<GroupDTO>());
				
				for (int i = 0; i < listModel.getSize(); i++) {
					GroupDTO temp = listModel.getElementAt(i);
					groups.getGroups().add(temp);
				}
				
				Gson gson = new GsonBuilder().create();
				String value = gson.toJson(groups);
				Properties config = ConfigHelper.setGroupingDetails(value);
				logger.info("Configurations are saved");
				writeTempConfig(config, MAIN_CONFIG);
				
				JOptionPane.showMessageDialog(getMe(), "Saved successfully!!", "Grouping",
						JOptionPane.WARNING_MESSAGE);
			}
		});
		btnSaveChanges.setBounds(178, 330, 124, 23);
		add(btnSaveChanges);
	}

	private DefaultListModel<GroupDTO> getDeviceGroups() {
		DefaultListModel<GroupDTO> listModel = new DefaultListModel<GroupDTO>();
		GroupsDTO groups = EMSUtility.fetchGroupedDevices();
		if (groups != null) {
			List<GroupDTO> groupList = groups.getGroups();
			if (groupList != null) {
				for (GroupDTO group : groupList) {
					listModel.addElement(group);
				}
			}
		}
		return listModel;
	}

	public JPanel getMe() {
		return this;
	}
	
	private boolean validateGroupInfo(){
		String groupName = txtGroup.getText();
		String groupDesc = textGrpDesc.getText();
		return isNullEmpty(groupName) | isNullEmpty(groupDesc);
	}
	
}
