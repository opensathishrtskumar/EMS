package com.ems.UI.internalframes;

import static com.ems.constants.EmsConstants.BAUDRATES;
import static com.ems.constants.EmsConstants.DTR;
import static com.ems.constants.EmsConstants.ENCODING;
import static com.ems.constants.EmsConstants.PARITY;
import static com.ems.constants.EmsConstants.POINTYPE;
import static com.ems.constants.EmsConstants.RTS;
import static com.ems.constants.EmsConstants.STOPBIT;
import static com.ems.constants.EmsConstants.TIMEOUT;
import static com.ems.constants.EmsConstants.WORDLENGTH;
import static com.ems.util.EMSSwingUtils.centerFrame;
import static com.ems.util.EMSSwingUtils.getNumericListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.swingworkers.PingSwingWorker;
import com.ems.util.EMSSwingUtils;
import com.ems.util.EMSUtility;
import javax.swing.ImageIcon;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;
import java.awt.Font;
import javax.swing.SwingConstants;

public class PingInternalFrame extends JInternalFrame implements AbstractIFrame {

	private static final Logger logger = LoggerFactory
			.getLogger(PingSwingWorker.class);
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("com.ems.UI.messages"); //$NON-NLS-1$

	private static final long serialVersionUID = 1L;
	private JTextField txtFieldAddress;
	private JTextField txtFieldDeviceId;
	private JSpinner spinnerLength;
	private JComboBox<String> comboBoxPointType;
	private JComboBox<String> comboBoxBaudRate;
	private JComboBox<String> comboBoxPorts;
	private JComboBox<String> comboBoxWordLength;
	private JComboBox<String> comboBoxParity;
	private JComboBox<String> comboBoxStopBit;
	private JTextArea textAreaPollingResult;
	private JComboBox<String> comboBoxEncoding;
	private JTextField txtFieldPollDelay;
	private JComboBox<String> comboBoxTimeout;

	private SwingWorker<Object, Object> pingWorker = null;

