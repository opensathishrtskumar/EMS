package com.ems.UI.internalframes.renderer;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

public class TextAreaRenderer extends JScrollPane implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	JTextArea textarea;

	public TextAreaRenderer() {
		textarea = new JTextArea(20, 20);
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		getViewport().add(textarea);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
			textarea.setForeground(table.getSelectionForeground());
			textarea.setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
			textarea.setForeground(table.getForeground());
			textarea.setBackground(table.getBackground());
		}

		textarea.setText((String) value);
		textarea.setCaretPosition(0);
		return this;
	}
}