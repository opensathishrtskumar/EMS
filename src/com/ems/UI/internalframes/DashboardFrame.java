package com.ems.UI.internalframes;

import static com.ems.constants.LimitConstants.DASHBOARD_REFRESH_FREQUENCY;
import static com.ems.constants.LimitConstants.DEFAULT_COMPANY_NAME;
import static com.ems.constants.MessageConstants.COMPANYNAME_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_DEVICES_KEY;
import static com.ems.constants.MessageConstants.DASHBOARD_REFRESHFREQUENCY_KEY;
import static com.ems.constants.MessageConstants.SEMICOLON;
import static com.ems.db.DBConnectionManager.getDashBoardInclauseQuery;
import static com.ems.db.DBConnectionManager.mapResultToDeviceDetail;
import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.util.EMSSwingUtils.centerFrame;
import static com.ems.util.EMSSwingUtils.getDeviceDetailLabel;
import static com.ems.util.EMSUtility.DASHBOARD_FMT;
import static com.ems.util.EMSUtility.DASHBOARD_POLLED_FMT;
import static com.ems.util.EMSUtility.getFormattedDate;
import static com.ems.util.EMSUtility.groupDeviceForPolling;
import static com.ems.util.EMSUtility.mapDevicesToSerialParams;

import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.custom.components.JMarqueeLabel;
import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.swingworkers.GroupedDeviceWorker;
import com.ems.db.DBConnectionManager;
import com.ems.response.handlers.DashboardResponseHandler;
import com.ems.tmp.datamngr.TempDataManager;

public class DashboardFrame extends JInternalFrame {
	private static final Logger logger = LoggerFactory
			.getLogger(DashboardFrame.class);

	private static final long serialVersionUID = 1L;
	private Properties config = TempDataManager.retrieveTempConfig(MAIN_CONFIG);
	private JPanel panelDevice = null;
	private GroupedDeviceWorker worker = null;
	private JLabel lblTime = null;

	/**
	 * Create the frame.
	 */
	public DashboardFrame() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(DashboardFrame.class.getResource("/com/ems/resources/agt_home.png")));
		setClosable(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		String companyName = config.getProperty(COMPANYNAME_KEY,DEFAULT_COMPANY_NAME);
		StringBuilder builder = new StringBuilder();
		builder.append("Dashboard" );
		setTitle(builder.toString());
		setBounds(100, 100, 856, 631);
		getContentPane().setLayout(null);
		centerFrame(getMe());
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 840, 38);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JMarqueeLabel lblMarque = new JMarqueeLabel(companyName);
		lblMarque.setBorder(null);
		lblMarque.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 15));
		lblMarque.setBounds(277, 11, 282, 22);
		panel.add(lblMarque);
		
		JLabel lblPolledOn = new JLabel("Refreshed on");
		lblPolledOn.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPolledOn.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPolledOn.setBounds(603, 15, 94, 14);
		panel.add(lblPolledOn);
		
		String polled = getFormattedDate(DASHBOARD_POLLED_FMT);
		lblTime = new JLabel(polled);
		lblTime.setHorizontalAlignment(SwingConstants.LEFT);
		lblTime.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblTime.setBounds(707, 15, 123, 14);
		panel.add(lblTime);
		
		String today = getFormattedDate(DASHBOARD_FMT);
		JLabel lblToday = new JLabel("Date " + today);
		lblToday.setFont(new Font("Tahoma", Font.PLAIN, 10));
		lblToday.setHorizontalAlignment(SwingConstants.CENTER);
		lblToday.setBounds(10, 11, 100, 22);
		panel.add(lblToday);
		
		panelDevice = new JPanel();
		panelDevice.setBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)));
		panelDevice.setBounds(0, 38, 840, 554);
		getContentPane().add(panelDevice);
		panelDevice.setLayout(new GridLayout(2, 3, 5, 5));
		
		initializeDevices(panelDevice);
		
		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				killWorker();
			}
		});
	}
	
	private void initializeDevices(JPanel panelDevice){
		List<DeviceDetailsDTO> devices = getConfiguredDevice();
		
		if(devices == null || devices.size() == 0){
			JOptionPane.showMessageDialog(getMe(), "No device configured",
					"Dashboard", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		List<ExtendedSerialParameter> list = mapDevicesToSerialParams(devices);
		
		for(ExtendedSerialParameter device : list){
			JScrollPane deviceDetail = getDeviceDetailJScrollPane(device);
			device.setPanel(deviceDetail);
			panelDevice.add(deviceDetail);
		}
		
		Map<String, List<ExtendedSerialParameter>> groupedDevices = groupDeviceForPolling(list);
		String refreshFreq = config.getProperty(DASHBOARD_REFRESHFREQUENCY_KEY, String.valueOf(DASHBOARD_REFRESH_FREQUENCY));
		worker = new GroupedDeviceWorker(groupedDevices);
		worker.setRefreshFrequency(Integer.parseInt(refreshFreq) * 60 * 1000);
		DashboardResponseHandler handler = new DashboardResponseHandler();
		handler.setPolledOn(lblTime);
		worker.setResponseHandler(handler);
		worker.execute();
		logger.info("Dashboardworker created...");
	}
	
	private JScrollPane getDeviceDetailJScrollPane(ExtendedSerialParameter device){
		JLabel label = getDeviceDetailLabel(device);
		JScrollPane scrollPane = new JScrollPane(label);
		return scrollPane;
	}
	
	private List<DeviceDetailsDTO> getConfiguredDevice(){
		String[] dashBoardDevices = config.getProperty(DASHBOARD_DEVICES_KEY, "").split(SEMICOLON);
		List<DeviceDetailsDTO> list = null;
		if(dashBoardDevices != null && dashBoardDevices.length != 0){
			String query = getDashBoardInclauseQuery(dashBoardDevices);
			try {
				Connection connection = DBConnectionManager.getConnection();
				PreparedStatement ps = connection.prepareStatement(query);
				int counter = 0;
				for(String deviceId : dashBoardDevices){
					if(deviceId != null && !deviceId.trim().isEmpty())
						ps.setLong(++counter, Long.parseLong(deviceId));
				}
				
				if(counter != 0){
					ResultSet resultSet = ps.executeQuery();
					logger.info("Devices retrieved for Dashboard");
					list = mapResultToDeviceDetail(resultSet);
				}
			} catch (Exception e) {
				logger.error("{}",e);
				logger.error("Failed to load dash board devices : {}",e.getLocalizedMessage());
			}
		} else {
			logger.info("No device configured for Dashboard");
		}
		
		if(list == null){
			logger.debug("Creating empty device list");
			list = new ArrayList<DeviceDetailsDTO>();
		}
		return list;
	}
	
	private JInternalFrame getMe(){
		return this;
	}
	
	private void killWorker(){
		if(worker != null){
			worker.cancel(true);
			worker = null;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		killWorker();
	}
}
