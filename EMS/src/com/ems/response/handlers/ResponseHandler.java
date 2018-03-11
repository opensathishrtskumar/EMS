package com.ems.response.handlers;

import com.ems.UI.dto.ExtendedSerialParameter;

public interface ResponseHandler {
	public void preStart();
	public void handleResponse(ExtendedSerialParameter parameter);
	public void postStop();
}
