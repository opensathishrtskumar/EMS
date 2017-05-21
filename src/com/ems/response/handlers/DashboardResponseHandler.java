package com.ems.response.handlers;

import static com.ems.constants.EmsConstants.GAP_BETWEEN_REQUEST;
import static com.ems.util.EMSSwingUtils.getDeviceDetailLabel;
import static com.ems.util.EMSUtility.DASHBOARD_POLLED_FMT;
import static com.ems.util.EMSUtility.getFormattedDate;

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ems.UI.dto.ExtendedSerialParameter;
public class DashboardResponseHandler implements ResponseHandler{
	
	private static final Logger logger = LoggerFactory
			.getLogger(DashboardResponseHandler.class);
	
	private JLabel polledOn = null;
	
	public JLabel getPolledOn() {
		return polledOn;
	}

	public void setPolledOn(JLabel polledOn) {
		this.polledOn = polledOn;
	}

	@Override
	public void handleResponse(ExtendedSerialParameter parameter) {
		
		if(parameter == null || parameter.getPanel() == null)
			return;
		try {
			Thread.sleep(GAP_BETWEEN_REQUEST);
			JLabel label = getDeviceDetailLabel(parameter);
			JScrollPane panel = parameter.getPanel();
			panel.setViewport(null);
			panel.setViewportView(label);
			panel.revalidate();
		} catch (Exception e) {
			logger.error("Reponse handler error : {}",e.getLocalizedMessage());
			logger.error("{}",e);
		} finally{
			if(polledOn != null){
				String polled = getFormattedDate(DASHBOARD_POLLED_FMT);
				polledOn.setText(polled);
			}
		}
	}

	@Override
	public void preStart() {
		//TODO : 
	}

	@Override
	public void postStop() {
		//TODO : 
	}
}
