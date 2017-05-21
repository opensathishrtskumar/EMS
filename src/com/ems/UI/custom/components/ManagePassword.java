package com.ems.UI.custom.components;

import static com.ems.constants.MessageConstants.PASSWORD_KEY;
import static com.ems.tmp.datamngr.TempDataManager.MAIN_CONFIG;
import static com.ems.tmp.datamngr.TempDataManager.retrieveTempConfig;
import static com.ems.tmp.datamngr.TempDataManager.writeTempConfig;
import static com.ems.util.Helper.checkNullEmpty;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

public class ManagePassword extends JDialog {

	private static final long serialVersionUID = -6610614993754269592L;
	private final JPanel contentPanel = new JPanel();
	private JPasswordField passwordFieldOld;
	private JPasswordField passwordFieldNew;
	private JPasswordField passwordFieldConfirm;

	/**
	 * Create the dialog.
	 */
	public ManagePassword(Frame parent) {
		super(parent, "Login", true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ManagePassword.class.getResource("/com/ems/resources/change_password.png")));

		setBounds(100, 100, 340, 193);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new CompoundBorder(null, new EtchedBorder(EtchedBorder.LOWERED, null, null)));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		contentPanel.setLayout(null);

		JLabel lblNewLabel = new JLabel("Old Password");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel.setBounds(31, 23, 116, 14);
		contentPanel.add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("New Password");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(31, 58, 116, 14);
		contentPanel.add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Confirm Password");
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_2.setBounds(31, 89, 116, 14);
		contentPanel.add(lblNewLabel_2);

		passwordFieldOld = new JPasswordField();
		passwordFieldOld.setBounds(172, 21, 103, 20);
		contentPanel.add(passwordFieldOld);

		passwordFieldNew = new JPasswordField();
		passwordFieldNew.setBounds(172, 56, 103, 20);
		contentPanel.add(passwordFieldNew);

		passwordFieldConfirm = new JPasswordField();
		passwordFieldConfirm.setBounds(172, 87, 103, 20);
		contentPanel.add(passwordFieldConfirm);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if(checkNullEmpty(passwordFieldOld.getText()) ||
						checkNullEmpty(passwordFieldNew.getText()) ||
						checkNullEmpty(passwordFieldConfirm.getText())){
					JOptionPane.showMessageDialog(getMe(),
							"Please fill all fields!", "Change password",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				if(!passwordFieldNew.getText().equals(passwordFieldConfirm.getText())){
					JOptionPane.showMessageDialog(getMe(),
							"New and Confirm password are not same!", "Change password",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Properties mainConfig = retrieveTempConfig(MAIN_CONFIG);
				String confPassword = mainConfig.getProperty(PASSWORD_KEY, "INVALID");
				
				if(confPassword.equals(passwordFieldOld.getText())){
					mainConfig.put(PASSWORD_KEY, passwordFieldConfirm.getText());
					writeTempConfig(mainConfig, MAIN_CONFIG);
					JOptionPane.showMessageDialog(getMe(),
							"Password changed successfully", "Change password",
							JOptionPane.INFORMATION_MESSAGE);
					passwordFieldOld.setText("");
					passwordFieldNew.setText("");
					passwordFieldConfirm.setText("");
					
				} else {
					JOptionPane.showMessageDialog(getMe(),
							"Invalid old password!", "Change password",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		//pack();
        setResizable(false);
		setLocationRelativeTo(parent);
	}	
	
	public JDialog getMe(){
		return this;
	}
}
