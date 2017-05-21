package com.ems.UI.custom.components;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.internalframes.renderer.PasswordRenderer;

public class CustomJTable extends JTable{
	private static final long serialVersionUID = 6887997290246601807L;
	private static final Logger logger = LoggerFactory
			.getLogger(CustomJTable.class);
	
	private PasswordRenderer passwordRenderer = new PasswordRenderer();
	private CustomPasswordEditor editor = new CustomPasswordEditor();

	@Override
	public TableCellEditor getCellEditor(int arg0, int arg1) {

		if(arg0 == 3 && arg1 == 1){
			logger.trace("available password 1 : {}",getValueAt(3, 1));
			return editor;
		}

		return super.getCellEditor(arg0, arg1);
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int arg0, int arg1) {

		//display password filed
		if(arg0 == 3 && arg1 == 1){
			logger.trace("available password 2 : {}",getValueAt(3, 1));
			return passwordRenderer;
		}

		return super.getCellRenderer(arg0, arg1);
	}
}

class CustomPasswordEditor extends DefaultCellEditor{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(CustomPasswordEditor.class);
	private JPasswordField pwf = new JPasswordField();
	public CustomPasswordEditor() {
		super(new JCheckBox());
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		PasswordRenderer renderer = (PasswordRenderer)table.getCellRenderer(row, column);
		logger.trace("Password entered : {}", renderer.getRenderedPassword());
		pwf.setText((String)value);
		return pwf;
	}

	public Object getCellEditorValue() {
		char[] password = pwf.getPassword();
		logger.trace("Password cell editor value : {}", new String(password));
		StringBuilder builder = new StringBuilder();
		builder.append(password);
		return builder.toString();
	}
}
