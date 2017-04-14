package com.ems.UI;

import static com.ems.constants.MessageConstants.NOT_IMPLEMNENTED;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.util.ResourceBundle;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.internalframes.PingInternalFrame;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.ems.UI.messages"); //$NON-NLS-1$

	private JFrame frmManageEnergy;
	private JDesktopPane desktopPane;
	private JMenuItem mntmOpen;
	private JMenuItem mntmAbout;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmManageEnergy.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmManageEnergy = new JFrame();
		frmManageEnergy.setAlwaysOnTop(false);
		frmManageEnergy.setResizable(true);
		frmManageEnergy.setTitle(BUNDLE.getString("Main.frmManageEnergy.title")); //$NON-NLS-1$
		frmManageEnergy.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frmManageEnergy.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frmManageEnergy.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu(BUNDLE.getString("Main.mnFile.text")); //$NON-NLS-1$
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);

		mntmOpen = new JMenuItem(BUNDLE.getString("Main.mntmOpen.text")); //$NON-NLS-1$
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frmManageEnergy, NOT_IMPLEMNENTED);
			}
		});
		mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem(BUNDLE.getString("Main.mntmSave.text")); //$NON-NLS-1$
		mntmSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frmManageEnergy, NOT_IMPLEMNENTED);
			}
		});
		mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
		mnFile.add(mntmSave);

		JMenuItem mntmExit = new JMenuItem(BUNDLE.getString("Main.mntmExit.text")); //$NON-NLS-1$
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int option = JOptionPane.showConfirmDialog(frmManageEnergy, BUNDLE.getString("CloseWindow.Titile.Msg") , 
						BUNDLE.getString("CloseWindow.Titile"), JOptionPane.WARNING_MESSAGE);

				if(option == JOptionPane.OK_OPTION){
					logger.info("EMS is closing");
					System.exit(0);
				}
			}
		});
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_MASK));
		mnFile.add(mntmExit);

		JMenu mnConnection = new JMenu(BUNDLE.getString("Main.mnConnection.text")); //$NON-NLS-1$
		mnConnection.setMnemonic('n');
		menuBar.add(mnConnection);

		JMenuItem mntmConnect = new JMenuItem(BUNDLE.getString("Main.mntmConnect.text")); //$NON-NLS-1$
		mntmConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frmManageEnergy, NOT_IMPLEMNENTED);
			}
		});
		mntmConnect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
		mnConnection.add(mntmConnect);

		JMenuItem mntmDisconnect = new JMenuItem(BUNDLE.getString("Main.mntmNewMenuItem.text")); //$NON-NLS-1$
		mntmDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frmManageEnergy, NOT_IMPLEMNENTED);
			}
		});
		mntmDisconnect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK));
		mnConnection.add(mntmDisconnect);

		JMenuItem mntmPing = new JMenuItem(BUNDLE.getString("Main.mntmNewMenuItem_1.text")); //$NON-NLS-1$
		mntmPing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JInternalFrame[] avlFrames = desktopPane.getAllFrames();

				boolean exit = false;

				if(avlFrames != null && avlFrames.length != 0){

					for(JInternalFrame iFrame : avlFrames){
						if(iFrame instanceof PingInternalFrame){
							exit = true;
							try {
								iFrame.setVisible(true);
								iFrame.setSelected(true);
							} catch (PropertyVetoException e) {
								e.printStackTrace();
							}
						}
					}
				}

				if(!exit){
					PingInternalFrame pingFrame = new PingInternalFrame();
					desktopPane.add(pingFrame);
					pingFrame.setVisible(true);
					try {
						pingFrame.setSelected(true);
					} catch (PropertyVetoException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mntmPing.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK));
		mnConnection.add(mntmPing);

		JMenu mnSetup = new JMenu(BUNDLE.getString("Main.mnSetup.text")); //$NON-NLS-1$
		mnSetup.setMnemonic('S');
		menuBar.add(mnSetup);

		JMenuItem mntmDevices = new JMenuItem(BUNDLE.getString("Main.mntmDevices.text")); //$NON-NLS-1$
		mntmDevices.setArmed(true);
		mntmDevices.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		mnSetup.add(mntmDevices);

		JMenu mnReports = new JMenu(BUNDLE.getString("Main.mnReports.text")); //$NON-NLS-1$
		mnReports.setMnemonic('R');
		menuBar.add(mnReports);

		JMenuItem mntmView = new JMenuItem(BUNDLE.getString("Main.mntmView.text")); //$NON-NLS-1$
		mntmView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frmManageEnergy, NOT_IMPLEMNENTED);
			}
		});
		mntmView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_MASK));
		mnReports.add(mntmView);

		JMenu mnHelp = new JMenu(BUNDLE.getString("Main.mnHelp.text")); //$NON-NLS-1$
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem(BUNDLE.getString("Main.mntmAbout.text")); //$NON-NLS-1$
		mntmAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_MASK));
		mnHelp.add(mntmAbout);
		frmManageEnergy.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		desktopPane = new JDesktopPane();
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		desktopPane.setBackground(SystemColor.window);
		frmManageEnergy.getContentPane().add(desktopPane);
	}
}
