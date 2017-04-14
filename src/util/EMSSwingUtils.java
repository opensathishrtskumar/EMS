package util;

import static com.ems.constants.EmsConstants.RETRYCOUNT;
import static com.ems.constants.EmsConstants.TIMEOUT;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;

import com.ems.UI.dto.ExtendedSerialParameter;
import com.ems.UI.internalframes.PingInternalFrame;

public abstract class EMSSwingUtils {

	/**
	 * @param combo
	 * @param arg
	 * 
	 * Adds item to Combobox
	 */
	public static void addItemsComboBox(JComboBox<String> combo, int selectedIndex ,String...arg){
		if(arg != null && combo != null){
			for(String port : arg){
				combo.addItem(port);
			}
			if(arg.length == 0) selectedIndex = -1;

			combo.setSelectedIndex(selectedIndex);
		}
	}

	/**
	 * @param combo
	 * @param arg
	 * 
	 * Adds item to Combobox
	 */
	public static void addItemsComboBox(JComboBox<String> combo, int selectedIndex, int...arg){
		if(arg != null & combo != null){
			for(int item : arg){
				combo.addItem(String.valueOf(item));
			}
			combo.setSelectedIndex(selectedIndex);
		}
	}


	/**
	 * Create SerialParameters from PingInternalFrame
	 */
	public static ExtendedSerialParameter getSerialParameters(JInternalFrame iFrame){
		ExtendedSerialParameter params = new ExtendedSerialParameter();

		if(iFrame != null && iFrame instanceof PingInternalFrame){
			PingInternalFrame pingFrame = (PingInternalFrame)iFrame;
			//Connection config
			params.setPortName(pingFrame.getComboBoxPorts());
			params.setBaudRate(pingFrame.getComboBoxBaudRate());
			params.setFlowControlIn(0);//TODO : Config later
			params.setFlowControlOut(0);//TODO : Config later
			params.setDatabits(pingFrame.getComboBoxWordLength());
			params.setStopbits(pingFrame.getComboBoxStopBit());
			params.setParity(pingFrame.getComboBoxParity());
			params.setEcho(false);
			params.setEncoding(pingFrame.getComboBoxEncoding());

			//Unit and its config
			params.setRetries(RETRYCOUNT);
			params.setUnitId(pingFrame.getTxtFieldDeviceId());
			params.setReference(pingFrame.getTxtFieldAddress());//Address is mapped
			params.setCount(pingFrame.getSpinnerLength());//Total number of registers to be read
			params.setPointType(3);
			params.setTimeout(TIMEOUT[1]);
			params.setPollDelay(pingFrame.getTxtFieldPollDelay());
		}

		return params;
	}

	public static KeyAdapter getNumericListener(){
		return new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char vChar = e.getKeyChar();
				if (!(Character.isDigit(vChar)
						|| (vChar == KeyEvent.VK_BACK_SPACE)
						|| (vChar == KeyEvent.VK_DELETE))) {
					e.consume();
				}
			}
		};
	}
}
