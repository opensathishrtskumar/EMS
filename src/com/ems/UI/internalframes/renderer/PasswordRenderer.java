package com.ems.UI.internalframes.renderer;

import java.awt.Component;

import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordRenderer implements TableCellRenderer {
	private static final Logger logger = LoggerFactory
			.getLogger(PasswordRenderer.class);
	private JPasswordField password = new JPasswordField();

	public PasswordRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			password.setForeground(table.getSelectionForeground());
			password.setBackground(table.getSelectionBackground());
		} else {
			password.setForeground(table.getForeground());
			password.setBackground(table.getBackground());
		}

		logger.trace("Password renderer : {}", (String)value);

		password.setText((String) value);
		return password;
	}

	public String getRenderedPassword(){
		StringBuilder builder = new StringBuilder();
		builder.append(password.getPassword());
		return builder.toString();
	}
}
