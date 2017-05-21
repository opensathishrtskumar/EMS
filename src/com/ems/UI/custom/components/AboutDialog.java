package com.ems.UI.custom.components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	String htmlString = "<html><head><style>body{background-color:#f2f2f2;}h1{color:black;text-align:center;}p{font-family:verdana;font-size: 13px;}</style></head><body><h1>Energy Management System</h1><p>version: v1.0.0</p><p>(c) Copyright <b><i><a href=\"#\">Saratha engineering.</a></i></b>  All rights reserved.</p></body></html>";

	/**
	 * Create the dialog.
	 */
	public AboutDialog(Frame parent) {
		setTitle("About");
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				AboutDialog.class.getResource("/com/ems/resources/about.png")));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(1, 0, 0, 0));

		JLabel lblNewLabel = new JLabel(htmlString);
		contentPanel.add(lblNewLabel);
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
	}
}
