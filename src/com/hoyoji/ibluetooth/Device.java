package com.hoyoji.ibluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class Device  {
	private String mName;
	private BluetoothDevice mBtDevice;
	private String mPassword;
	private ConnectedThread mConnectedThread;
	private ConnectTask mConnectTask;
	private Command mCurrentCommand;
//	private boolean mIsClosingPreviousConnection = false;
	private boolean mIsRememberPassword = false;

	private BluetoothAdapter mBluetoothAdapter;
	
	public Device(String name, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter){
		mName = name;
		mBtDevice = btDevice;
		mBluetoothAdapter = bluetoothAdapter;
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}
	
	public void setName(String name){
		mName = name;
	}
	
	public String getName(){
		return mName;
	}
	
	public void setPassword(String password){
		mPassword = password;
	}
	
	public String getPassword(){
		return mPassword;
	}
	public void setIsRememberPassword(boolean isRememberPassword){
		mIsRememberPassword = isRememberPassword;
	}
	
	public boolean getIsRememberPassword(){
		return mIsRememberPassword;
	}

	public Command getCurrentCommand() {
		return mCurrentCommand;
	}
	
	public String toString(){
		return mName;
	}

	public BluetoothDevice getBtDevice() {
		return mBtDevice;
	}
	
	
	
	protected void issueCommand(final Command command, final AsyncCallback callback){

			try {
				command.setPassword(mPassword.getBytes());
			} catch (Exception e1) {
				if(callback != null){
					callback.error(e1);
					return;
				}
			}

			if(mConnectedThread != null && mConnectedThread.isConnected()){
				try {
					if(callback != null){
						callback.progress("正在发送命令 " + command.getTypeName() + " 到设备: " + getName() + "...");
					}
					mConnectedThread.write(command.getBytes());
					if(callback != null){
						callback.success(this);
					}
				} catch (IOException e) {
					disconnect(null);
					connectAndIssueCommand(command, callback);
				}
			} else {
				connectAndIssueCommand(command, callback);
			}
	}
	
	private void connectAndIssueCommand(Command command, final AsyncCallback callback){
		mCurrentCommand = command;
		connect(new AsyncCallback(){
			@Override
			public void success(Object object) {
				issueCommand(mCurrentCommand, new AsyncCallback(){
					@Override
					public void success(Object object) {
						if(callback != null){
							callback.success(object);
						}
						mCurrentCommand = null;
					}
					@Override
					public void progress(String msg){
						if(callback != null){
							callback.progress(msg);
						}
					}
					@Override
					public void error(Exception errorMsg) {
						if(callback != null){
							callback.error(errorMsg);
						}
					}
				});
			}
			@Override
			public void error(Exception errorMsg) {
				if(callback != null){
					callback.error(errorMsg);
				}
			}
			@Override
			public void progress(String progressMsg) {
				if(callback != null){
					callback.progress(progressMsg);
				}
				
			}
		});
	}
	
	public void connect(final AsyncCallback callback) {
	    	if(mConnectedThread != null && mConnectedThread.isConnected()){
	    		return;
	    	}
	    	mConnectedThread = null;
    	   	if(mConnectTask != null){
    	   		mConnectTask.close();
        	}

			if(callback != null){
				callback.progress("正在连接到设备: " + getName() + " ...");
			}
        	mConnectTask = ConnectTask.newInstance(new AsyncCallback(){
				@Override
				public void success(Object thread) {
					mConnectedThread = (ConnectedThread)thread;

					if(callback != null){
						callback.success(this);
					}
				}

				@Override
				public void error(Exception errMsg) {
					mConnectTask = null;
					if(callback != null){
						callback.error(errMsg);
					}
				}

				@Override
				public void progress(String progressMsg) {
					if(callback != null){
						callback.progress(progressMsg);
					}
					
				}
        	}, this);
    }
	
	public void disconnect(AsyncCallback object) {	    	
		if(mConnectTask != null){
	   		mConnectTask.close();
	   		mConnectTask = null;
		}
		if(mConnectedThread != null){
			 mConnectedThread.cancel();
			 mConnectedThread = null;
		}
	}

	public void waitForResponse(AsyncCallback asyncCallback) {
		if(mConnectedThread == null || !mConnectedThread.isConnected()){
			if(asyncCallback != null){
				Exception errorException = new Exception("尚未连接到设备: " + getName());
				asyncCallback.error(errorException);
			}
		} else {
			if(asyncCallback != null){
				asyncCallback.progress("正在等待设备回复...");
			}
			mConnectedThread.readResponse(asyncCallback);
		}
		
	}
}
