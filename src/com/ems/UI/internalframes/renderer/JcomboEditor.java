package com.ems.UI.internalframes.renderer;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcomboEditor extends DefaultCellEditor {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(JcomboEditor.class);
	private JComboBox<Object> comboBox;
	private MyComboModel model;

	public JcomboEditor() {
		super(new JCheckBox());
		comboBox = new JComboBox<Object>();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		comboBox.setModel((MyComboModel) value);
		model = (MyComboModel) value;
		return comboBox;
	}

	public Object getCellEditorValue() {
		model.setSelectedIndices(model.getSelectedIndices());
		return model;
	}
}