package com.ems.UI.custom.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.response.handlers.Event;
import com.ems.response.handlers.EventHandler;
import com.ems.response.handlers.Events;
import com.ems.tmp.datamngr.TempDataManager;

import static com.ems.util.Helper.*;
import static com.ems.tmp.datamngr.TempDataManager.*;
import static com.ems.constants.MessageConstants.*;

public class LoginDialog extends JDialog {
	private static final Logger logger = LoggerFactory.getLogger(LoginDialog.class);
	private static final long serialVersionUID = 6764947954687120430L;
	private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JLabel lbUsername;
    private JLabel lbPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private boolean succeeded;
    private EventHandler handler;
    
    public EventHandler getHandler() {
		return handler;
	}

	public void setHandler(EventHandler handler) {
		this.handler = handler;
	}

	public final JDialog getMe(){
    	return this;
    }
    
    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        //Dont need close button
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
        	@Override
        	public void windowClosing(WindowEvent arg0) {
				int option = JOptionPane.showConfirmDialog(getMe(), "Close all?",
						"Exit", JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION){
					logger.info("Login closing system....");
					TempDataManager.deleteFrameLock();
					System.exit(0);
				}
        	}
		});
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();
 
        cs.fill = GridBagConstraints.HORIZONTAL;
 
        lbUsername = new JLabel("User name ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);
 
        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);
 
        lbPassword = new JLabel("Password ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);
 
        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
        panel.setBorder(new LineBorder(Color.GRAY));
 
        btnLogin = new JButton("Login");
 
        btnLogin.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
                if (authenticate(getUsername(), getPassword())) {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Hi " + getUsername() + "! You have successfully logged in.",
                            "Login",
                            JOptionPane.INFORMATION_MESSAGE);
                    succeeded = true;
                    dispose();
                    
                    if(getHandler() != null)
                    	getHandler().handle(new Event() {
							@Override
							public Events getEvent() {
								return Events.LOGIN_SUCCESS;
							}
						});
                    
                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Invalid username or password",
                            "Login",
                            JOptionPane.ERROR_MESSAGE);
                    // reset username and password
                    tfUsername.setText("");
                    pfPassword.setText("");
                    succeeded = false;
                    
                    if(getHandler() != null)
                    	getHandler().handle(new Event() {
							@Override
							public Events getEvent() {
								return Events.LOGIN_FAILURE;
							}
						});
                }
            }
        });
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e) {
            	if(getHandler() != null)
                	getHandler().handle(new Event() {
						@Override
						public Events getEvent() {
							return Events.LOGIN_CANCEL;
						}
					});
            	
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(btnLogin);
        //bp.add(btnCancel);
 
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);
 
        tfUsername.setText("");
        pfPassword.setText("");
        
        pack();
        initialize();
        setResizable(false);
        setLocationRelativeTo(parent);
    }
    
    private void initialize(){
    	try {
			if(!isTempFileAvailable(MAIN_CONFIG)){
				Properties initialConfig = getInitialMainConfig();
				writeTempConfig(initialConfig, MAIN_CONFIG);
			}
		} catch (Exception e) {
			logger.error("login frame error : {}",e.getLocalizedMessage());
			logger.error("{}",e);
		}
    }
    
    public String getUsername() {
        return tfUsername.getText().trim();
    }
 
    public String getPassword() {
        return new String(pfPassword.getPassword());
    }
 
    public boolean isSucceeded() {
        return succeeded;
    }
    
    private static boolean authenticate(String username, String password){
    	Properties properties = retrieveTempConfig(MAIN_CONFIG);
    	String uName = properties.getProperty(USERNAME_KEY,"invalid");
    	String pwd = properties.getProperty(PASSWORD_KEY,"invalid");
    	
    	if(checkNullEmpty(username) || checkNullEmpty(password)){
    		logger.info("Null or empty credentials");
    		return false;
    	}
    	
    	if(username.equals(uName) && password.equals(pwd)){
    		logger.info("Login successful");
    		return true;
    	} else {
    		logger.info("Invalid credentials credentials");
    		return false;
    	}
    }
}