package com.ems.UI.internalframes;

import static com.ems.constants.MessageConstants.DBCONFIG_KEY;
import static com.ems.util.EMSSwingUtils.centerFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.custom.components.CustomJTable;
import com.ems.db.DBConnectionManager;
import com.ems.tmp.datamngr.TempDataManager;

public class DBConfigurer extends JInternalFrame {
	private static final long serialVersionUID = 3575627077849311701L;
	private static final Logger logger = LoggerFactory
			.getLogger(DBConfigurer.class);
	private JTable table;

	/**
	 * Create the frame.
	 */
	public DBConfigurer() {
		setIconifiable(true);
		setFrameIcon(new ImageIcon(DBConfigurer.class.getResource("/com/ems/resources/application-x-sqlite2.png")));

		setTitle("Configure Database");
		setClosable(true);
		setBounds(100, 100, 450, 225);
		centerFrame(getMe());
		getContentPane().setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 434, 190);
		getContentPane().add(panel);
		panel.setLayout(null);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(0, 0, 434, 147);
		panel.add(panel_1);

		table = new CustomJTable();
		table.setFont(new Font("Tahoma", Font.PLAIN, 12));
		table.setFillsViewportHeight(true);
		table.setBounds(56, 11, 273, 127);
		table.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"Property", "Values"
				}
				) {
			Class[] columnTypes = new Class[] {
					String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
					false, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(161);
		panel_1.setLayout(null);
		table.setPreferredSize(new Dimension(14, 39));
		table.setAutoCreateRowSorter(false);
		table.setRowHeight(25);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(0, 0, 434, 147);
		panel_1.add(scrollPane);
		scrollPane
		.setViewportBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		scrollPane.setMinimumSize(new Dimension(66, 52));

		JButton btnTestConnection = new JButton("Test Connection");
		btnTestConnection.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnTestConnection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JTable jTable = getTable();
				DefaultTableModel model = (DefaultTableModel)jTable.getModel();
				model.fireTableDataChanged();
				Properties props = new Properties();
				props.put(DBCONFIG_KEY[0], model.getValueAt(0, 1));
				props.put(DBCONFIG_KEY[1], model.getValueAt(1, 1));
				props.put(DBCONFIG_KEY[2], model.getValueAt(2, 1));

				String password = (String) jTable.getCellEditor(3, 1).getCellEditorValue();
				password = (String) (password == null
						|| password.trim().isEmpty() ? jTable.getValueAt(3, 1)
								: password);
				logger.info("Password : {}", password);
				props.put(DBCONFIG_KEY[3], password);

				boolean status = DBConnectionManager.verifyConnection(props);

				if(status){
					JOptionPane.showMessageDialog(getMe(),
							"Connection success", "Success",
							JOptionPane.INFORMATION_MESSAGE);
					TempDataManager.writeDBConfig(props);
				}else{
					JOptionPane.showMessageDialog(getMe(),
							"Invalid connection details", "Failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnTestConnection.setBounds(157, 158, 123, 23);
		panel.add(btnTestConnection);

		addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameOpened(InternalFrameEvent arg0) {
				// Assume when properites is empty, No DB configured
				Properties properties = TempDataManager.retrieveDBConfig();

				populateDBProps(properties, table);
			}
		});
	}

	public void populateDBProps(Properties props, JTable table) {
		DefaultTableModel model = (DefaultTableModel) table.getModel();

		if (props != null) {
			model.insertRow(0, new Object[] { DBCONFIG_KEY[0],
					props.getProperty(DBCONFIG_KEY[0], "") });
			model.insertRow(1,new Object[] { DBCONFIG_KEY[1],
					props.getProperty(DBCONFIG_KEY[1], "") });
			model.insertRow(2,new Object[] { DBCONFIG_KEY[2],
					props.getProperty(DBCONFIG_KEY[2], "") });

			logger.info("Loaded password : {}", props.getProperty(DBCONFIG_KEY[3], ""));
			model.insertRow(3,new Object[] { DBCONFIG_KEY[3],
					props.getProperty(DBCONFIG_KEY[3], "") });
			table.setValueAt(props.getProperty(DBCONFIG_KEY[3], ""), 3, 1);
			System.out.println(table.getCellEditor(0, 0));
			model.fireTableDataChanged();
		}
	}

	public JInternalFrame getMe() {
		return this;
	}

	public JTable getTable() {
		return table;
	}
}
