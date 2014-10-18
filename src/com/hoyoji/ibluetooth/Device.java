package com.hoyoji.ibluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

public class Device  {
	public static final byte TYPE_OUTPUT = 0x02;
	public static final byte TYPE_WRITEPASSWORD = 0x00;
	public static final byte TYPE_READPASSWORD = 0x01;
		
	private String mName;
	private BluetoothDevice mBtDevice;
	private String mPassword;
	private ConnectedThread mConnectedThread;
	private ConnectTask mConnectTask;
	private Command mCurrentCommand;
//	private boolean mIsClosingPreviousConnection = false;
	private boolean mIsRememberPassword = false;
	private AsyncCallback mResponseCallback = null;
	private int mPendingCommandCount = 0;
//	private static Handler handler = new Handler(Looper.getMainLooper());

	private BluetoothAdapter mBluetoothAdapter;
	private SharedPreferences mSharedPreferences;
	
	public Device(Context ctx, String name, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter){
		mName = name;
		mBtDevice = btDevice;
		mBluetoothAdapter = bluetoothAdapter;
		mSharedPreferences = ctx.getSharedPreferences("device_passwords", 0);
		mPassword = mSharedPreferences.getString(btDevice.getAddress(), "0000");
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
		mSharedPreferences.edit().putString(mBtDevice.getAddress(), mPassword).commit();
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
					callback.error(this, e1);
					return;
				}
			}

			if(mConnectedThread != null && mConnectedThread.isConnected()){
				try {
					if(callback != null){
						callback.progress(this, "正在发送指令 " + getTypeName(command) + "...");
					}
					mConnectedThread.write(command.getBytes());
					mPendingCommandCount++;
//					handler.postDelayed(new Runnable(){
//						@Override
//						public void run() {
//							Exception errorException = new Exception(getTypeName(command) + "指令超时，可能未成功送达");
//							callback.error(Device.this, errorException );
//						}
//					}, 2000);
					if(callback != null){
						callback.success(this, command);
					}
				} catch (IOException e) {
					disconnect(null);
					connectAndIssueCommand(command, callback);
				}
			} else {
				connectAndIssueCommand(command, callback);
			}
	}

	public String getTypeName(Command command) {
		switch(command.getType())
		{
			case TYPE_OUTPUT : return "输出";
			case TYPE_WRITEPASSWORD : return "修改密码";
			case TYPE_READPASSWORD : return "读取密码";
			default : return Integer.toHexString(command.getType());
		}
	}
	
	private void connectAndIssueCommand(Command command, final AsyncCallback callback){
		mCurrentCommand = command;
		connect(new AsyncCallback(){
			@Override
			public void success(Device device, Object object) {
				issueCommand(mCurrentCommand, new AsyncCallback(){
					@Override
					public void success(Device device, Object object) {
						if(callback != null){
							callback.success(device, object);
						}
						mCurrentCommand = null;
					}
					@Override
					public void progress(Device device, String msg){
						if(callback != null){
							callback.progress(device, msg);
						}
					}
					@Override
					public void error(Device device, Exception errorMsg) {
						if(callback != null){
							callback.error(device, errorMsg);
						}
					}
				});
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(callback != null){
					callback.error(device, errorMsg);
				}
			}
			@Override
			public void progress(Device device, String progressMsg) {
				if(callback != null){
					callback.progress(device, progressMsg);
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
				callback.progress(this, "正在连接...");
			}
        	mConnectTask = ConnectTask.newInstance(new AsyncCallback(){
				@Override
				public void success(Device device, Object thread) {
					mConnectedThread = (ConnectedThread)thread;
					mConnectedThread.setResponseCallback(new AsyncCallback(){
						@Override
						public void success(Device device, Object data) {
							if(mPendingCommandCount > 0){
								mPendingCommandCount--;
							}
							if(mPendingCommandCount == 0){
								disconnect(mResponseCallback);
							}
							if(mResponseCallback != null){
								Command resp = new Command();
								resp.parseResponse((byte[]) data);
								mResponseCallback.success(Device.this, resp);
							}
						}
				
						@Override
						public void error(Device device, Exception errorException) {
							mPendingCommandCount = 0;
//							if(mResponseCallback != null){
//								mResponseCallback.error(Device.this, errorException);
//							}
						}
				
						@Override
						public void progress(Device device, String progressMsg) {
							if(mResponseCallback != null){
								mResponseCallback.progress(Device.this, progressMsg);
							}
						}
						
					});
					if(callback != null){
						callback.success(device, thread);
					}
				}

				@Override
				public void error(Device device, Exception errMsg) {
					mConnectTask = null;
					if(callback != null){
						callback.error(device, errMsg);
					}
				}

				@Override
				public void progress(Device device, String progressMsg) {
					if(callback != null){
						callback.progress(device, progressMsg);
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

	public void setResponseCallback(final AsyncCallback asyncCallback) {
		this.mResponseCallback = asyncCallback;
	}
}
