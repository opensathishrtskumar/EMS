package com.ems.UI.internalframes;

import java.util.HashMap;
import java.util.Map;


public abstract class IFrameMapping {
	private static Map<Integer, String> frameMapping = new HashMap<Integer, String>();

	static {
		frameMapping.put(PingInternalFrame.class.getCanonicalName().hashCode(),
				PingInternalFrame.class.getCanonicalName());

		frameMapping.put(
				ManageDeviceIFrame.class.getCanonicalName().hashCode(),
				ManageDeviceIFrame.class.getCanonicalName());
	}

	public static Map<Integer, String> getFrameMapping() {
		return frameMapping;
	}

	public static int getCanonicalHashCode(Object obj){
		int hashCode = 0;

		if(obj != null){
			hashCode = obj.getClass().getCanonicalName().hashCode();
		}

		return hashCode;
	}

	public static int getCanonicalHashCode(Class className){
		int hashCode = 0;

		if(className != null){
			hashCode = className.getCanonicalName().hashCode();
		}

		return hashCode;
	}
}
