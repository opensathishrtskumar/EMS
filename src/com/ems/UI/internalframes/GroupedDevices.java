package com.ems.UI.internalframes;

import static com.ems.util.EMSSwingUtils.getDeviceDetailLabel;
import static com.ems.util.EMSUtility.groupDeviceForPolling;
import static com.ems.util.EMSUtility.mapDevicesToSerialParams;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.dto.GroupDTO;
import com.ems.UI.dto.GroupsDTO;
import com.ems.UI.swingworkers.GroupedDeviceWorker;
import com.ems.concurrency.ConcurrencyUtils;
import com.ems.response.handlers.DashboardResponseHandler;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;

public class GroupedDevices extends JInternalFrame implements AbstractIFrame{
	private static final Logger logger = LoggerFactory.getLogger(GroupedDevices.class);
	private static final long serialVersionUID = 1L;
	private GroupedDeviceWorker worker = null;

	/**
	 * Create the frame.
	 */
	public GroupedDevices() {
		//Dont need close button
		setIconifiable(true);
		setFrameIcon(new ImageIcon(GroupedDevices.class.getResource("/com/ems/resources/group.png")));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setClosable(true);
		setResizable(true);
		setMaximizable(true);
		
		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameActivated(InternalFrameEvent arg0) {
			}
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
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
		setBounds(0, 0, 100, 100);
		
		
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
		tabbedPane.setToolTipText("Devices by group");
		panel.add(tabbedPane);

		addContent(tabbedPane);
		EMSSwingUtils.setMaximizedSize(this);
	}

	private void addContent(JTabbedPane tabbedPane){

		GroupsDTO groups = EMSUtility.fetchGroupedDevices();
		boolean groupConfigured = false;

		if(groups != null){
			List<GroupDTO> groupList = groups.getGroups();
			if(groupList != null){

				final List<ExtendedSerialParameter> mainList = new ArrayList<ExtendedSerialParameter>();
				
				for(GroupDTO group : groupList){
					final JPanel panel_1 = new JPanel();
					tabbedPane.addTab(group.getGroupName(), null, new JScrollPane(panel_1), group.getGroupDescription());
					panel_1.setLayout(new GridLayout());

					List<DeviceDetailsDTO> deviceList = group.getDevices();
					if(deviceList != null){
						List<ExtendedSerialParameter> list = mapDevicesToSerialParams(deviceList);
						List<Future<Object>> tasks = new ArrayList<>();
						
						for(final ExtendedSerialParameter device : list){
							mainList.add(device);
							
							//Load device details label asynchronously
							Callable<Object> deviceAdder = new Callable<Object>() {
								@Override
								public String call() throws Exception {
									JScrollPane deviceDetail = getDeviceDetailJScrollPane(device);
									device.setPanel(deviceDetail);
									panel_1.add(deviceDetail);
									logger.trace("Grouped device added {}" , device);
									return "Grouped device added successfully...";
								}
							};
							
							Future<Object> future = ConcurrencyUtils.execute(deviceAdder);
							logger.trace("Grouped device Task submitted for Device {} ",device.getUniqueId());
							tasks.add(future);
						}
						
						//Wait for tasks if any
						/*for(Future<Object> future : tasks){
							try {future.get();} catch (Exception e) {logger.error("{}",e);}
						}*/
					}

					groupConfigured = true;
				}
				
				logger.debug("Grouped devices added for polling ");
				
				Map<String, List<ExtendedSerialParameter>> groupedDevices = groupDeviceForPolling(mainList);
				String refreshFreq = ConfigHelper.getDashboardFrequency();
				worker = new GroupedDeviceWorker(groupedDevices);
				worker.setRefreshFrequency(Integer.parseInt(refreshFreq) * 60 * 1000);
				DashboardResponseHandler handler = new DashboardResponseHandler();
				worker.setResponseHandler(handler);
				worker.execute();
				
				logger.debug("Poller initiated for Grouped Devices screen");
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

	private void killWorker(){
		if(worker != null){
			worker.cancel(true);
			worker = null;
		}
	}
	
	public final JInternalFrame getMe(){
    	return this;
    }
	
	@Override
	protected void finalize() throws Throwable {
		killWorker();
	}

	@Override
	public void releaseResource() {
		killWorker();
	}
}
