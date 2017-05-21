package com.ems.UI.internalframes.renderer;

import javax.swing.DefaultComboBoxModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyComboModel extends DefaultComboBoxModel<Object> {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory
			.getLogger(MyComboModel.class);
	private int[] selectedIndices;

	public MyComboModel(Object[] data, Object selectedItem) {
		for (int i = 0; i < data.length; i++) {
			addElement(data[i]);
		}
		super.setSelectedItem(selectedItem);
	}

	public MyComboModel(Object[] data) {
		for (int i = 0; i < data.length; i++) {
			addElement(data[i]);
		}
	}

	public void setSelectedIndices(int[] selectedIndices) {
		this.selectedIndices = selectedIndices;
	}

	public int[] getSelectedIndices() {
		if (selectedIndices == null)
			return new int[] {};
		return selectedIndices;
	}
}