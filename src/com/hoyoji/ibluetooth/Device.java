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
	private ConnectedThread mBluetoothService;
	private ConnectTask mConnectThread;
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

			if(mBluetoothService != null && mBluetoothService.isConnected()){
				try {
					mBluetoothService.write(command.getBytes());
					if(callback != null){
						callback.success(this);
					}
				} catch (IOException e) {
//					if(callback != null){
//						callback.connectError(e.getMessage());
//					}
					mBluetoothService.cancel();
					mBluetoothService = null;
					mCurrentCommand = command;
					connect(new AsyncCallback(){
						@Override
						public void success(Object object) {
							if(callback != null){
								callback.success(object);
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
			} else {
				mCurrentCommand = command;
				connect(new AsyncCallback(){
					@Override
					public void success(Object object) {
						if(callback != null){
							callback.success(object);
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
	}
	
	public void connect(final AsyncCallback callback) {
	    	if(mBluetoothService != null && mBluetoothService.isConnected()){
	    		return;
	    	}
	    	mBluetoothService = null;
    	   	if(mConnectThread != null){
    	   		mConnectThread.close();
        	}
        	mConnectThread = ConnectTask.newInstance(new AsyncCallback(){
				@Override
				public void success(Object thread) {
					mBluetoothService = (ConnectedThread)thread;
//					if(callback != null){
//						callback.connectSuccess(Device.this);
//					}
					if(mCurrentCommand != null){
						issueCommand(mCurrentCommand, new AsyncCallback(){

							@Override
							public void success(Object object) {
								if(callback != null){
									callback.success(object);
								}
								mCurrentCommand = null;
							}
							@Override
							public void error(Exception errorMsg) {
								if(callback != null){
									callback.error(errorMsg);
								}
							}
						});
					}
				}

				@Override
				public void error(Exception errMsg) {
					mConnectThread = null;
					if(callback != null){
						callback.error(errMsg);
					}
				}
        	}, this);
    }
}
