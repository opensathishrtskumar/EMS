package com.ems.UI.internalframes;

import static com.ems.constants.QueryConstants.SELECT_ENABLED_ENDEVICES;
import static com.ems.util.EMSSwingUtils.centerFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.DeviceDetailsDTO;
import com.ems.constants.EmsConstants;
import com.ems.db.DBConnectionManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;

public class LiveChartIFrame extends JInternalFrame {
	private static final Logger logger = LoggerFactory.getLogger(LiveChartIFrame.class);
	private static final long serialVersionUID = 6606072613952247592L;
	private JComboBox<String> comboBoxDevice;
	private List<DeviceDetailsDTO> deviceList;
	private Map<String, DeviceDetailsDTO> deviceMap;
	private MeterUsageChart monitorPanel = null;

	/**
	 * Create the frame.
	 */
	public LiveChartIFrame() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(LiveChartIFrame.class.getResource("/com/ems/resources/system_16x16.gif")));
		setTitle("Live Monitoring");
		setClosable(true);
		setBounds(100, 100, 949, 680);
		centerFrame(getMe());
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);

		addInternalFrameListener(new InternalFrameAdapter() {

			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Confirm Close?", "Exit",
						JOptionPane.YES_NO_OPTION);
				if (option == JOptionPane.YES_OPTION) {
					dispose();
				}
				
				if(monitorPanel != null){
					monitorPanel.stop();
				}
			}

			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {

			}
		});
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel_1.add(panel, BorderLayout.NORTH);
		panel.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)),
				"Report criteria", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblDevice = new JLabel("Device Name");
		lblDevice.setSize(new Dimension(100, 25));
		lblDevice.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDevice.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblDevice);

		comboBoxDevice = new JComboBox<String>();
		comboBoxDevice.setSize(new Dimension(100, 25));
		comboBoxDevice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {

			}
		});

		panel.add(comboBoxDevice);

		JButton btnLoad = new JButton("Monitor");
		btnLoad.setSize(new Dimension(100, 25));
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				DeviceDetailsDTO device = deviceMap.get(comboBoxDevice.getSelectedItem());
				
				if (monitorPanel != null) {
					panel_1.remove(monitorPanel);
					monitorPanel.stop();
					
					JOptionPane.showMessageDialog(getMe(), "Reloading Live Monitoring " + device.getDeviceName());
				}

				String[] memoryMapping = loadMemoryMapping(device);

				ChartDTO dto = new ChartDTO().setDeviceName(device.getDeviceName()).setMaxAge(100000)
						.setSeriesName(memoryMapping).setxAxisName("Readings").setyAxisName("Time")
						.setDeviceUniqueId(device.getUniqueId())
						.setInterval(ConfigHelper.getLiveRefreshFrequency() * 1000).setParameter(device);

				monitorPanel = new MeterUsageChart(dto);
				monitorPanel.start();
				panel_1.add(monitorPanel, BorderLayout.CENTER);
				
				panel_1.repaint();
				panel_1.revalidate();
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setSize(new Dimension(100, 25));
		panel.add(scrollPane);
		panel.add(btnLoad);
		loadAvailableActiveDevices(comboBoxDevice);
	}

	public JComboBox<String> getComboBoxDevice() {
		return comboBoxDevice;
	}

	public void loadAvailableActiveDevices(JComboBox<String> devices) {

		try {
			deviceList = DBConnectionManager.getAvailableDevices(SELECT_ENABLED_ENDEVICES);
			String[] deviceIds = new String[deviceList.size()];
			int index = 0;
			deviceMap = new HashMap<>(deviceList.size());
			for (DeviceDetailsDTO unit : deviceList) {
				deviceIds[index++] = unit.getDeviceName();
				deviceMap.put(unit.getDeviceName(), unit);
			}

			EMSSwingUtils.addItemsComboBox(devices, 0, deviceIds);
			devices.revalidate();
			devices.repaint();
		} catch (Exception e) {
			logger.error("{}", e);
		}
	}

	private String[] loadMemoryMapping(DeviceDetailsDTO device) {
		String[] mappings = new String[] {};
		Properties prop = EMSUtility.loadProperties(device.getMemoryMapping());
		Collection<Object> collection = prop.values();
		collection.remove(EmsConstants.NO_MAP);//NoMap not required in Chart
		mappings = collection.toArray(new String[prop.values().size()]);
		return mappings;
	}

	public JInternalFrame getMe() {
		return this;
	}
}
