package com.ems.UI.internalframes.renderer;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class JComboRenderer extends JScrollPane implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(JComboRenderer.class);
	private JComboBox<Object> list;

	public JComboRenderer() {
		list = new JComboBox<Object>();
		getViewport().add(list);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
			list.setForeground(table.getSelectionForeground());
			list.setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
			list.setForeground(table.getForeground());
			list.setBackground(table.getBackground());
		}

		list.setModel((MyComboModel) value);

		return list;
	}
}


