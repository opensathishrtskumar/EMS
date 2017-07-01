package com.ems.UI.internalframes;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.panels.GroupDevicesPanel;
import com.ems.panels.ManageGroup;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class GroupDevices extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(GroupDevices.class);
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GroupDevices frame = new GroupDevices(null);
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
	public GroupDevices(Frame parent) {
		super(parent, "Group Devices", true);
		
		//Dont need close button
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Confirm to close?",
						"Exit", JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION){
					logger.info("Closing Grouped devices frame....");
					dispose();
				}
			}
		});
		
		setModal(true);
		setTitle("Group Devices");
		setBounds(100, 100, 883, 478);
		getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane);
		
		ManageGroup groupPanel = new ManageGroup();
		splitPane.setLeftComponent(groupPanel);
		
		GroupDevicesPanel groupingPanel = new GroupDevicesPanel();
		splitPane.setRightComponent(new JScrollPane(groupingPanel));
	}
	
	
	public final JDialog getMe(){
    	return this;
    }
}