	/**
	 * Create the frame.
	 */
	public PingInternalFrame() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(PingInternalFrame.class.getResource("/com/ems/resources/system_16x16.gif")));
		setMaximizable(false);
		setClosable(true);
		setResizable(false);
		setTitle(BUNDLE.getString("PingInternalFrame.this.title")); //$NON-NLS-1$
		setBounds(100, 100, 737, 557);
		centerFrame(getMe());
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent arg0) {
				logger.info("Closing internal frame... Release resource");

				if(pingWorker != null)
					pingWorker.cancel(true);

				super.internalFrameClosing(arg0);
			}
		});

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		getContentPane().add(panel);
		panel.setLayout(new GridLayout(2, 1, 0, 0));

		JPanel panel_7 = new JPanel();
		panel.add(panel_7);
		panel_7.setLayout(new GridLayout(1, 0, 0, 0));

		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new TitledBorder(null, BUNDLE
				.getString("PingInternalFrame.panel_9.borderTitle"),
				TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		panel_7.add(panel_9);
		panel_9.setLayout(null);

		JLabel lblNewLabel_6 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_6.text")); //$NON-NLS-1$
		lblNewLabel_6.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_6.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_6.setBounds(6, 16, 97, 24);
		panel_9.add(lblNewLabel_6);

		String[] availablePorts = EMSUtility.getAvailablePort();
		comboBoxPorts = new JComboBox<String>();
		comboBoxPorts.setBounds(113, 16, 105, 24);
		EMSSwingUtils.addItemsComboBox(comboBoxPorts, 0, availablePorts);
		panel_9.add(comboBoxPorts);

		JLabel lblNewLabel_4 = new JLabel("");
		lblNewLabel_4.setBounds(6, 40, 113, 24);
		lblNewLabel_4.setVisible(false);
		panel_9.add(lblNewLabel_4);

		JLabel lblNewLabel_5 = new JLabel("");
		lblNewLabel_5.setBounds(119, 40, 113, 24);
		lblNewLabel_5.setVisible(false);
		panel_9.add(lblNewLabel_5);

		JPanel panel_8 = new JPanel();
		panel_8.setBorder(null);
		panel_7.add(panel_8);
		panel_8.setLayout(null);

		JLabel lblTimeout = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblTimeout.text")); //$NON-NLS-1$
		lblTimeout.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblTimeout.setHorizontalAlignment(SwingConstants.CENTER);
		lblTimeout.setBounds(10, 11, 72, 23);
		panel_8.add(lblTimeout);

		comboBoxTimeout = new JComboBox<String>();
		comboBoxTimeout.setBounds(107, 12, 100, 20);
		EMSSwingUtils.addItemsComboBox(comboBoxTimeout, 0, TIMEOUT);
		panel_8.add(comboBoxTimeout);

		JLabel lblNewLabel_12 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_12.text")); //$NON-NLS-1$
		lblNewLabel_12.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_12.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_12.setBounds(10, 47, 72, 14);
		panel_8.add(lblNewLabel_12);

		txtFieldPollDelay = new JTextField();
		txtFieldPollDelay.addKeyListener(getNumericListener());
		txtFieldPollDelay.setText(BUNDLE
				.getString("PingInternalFrame.textField.text")); //$NON-NLS-1$
		txtFieldPollDelay.setBounds(107, 43, 100, 20);
		panel_8.add(txtFieldPollDelay);
		txtFieldPollDelay.setColumns(10);

		JPanel panel_10 = new JPanel();
		panel_7.add(panel_10);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_6);
		panel_6.setLayout(new GridLayout(2, 1, 0, 0));

		JPanel panel_11 = new JPanel();
		panel_11.setBorder(null);
		panel_6.add(panel_11);
		panel_11.setLayout(null);

		JLabel lblAddress = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblAddress.text")); //$NON-NLS-1$
		lblAddress.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblAddress.setHorizontalAlignment(SwingConstants.CENTER);
		lblAddress.setBounds(10, 10, 67, 14);
		panel_11.add(lblAddress);

		txtFieldAddress = new JTextField();
		txtFieldAddress.addKeyListener(getNumericListener());
		txtFieldAddress.setText(BUNDLE
				.getString("PingInternalFrame.txtFieldAddress.text")); //$NON-NLS-1$
		txtFieldAddress.setBounds(96, 11, 86, 18);
		panel_11.add(txtFieldAddress);
		txtFieldAddress.setColumns(10);

		JLabel lblNewLabel_7 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_7.text")); //$NON-NLS-1$
		lblNewLabel_7.setHorizontalTextPosition(SwingConstants.LEADING);
		lblNewLabel_7.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_7.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_7.setBounds(228, 10, 67, 14);
		panel_11.add(lblNewLabel_7);

		spinnerLength = new JSpinner();
		spinnerLength.setModel(new SpinnerNumberModel(new Integer(10), new Integer(1), null, new Integer(1)));
		spinnerLength.setLocation(326, 7);
		spinnerLength.setSize(new Dimension(100, 20));
		spinnerLength.setEditor(new JSpinner.DefaultEditor(spinnerLength));
		panel_11.add(spinnerLength);

		JLabel lblNewLabel_9 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_9.text")); //$NON-NLS-1$
		lblNewLabel_9.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_9.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_9.setBounds(460, 10, 74, 14);
		panel_11.add(lblNewLabel_9);

		txtFieldDeviceId = new JTextField();
		txtFieldDeviceId.setText(BUNDLE
				.getString("PingInternalFrame.txtFieldDeviceId.text")); //$NON-NLS-1$
		txtFieldDeviceId.addKeyListener(getNumericListener());
		txtFieldDeviceId.setBounds(557, 11, 86, 18);
		panel_11.add(txtFieldDeviceId);
		txtFieldDeviceId.setColumns(10);

		JPanel panel_12 = new JPanel();
		panel_12.setBorder(null);
		panel_6.add(panel_12);
		panel_12.setLayout(null);

		comboBoxPointType = new JComboBox<String>();
		comboBoxPointType.setBounds(171, 7, 168, 20);
		comboBoxPointType.setEnabled(false);
		EMSSwingUtils.addItemsComboBox(comboBoxPointType, 2, POINTYPE);
		panel_12.add(comboBoxPointType);

		JLabel lblModbusPointType = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblModbusPointType.text")); //$NON-NLS-1$
		lblModbusPointType.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblModbusPointType.setHorizontalAlignment(SwingConstants.CENTER);
		lblModbusPointType.setBounds(39, 11, 122, 13);
		panel_12.add(lblModbusPointType);

		JLabel lblNewLabel_11 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_11.text")); //$NON-NLS-1$
		lblNewLabel_11.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_11.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_11.setBounds(373, 9, 83, 17);
		panel_12.add(lblNewLabel_11);

		comboBoxEncoding = new JComboBox<String>();
		comboBoxEncoding.setBounds(476, 7, 97, 20);
		comboBoxEncoding.setEnabled(false);
		EMSSwingUtils.addItemsComboBox(comboBoxEncoding, 1, ENCODING);
		panel_12.add(comboBoxEncoding);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)), "Config", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-2$
		getContentPane().add(panel_1);
		panel_1.setLayout(null);

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(6, 16, 235, 148);
		panel_2.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblNewLabel = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel.text")); //$NON-NLS-1$
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(0, 11, 104, 20);
		panel_2.add(lblNewLabel);

		comboBoxBaudRate = new JComboBox<String>();
		comboBoxBaudRate.setLocation(117, 11);
		comboBoxBaudRate.setSize(new Dimension(97, 20));
		comboBoxBaudRate.setMaximumSize(new Dimension(100, 20));
		EMSSwingUtils.addItemsComboBox(comboBoxBaudRate, 6, BAUDRATES);
		panel_2.add(comboBoxBaudRate);

		JLabel lblNewLabel_2 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_2.text")); //$NON-NLS-1$
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setBounds(0, 42, 104, 20);
		panel_2.add(lblNewLabel_2);

		comboBoxWordLength = new JComboBox<String>();
		comboBoxWordLength.setBounds(117, 42, 97, 20);
		EMSSwingUtils.addItemsComboBox(comboBoxWordLength, 1, WORDLENGTH);
		panel_2.add(comboBoxWordLength);

		JLabel lblNewLabel_3 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_3.text")); //$NON-NLS-1$
		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_3.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_3.setBounds(0, 73, 104, 20);
		panel_2.add(lblNewLabel_3);

		comboBoxParity = new JComboBox<String>();
		comboBoxParity.setLocation(117, 73);
		comboBoxParity.setSize(new Dimension(97, 20));
		EMSSwingUtils.addItemsComboBox(comboBoxParity, 2, PARITY);
		panel_2.add(comboBoxParity);

		JLabel lblNewLabel_1 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_1.text")); //$NON-NLS-1$
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(0, 104, 104, 20);
		panel_2.add(lblNewLabel_1);

		comboBoxStopBit = new JComboBox<String>();
		comboBoxStopBit.setLocation(117, 104);
		comboBoxStopBit.setSize(new Dimension(97, 20));
		EMSSwingUtils.addItemsComboBox(comboBoxStopBit, 0, STOPBIT);
		panel_2.add(comboBoxStopBit);

		JPanel panel_3 = new JPanel();
		panel_3.setBounds(243, 16, 235, 153);
		panel_3.setEnabled(false);
		panel_3.setBorder(new TitledBorder(
				null,
				BUNDLE.getString("PingInternalFrame.panel_3.borderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		panel_1.add(panel_3);
		panel_3.setLayout(null);

		JLabel lblNewLabel_8 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_8.text")); //$NON-NLS-1$
		lblNewLabel_8.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_8.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_8.setBounds(10, 21, 76, 14);
		panel_3.add(lblNewLabel_8);

		JLabel lblNewLabel_10 = new JLabel(
				BUNDLE.getString("PingInternalFrame.lblNewLabel_10.text")); //$NON-NLS-1$
		lblNewLabel_10.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_10.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_10.setBounds(10, 60, 76, 14);
		panel_3.add(lblNewLabel_10);

		JComboBox<String> comboBoxDTR = new JComboBox<String>();
		comboBoxDTR.setEnabled(false);
		EMSSwingUtils.addItemsComboBox(comboBoxDTR, 0, DTR);
		comboBoxDTR.setBounds(109, 21, 100, 20);
		panel_3.add(comboBoxDTR);

		JComboBox<String> comboBoxRTS = new JComboBox<String>();
		comboBoxRTS.setEnabled(false);
		EMSSwingUtils.addItemsComboBox(comboBoxRTS, 0, RTS);
		comboBoxRTS.setBounds(109, 57, 100, 20);
		panel_3.add(comboBoxRTS);

		JPanel panel_4 = new JPanel();
		panel_4.setBounds(480, 16, 235, 153);
		panel_4.setBorder(new TitledBorder(
				null,
				BUNDLE.getString("PingInternalFrame.panel_4.borderTitle"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
		panel_1.add(panel_4);
		panel_4.setLayout(null);

		JButton btnStartPolling = new JButton(
				BUNDLE.getString("PingInternalFrame.btnStartPolling.text")); //$NON-NLS-1$
		btnStartPolling.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnStartPolling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JInternalFrame frame = getMe();
				ExtendedSerialParameter params = EMSSwingUtils
						.getSerialParameters(frame);
				if(pingWorker != null)
					pingWorker.cancel(true);

				pingWorker = new PingSwingWorker(params,
						getTextAreaPollingResult());
				pingWorker.execute();
			}
		});
		btnStartPolling.setBounds(67, 32, 117, 23);
		panel_4.add(btnStartPolling);

		JButton btnStopPolling = new JButton(
				BUNDLE.getString("PingInternalFrame.btnStopPolling.text")); //$NON-NLS-1$
		btnStopPolling.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnStopPolling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (pingWorker == null) {
					JOptionPane.showMessageDialog(getMe(),
							"No Ping action is initiated", "Ping",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					pingWorker.cancel(true);
					pingWorker = null;
					
					JOptionPane.showMessageDialog(getMe(),
							"Ping operation interruped", "Ping interrupted",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		btnStopPolling.setBounds(67, 74, 117, 23);
		panel_4.add(btnStopPolling);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)), "Result", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))); //$NON-NLS-1$
		getContentPane().add(panel_5);
		panel_5.setLayout(new GridLayout(1, 0, 0, 0));

		textAreaPollingResult = new JTextArea();
		textAreaPollingResult.setSize(new Dimension(400, 300));
		textAreaPollingResult.setEditable(false);
		panel_5.add(textAreaPollingResult);

		JScrollPane scrollPane = new JScrollPane(textAreaPollingResult);
		panel_5.add(scrollPane);
	}

	public JInternalFrame getMe() {
		return this;
	}

	public int getTxtFieldAddress() {
		int addressRef = Integer.parseInt(txtFieldAddress.getText()) - 1;
		logger.info("Reference Address " + addressRef);
		return addressRef;
	}

	public int getTxtFieldDeviceId() {
		int unitId = Integer.parseInt(txtFieldDeviceId.getText());
		return unitId;
	}

	public int getSpinnerLength() {
		int regCount = Integer.parseInt(spinnerLength.getValue().toString());
		return regCount;
	}

	public int getComboBoxPointType() {
		// return function code, its is given in order - 03(Holding Register)
		// with array index 2
		int pointType = comboBoxPointType.getSelectedIndex() + 1;
		return pointType;
	}

	public int getComboBoxBaudRate() {
		int baudRate = Integer.parseInt(comboBoxBaudRate.getSelectedItem()
				.toString());
		return baudRate;
	}

	public String getComboBoxPorts() {
		String serialPort = "";
		if (comboBoxPorts.getSelectedIndex() != -1) {
			serialPort = comboBoxPorts.getSelectedItem().toString();
		}			
		return serialPort;
	}

	public int getComboBoxWordLength() {
		int dataBits = Integer.parseInt(comboBoxWordLength.getSelectedItem()
				.toString());
		return dataBits;
	}

	public String getComboBoxParity() {
		return comboBoxParity.getSelectedItem().toString();
	}

	public int getComboBoxStopBit() {
		int stopBits = Integer.parseInt(comboBoxStopBit.getSelectedItem()
				.toString());
		return stopBits;
	}

	public JTextArea getTextAreaPollingResult() {
		return textAreaPollingResult;
	}

	public String getComboBoxEncoding() {
		return comboBoxEncoding.getSelectedItem().toString();
	}

	public int getTxtFieldPollDelay() {
		int pollDelay = Integer.parseInt(txtFieldPollDelay.getText());
		return pollDelay;
	}

	public int getComboBoxTimeout() {
		int timeout = Integer.parseInt(comboBoxTimeout.getSelectedItem()
				.toString());
		return timeout;
	}

	@Override
	protected void finalize() throws Throwable {

		if (pingWorker != null)
			pingWorker.cancel(true);

		super.finalize();
	}

	@Override
	public void releaseResource() {
		if (pingWorker != null)
			pingWorker.cancel(true);
	}

}
