package com.ems.UI.internalframes;

import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;
import static com.ems.constants.MessageConstants.DASHBOARD_REFRESHFREQUENCY_KEY;
import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.util.EMSSwingUtils.getDeviceDetailLabel;
import static com.ems.util.EMSUtility.groupDeviceForPolling;
import static com.ems.util.EMSUtility.mapDevicesToSerialParams;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.custom.components.LoginDialog;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.GroupDTO;
import com.ems.UI.dto.GroupsDTO;
import com.ems.UI.swingworkers.GroupedDeviceWorker;
import com.ems.response.handlers.DashboardResponseHandler;
import com.ems.tmp.datamngr.TempDataManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GroupedDevices extends JDialog {
	private static final Logger logger = LoggerFactory.getLogger(GroupedDevices.class);
	private static final long serialVersionUID = 1L;
	private Properties config = TempDataManager.retrieveTempConfig(MAIN_CONFIG);
	private GroupedDeviceWorker worker = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GroupedDevices frame = new GroupedDevices(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GroupedDevices(Frame parent) {
		super(parent, "Grouped Devices", true);
		
		//Dont need close button
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Confirm to close?",
						"Exit", JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION){
					logger.info("Closing Grouped devices frame....");
					killWorker();
					dispose();
				}
			}
		});
		
		setTitle("Grouped Devices");
		setBounds(100, 100, 839, 495);
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setToolTipText("Devices by group");
		panel.add(tabbedPane);

		addContent(tabbedPane);
	}

	private void addContent(JTabbedPane tabbedPane){

		GroupsDTO groups = fetchGroupedDevices();

		boolean groupConfigured = false;

		if(groups != null){
			List<GroupDTO> groupList = groups.getGroups();
			if(groupList != null){

				List<ExtendedSerialParameter> mainList = new ArrayList<ExtendedSerialParameter>();
				
				for(GroupDTO group : groupList){
					JPanel panel_1 = new JPanel();
					tabbedPane.addTab(group.getGroupName(), null, new JScrollPane(panel_1), group.getGroupDescription());
					panel_1.setLayout(new GridLayout());

					List<DeviceDetailsDTO> deviceList = group.getDevices();
					if(deviceList != null){
						List<ExtendedSerialParameter> list = mapDevicesToSerialParams(deviceList);

						for(ExtendedSerialParameter device : list){
							JScrollPane deviceDetail = getDeviceDetailJScrollPane(device);
							device.setPanel(deviceDetail);
							mainList.add(device);
							panel_1.add(deviceDetail);
						}
					}

					groupConfigured = true;
				}
				
				Map<String, List<ExtendedSerialParameter>> groupedDevices = groupDeviceForPolling(mainList);
				String refreshFreq = config.getProperty(DASHBOARD_REFRESHFREQUENCY_KEY, String.valueOf(DASHBOARD_REFRESH_FREQUENCY));
				worker = new GroupedDeviceWorker(groupedDevices);
				worker.setRefreshFrequency(Integer.parseInt(refreshFreq) * 60 * 1000);
				DashboardResponseHandler handler = new DashboardResponseHandler();
				worker.setResponseHandler(handler);
				worker.execute();
				
			}
		}

		if(!groupConfigured){
			JPanel panel_1 = new JPanel();
			tabbedPane.addTab("Empty", null, panel_1, "No group configured");
			panel_1.setLayout(new GridLayout());
		}
	}


	private JScrollPane getDeviceDetailJScrollPane(ExtendedSerialParameter device){
		JLabel label = getDeviceDetailLabel(device);
		JScrollPane scrollPane = new JScrollPane(label);
		return scrollPane;
	}

	private GroupsDTO fetchGroupedDevices(){

		List<GroupDTO> group = new ArrayList<GroupDTO>();

		String deviceJson = null;

		try {
			byte[] bytes = Files.readAllBytes(new File("C:\\Users\\Gokul.m\\Desktop\\json.txt").toPath());
			deviceJson = new String(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Gson gson = new GsonBuilder().create();

		for(int i=0;i<5;i++){
			GroupDTO g = new GroupDTO();
			g.setGroupName("Group " + i);
			g.setGroupDescription("description of group " + i);
			ArrayList<DeviceDetailsDTO>  devices = new ArrayList<DeviceDetailsDTO>();

			DeviceDetailsDTO d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + i);
			d.setDeviceId(3);
			devices.add(d);

			d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + i);
			d.setDeviceId(4);
			devices.add(d);

			/*d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + i);
			d.setDeviceId(5);
			devices.add(d);

			d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + i);
			d.setDeviceId(6);
			devices.add(d);

			d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + i);
			d.setDeviceId(6);
			devices.add(d);

			d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + (i + 2));
			d.setDeviceId(6);
			devices.add(d);

			d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + (i + 2));
			d.setDeviceId(6);
			devices.add(d);

			d = gson.fromJson(deviceJson, DeviceDetailsDTO.class);
			d.setDeviceName("Device " + (i + 2));
			d.setDeviceId(6);
			devices.add(d);*/

			g.setDevices(devices);
			group.add(g);
		}

		GroupsDTO groups = new GroupsDTO();
		groups.setGroups(group);
		groups.setTimestamp(System.currentTimeMillis());

		return groups;
	}

	private void killWorker(){
		if(worker != null){
			worker.cancel(true);
			worker = null;
		}
	}
	
	public final JDialog getMe(){
    	return this;
    }
	
	@Override
	protected void finalize() throws Throwable {
		killWorker();
	}
}
