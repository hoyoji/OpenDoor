package com.hoyoji.ibluetooth;

public interface AsyncCallback {
	public void success(Device device, Object data);
	public void error(Device device, Exception errorException);
	public void progress(Device device, String progressMsg);
}
