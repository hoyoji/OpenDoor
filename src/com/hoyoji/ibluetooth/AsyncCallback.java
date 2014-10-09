package com.hoyoji.ibluetooth;

public abstract class AsyncCallback {
	public abstract void success(Object object);
	public abstract void error(Exception errorException);
	public void progress(String progressMsg){}
}
