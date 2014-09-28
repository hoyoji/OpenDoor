package com.hoyoji.btcontroller;

public interface ConnectCallback {
	public void connectSuccess(Object object);
	public void connectError(Exception errorException);
}
