package com.ems.UI;

import static com.ems.util.ConfigHelper.getCompanyName;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.custom.components.AboutDialog;
import com.ems.UI.custom.components.LoginDialog;
import com.ems.UI.custom.components.ManagePassword;
import com.ems.UI.custom.components.PreferencesDialog;
import com.ems.UI.internalframes.AbstractIFrame;
import com.ems.UI.internalframes.DBConfigurer;
import com.ems.UI.internalframes.DashboardFrame;
import com.ems.UI.internalframes.GroupDevices;
import com.ems.UI.internalframes.GroupedDevices;
import com.ems.UI.internalframes.ManageDeviceIFrame;
import com.ems.UI.internalframes.PingInternalFrame;
import com.ems.UI.internalframes.PollingIFrame;
import com.ems.UI.internalframes.ReportsIFrame;
import com.ems.response.handlers.Event;
import com.ems.response.handlers.EventHandler;
import com.ems.response.handlers.Events;
import com.ems.scheduler.CumulativeReportJob;
import com.ems.scheduler.DailyReportJob;
import com.ems.scheduler.SchedulerConfigurer;
import com.ems.scheduler.SchedulerUtil;
import com.ems.tmp.datamngr.TempDataManager;
import com.ems.util.ConfigHelper;
import com.ems.util.EMSSwingUtils;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("com.ems.UI.messages"); //$NON-NLS-1$

	private JFrame frmManageEnergy;
	private JDesktopPane desktopPane;
	private JMenuItem mntmAbout;
	private JMenuBar menuBar;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if(!TempDataManager.frameLockExist()){
						Main window = new Main();
						window.frmManageEnergy.setVisible(true);
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
				} catch (Exception e) {
					logger.error("{}",e);
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
	
	public JFrame getMe(){
		return frmManageEnergy;
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmManageEnergy = new JFrame();
		frmManageEnergy.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/com/ems/resources/gnome-monitor-big.png")));
		frmManageEnergy.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(),
						"Really Close?", "Exit",
						JOptionPane.YES_NO_OPTION);  
		        if (option == JOptionPane.YES_OPTION){
		        	
		        	//TempDataManager.deleteFrameLock();
		        	
		        	//Close all existing IFrames, that will close its workers
		        	for(JInternalFrame frame : desktopPane.getAllFrames()){
		        		if(frame instanceof AbstractIFrame){
		        			((AbstractIFrame)frame).releaseResource();
		        		}
		        		frame.dispose();
		        	}
		        	logger.info("Application is closed...");
		        	frmManageEnergy.dispose();
		        	System.exit(0);
		        }
				
			}
			@Override
			public void windowActivated(WindowEvent arg0) {
				//TempDataManager.createFrameLock();
			}
			
			@Override
			public void windowOpened(WindowEvent arg0) {
				logger.info("Application is opened...");
				LoginDialog dialog = new LoginDialog(getMe());
				//Dashboard not required
				dialog.setHandler(new EventHandler() {
					
					@Override
					public void handle(Event event) {
						if(event.getEvent() == Events.LOGIN_SUCCESS){
							
							JobDetail job = SchedulerUtil.createJob("DailyReportJob", "EMS", DailyReportJob.class);
							Trigger trigger = SchedulerUtil.createTrigger("DailyReport", "EMS", ConfigHelper.getDailyReportCronExpr());
							SchedulerConfigurer.scheduleJob(trigger, job);
							
							JobDetail reportJob = SchedulerUtil.createJob("DailyCumulativeReportJob", "EMS", CumulativeReportJob.class);
							Trigger reportTrigger = SchedulerUtil.createTrigger("DailyCumulativeReport", "EMS", ConfigHelper.getcumulativeReportCronExpr());
							SchedulerConfigurer.scheduleJob(reportTrigger, reportJob);
						}
					}
				});
				dialog.setModal(true);
				dialog.setVisible(true);
			}
		});
		frmManageEnergy.setAlwaysOnTop(true);
		frmManageEnergy.setResizable(true);
		frmManageEnergy
				.setTitle(BUNDLE.getString("Main.frmManageEnergy.title") + getCompanyName()); //$NON-NLS-1$
		/*frmManageEnergy.setExtendedState(JFrame.MAXIMIZED_BOTH);*/
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frmManageEnergy.setSize(dimension);
		
		frmManageEnergy.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		menuBar = new JMenuBar();
		frmManageEnergy.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu(BUNDLE.getString("Main.mnFile.text")); //$NON-NLS-1$
		mnFile.setMnemonic('F');
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem(
				BUNDLE.getString("Main.mntmExit.text")); //$NON-NLS-1$
		mntmExit.setMnemonic('X');
		mntmExit.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/gnome-logout.png")));
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int option = JOptionPane.showConfirmDialog(frmManageEnergy,
						BUNDLE.getString("CloseWindow.Titile.Msg"),
						BUNDLE.getString("CloseWindow.Titile"),
						JOptionPane.WARNING_MESSAGE);

				if (option == JOptionPane.OK_OPTION) {
					logger.info("EMS is closing");
					TempDataManager.deleteFrameLock();
					System.exit(0);
				}
			}
		});

		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		
		JMenuItem mntmLockScreen = new JMenuItem(BUNDLE.getString("Main.mntmLockScreen.text")); //$NON-NLS-1$
		mntmLockScreen.setMnemonic('L');
		mntmLockScreen.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/gnome-lockscreen.png")));
		mntmLockScreen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		mntmLockScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				LoginDialog dialog = new LoginDialog(getMe());
				dialog.setModal(true);
				dialog.setVisible(true);
			}
		});
		mnFile.add(mntmLockScreen);
		
		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				InputEvent.ALT_MASK));
		mnFile.add(mntmExit);

		JMenu mnConnection = new JMenu(
				BUNDLE.getString("Main.mnConnection.text")); //$NON-NLS-1$
		mnConnection.setMnemonic('n');
		menuBar.add(mnConnection);

		JMenuItem mntmPing = new JMenuItem(
				BUNDLE.getString("Main.mntmNewMenuItem_1.text")); //$NON-NLS-1$
		mntmPing.setMnemonic('I');
		mntmPing.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/filefind.png")));
		mntmPing.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EMSSwingUtils.openSingletonIFrame(desktopPane,
						PingInternalFrame.class);
			}
		});

		JSeparator separator_2 = new JSeparator();
		mnConnection.add(separator_2);
		mntmPing.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
				InputEvent.ALT_MASK));
		mnConnection.add(mntmPing);
		
		JSeparator separator_3 = new JSeparator();
		mnConnection.add(separator_3);
		
		JMenuItem mntmPolling = new JMenuItem(BUNDLE.getString("Main.mntmPolling.text")); //$NON-NLS-1$
		mnConnection.add(mntmPolling);
		mntmPolling.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/gtk-refresh.png")));
		mntmPolling.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EMSSwingUtils.openSingletonIFrame(desktopPane,
						PollingIFrame.class);
			}
		});
		mntmPolling.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		mntmPolling.setMnemonic('P');
		
		JSeparator separator_7 = new JSeparator();
		mnConnection.add(separator_7);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem(BUNDLE.getString("Main.mntmNewMenuItem_1.text_2")); //$NON-NLS-1$
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EMSSwingUtils.openSingletonIFrame(desktopPane,
						DashboardFrame.class);
			}
		});
		mntmNewMenuItem_1.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/agt_home.png")));
		mntmNewMenuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK));
		mntmNewMenuItem_1.setMnemonic('B');
		mnConnection.add(mntmNewMenuItem_1);

		JMenu mnSetup = new JMenu(BUNDLE.getString("Main.mnSetup.text")); //$NON-NLS-1$
		mnSetup.setMnemonic('S');
		menuBar.add(mnSetup);

		JMenuItem mntmDevices = new JMenuItem(
				BUNDLE.getString("Main.mntmDevices.text")); //$NON-NLS-1$
		mntmDevices.setMnemonic('D');
		mntmDevices.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/system_16x16.gif")));
		mntmDevices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EMSSwingUtils.openSingletonIFrame(desktopPane,
						ManageDeviceIFrame.class);
			}
		});
		mntmDevices.setArmed(true);
		mntmDevices.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				InputEvent.CTRL_MASK));
		mnSetup.add(mntmDevices);
		
		JSeparator separator_4 = new JSeparator();
		mnSetup.add(separator_4);
		
		JMenuItem mntmNewMenuItem = new JMenuItem(BUNDLE.getString("Main.mntmNewMenuItem.text_1"));
		mntmNewMenuItem.setMnemonic('O');
		mntmNewMenuItem.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/application-x-sqlite2.png")));
		mntmNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EMSSwingUtils.openSingletonIFrame(desktopPane,
						DBConfigurer.class);
			}
		});
		mnSetup.add(mntmNewMenuItem);
		
		JSeparator separator_5 = new JSeparator();
		mnSetup.add(separator_5);
		
		JMenuItem mntmManagePassword = new JMenuItem(BUNDLE.getString("Main.mntmManagePassword.text")); //$NON-NLS-1$
		mntmManagePassword.setMnemonic('W');
		mntmManagePassword.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
		mntmManagePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ManagePassword password = new ManagePassword(getMe());
				password.setModal(true);
				password.setVisible(true);
			}
		});
		mntmManagePassword.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/change_password.png")));
		mnSetup.add(mntmManagePassword);
		
		JMenuItem mntmPreference = new JMenuItem(BUNDLE.getString("Main.mntmNewMenuItem_1.text_1")); //$NON-NLS-1$
		mntmPreference.setMnemonic('F');
		mntmPreference.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				PreferencesDialog preference = new PreferencesDialog(getMe());
				preference.setModal(true);
				preference.setVisible(true);
			}
		});
		
		JSeparator separator_6 = new JSeparator();
		mnSetup.add(separator_6);
		mntmPreference.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/Service-Manager.png")));
		mntmPreference.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		mnSetup.add(mntmPreference);

		JMenu mnReports = new JMenu(BUNDLE.getString("Main.mnReports.text")); //$NON-NLS-1$
		mnReports.setMnemonic('R');
		menuBar.add(mnReports);

		JMenuItem mntmView = new JMenuItem(
				BUNDLE.getString("Main.mntmView.text")); //$NON-NLS-1$
		mntmView.setMnemonic('V');
		mntmView.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/search.png")));
		mntmView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				EMSSwingUtils.openSingletonIFrame(desktopPane, ReportsIFrame.class);
			}
		});
		mntmView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				InputEvent.ALT_MASK));
		mnReports.add(mntmView);
		
		JMenuItem mntmGroupDevice = new JMenuItem(BUNDLE.getString("Main.mntmGroupDevice.text")); //$NON-NLS-1$
		mntmGroupDevice.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/group.png")));
		mntmGroupDevice.setMnemonic('G');
		mntmGroupDevice.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		mntmGroupDevice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Show grouping feauture
				GroupDevices dialog = new GroupDevices(getMe());
				dialog.setModal(true);
				dialog.setVisible(true);
			}
		});
		mnReports.add(mntmGroupDevice);
		
		JMenuItem mntmGroupedDevices = new JMenuItem(BUNDLE.getString("Main.mntmGroupedDevices.text")); //$NON-NLS-1$
		mntmGroupedDevices.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/group.png")));
		mntmGroupedDevices.setMnemonic('R');
		mntmGroupedDevices.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
		mntmGroupedDevices.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Show grouped devices
				EMSSwingUtils.openSingletonIFrame(desktopPane, GroupedDevices.class);
			}
		});
		mnReports.add(mntmGroupedDevices);

		JMenu mnHelp = new JMenu(BUNDLE.getString("Main.mnHelp.text")); //$NON-NLS-1$
		mnHelp.setMnemonic('H');
		menuBar.add(mnHelp);

		mntmAbout = new JMenuItem(BUNDLE.getString("Main.mntmAbout.text")); //$NON-NLS-1$
		mntmAbout.setMnemonic('A');
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AboutDialog dialog = new AboutDialog(getMe());
				dialog.setVisible(true);
			}
		});
		mntmAbout.setIcon(new ImageIcon(Main.class.getResource("/com/ems/resources/about.png")));
		mntmAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
				InputEvent.ALT_MASK));
		mnHelp.add(mntmAbout);
		frmManageEnergy.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		desktopPane = new JDesktopPane()/*{
			private static final long serialVersionUID = -6668135361372162932L;
			ImageIcon icon = new ImageIcon(Main.class.getResource("/com/ems/resources/images_desk.png"));
            Image image = icon.getImage();
            Image scaledimage = image.getScaledInstance(435, 414, Image.SCALE_SMOOTH);
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
				g.drawImage(scaledimage,
						(getWidth() - scaledimage.getWidth(this)) / 2,
						(getHeight() - scaledimage.getHeight(this)) / 2, this);
            }
		}*/;
		desktopPane.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		desktopPane.setBackground(SystemColor.text);
		frmManageEnergy.getContentPane().add(desktopPane);
		frmManageEnergy.setAlwaysOnTop(false);
		
		EMSSwingUtils.addTrayIcon(getCompanyName());
	}
}
