package com.ems.admin;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ems.security.Security;

public class Admin {

	private JFrame frmAdmin;
	private JTextField txtConfigLocation;
	private JFileChooser fileChooser;
	private JPasswordField password;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Admin window = new Admin();
					window.frmAdmin.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Admin() {
		initialize();
	}

	private String getTempFile(){
		String tmpDir = System.getProperty("java.io.tmpdir");
		File file = new File(tmpDir);
		return file.getParent();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAdmin = new JFrame();
		frmAdmin.setTitle("Admin");
		frmAdmin.setBounds(100, 100, 607, 437);
		frmAdmin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAdmin.getContentPane().setLayout(null);

		password = new JPasswordField();

		JLabel lblConfigFile = new JLabel("Config file");
		lblConfigFile.setBounds(22, 11, 76, 14);
		frmAdmin.getContentPane().add(lblConfigFile);

		txtConfigLocation = new JTextField();
		txtConfigLocation.setEditable(false);
		txtConfigLocation.setBounds(95, 8, 217, 20);
		frmAdmin.getContentPane().add(txtConfigLocation);
		txtConfigLocation.setColumns(10);

		fileChooser = new JFileChooser(getTempFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Config File only", new String[]{"db"});
		fileChooser.addChoosableFileFilter(filter);
		JButton selectConfig = new JButton("...");
		selectConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int option = fileChooser.showOpenDialog(getMe());

				if(option == JFileChooser.APPROVE_OPTION){
					txtConfigLocation.setText(fileChooser.getSelectedFile().getAbsolutePath());
				} else {
					txtConfigLocation.setText("");
				}
			}
		});
		selectConfig.setBounds(323, 7, 35, 23);
		frmAdmin.getContentPane().add(selectConfig);

		final JTextArea textArea = new JTextArea();
		textArea.setBounds(95, 51, 217, 154);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBounds(95, 51, 410, 243);
		frmAdmin.getContentPane().add(scrollPane);

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				password.setText("");

				if(!checkConfigLocation()){
					JOptionPane.showMessageDialog(getMe(), "Select Config File", "Select", JOptionPane.OK_OPTION);
					return;
				}

				int option = JOptionPane.showConfirmDialog(getMe(), password, "Password", JOptionPane.OK_CANCEL_OPTION);
				char[] pwd = password.getPassword();
				password.setText("");
				if(option == JOptionPane.OK_OPTION){
					boolean loginStatus = verifyPassword(pwd);
					if(loginStatus){
						byte[] content = loadFile(getFilePath());
						textArea.setText(new String(content));
					}
				}
			}
		});

		btnOpen.setBounds(97, 344, 65, 23);
		frmAdmin.getContentPane().add(btnOpen);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				password.setText("");

				if(!checkConfigLocation()){
					JOptionPane.showMessageDialog(getMe(), "Select Config File", "Select", JOptionPane.OK_OPTION);
					return;
				}

				if(textArea.getText() == null || textArea.getText().trim().isEmpty()){
					JOptionPane.showMessageDialog(getMe(), "Empty Content!!!", "Empty!", JOptionPane.OK_OPTION);
					return;
				}
				
				int option = JOptionPane.showConfirmDialog(getMe(), password, "Password", JOptionPane.OK_CANCEL_OPTION);
				char[] pwd = password.getPassword();
				password.setText("");
				if(option == JOptionPane.OK_OPTION){
					boolean loginStatus = verifyPassword(pwd);
					if(loginStatus){
						String content = textArea.getText();
						writeContent(content, getFilePath());
						textArea.setText("");
						txtConfigLocation.setText("");
					}
				}
			}
		});
		btnSave.setBounds(172, 344, 65, 23);
		frmAdmin.getContentPane().add(btnSave);

		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
				txtConfigLocation.setText("");
				password.setText("");
			}
		});
		btnClear.setBounds(247, 344, 65, 23);
		frmAdmin.getContentPane().add(btnClear);
	}

	private void writeContent(String content, String filePath){
		Security security = Security.getInstance();
		
		ByteArrayInputStream inStream  = new ByteArrayInputStream(content.getBytes());
		try {
			security.encrypt(inStream, new FileOutputStream(filePath), 4);
			/*InputStream encrypted = security.getEncryptedStream(inStream);
			security.doCopy(encrypted, new FileOutputStream(filePath), 4);*/
			System.out.println("File copied...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private byte[] loadFile(String configFile){
		byte[] content = new byte[]{};

		File file = null;

		if (configFile != null && (file = new File(configFile)) != null
				&& file.exists()) {
			try {
				Security security = Security.getInstance();
				InputStream originalStream = security.getDecryptedStream(new FileInputStream(file));
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				security.doCopy(originalStream, outStream, 4);	
				content = outStream.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return content;
	}

	private JFrame getMe(){
		return frmAdmin;
	}

	private byte[] getPassword(){
		return new byte[]{68, 120, 66, 64, 50, 48, 49, 55, 48, 48, 49};
	}

	private boolean verifyPassword(char[] password){
		boolean status = false;

		try{
			status = new String(password).equals(new String(getPassword()));
		} catch(Exception e){
			status = false;
		}

		return status;
	}

	private String getFilePath(){
		return txtConfigLocation.getText();
	}

	private boolean checkConfigLocation(){
		boolean status = false;
		if (txtConfigLocation.getText() != null
				&& !txtConfigLocation.getText().trim().isEmpty()) {
			status = true;
		}
		return status;
	}
}
