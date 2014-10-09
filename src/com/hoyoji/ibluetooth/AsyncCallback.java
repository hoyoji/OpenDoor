package com.hoyoji.ibluetooth;

public interface AsyncCallback {
	public void success(Object object);
	public void error(Exception errorException);
	public void progress(String progressMsg);
}
