package com.ems.UI.internalframes.renderer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class JTableRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 8105908466727464290L;
	Color odd = new Color(223, 228, 230);

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		final Component c = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		c.setBackground(row % 2 == 0 ? odd : Color.WHITE);
		return c;
	}

}
