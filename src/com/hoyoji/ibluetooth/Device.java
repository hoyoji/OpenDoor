package com.hoyoji.ibluetooth;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.hoyoji.ibluetooth.Command.PasswordErrorException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;

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
	
	Timer timer = new Timer(true);   
	// true 说明这个timer以daemon方式运行（优先级低，   
	// 程序结束timer也自动结束），注意，javax.swing   
	// 包中也有一个Timer类，如果import中用到swing包，   
	// 要注意名字的冲突。   
	  
	TimerTask task;
	
	public Device(Context ctx, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter){
		mBtDevice = btDevice;
		mBluetoothAdapter = bluetoothAdapter;
		mSharedPreferences = ctx.getSharedPreferences("device_info", 0);
		mPassword = mSharedPreferences.getString(btDevice.getAddress(), "0000");
		mIsRememberPassword = mSharedPreferences.getBoolean(btDevice.getAddress() + "_remember", true);
		mName = mSharedPreferences.getString(btDevice.getAddress() + "_name", btDevice.getName());
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}
	
	public void setName(String name){
		if(!mName.equals(name)){
			mSharedPreferences.edit().putString(mBtDevice.getAddress() + "_name", name).commit();
		}
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
		mSharedPreferences.edit().putBoolean(mBtDevice.getAddress()+"_remember", mIsRememberPassword).commit();
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
	

	public void changePassword(byte[] password, final AsyncCallback callback) throws Exception{
		Command command = new Command();
		command.setType(Device.TYPE_WRITEPASSWORD);
		byte[] data = {};
		
		if(password.length == 4) {
			byte[] passwordBytes = new byte[4];
			for(int i = 0; i < 4; i++){
				if(password[i] < '0' || password[i] > '9'){
					throw new Exception("密码只能包含数字");
				} else {
					passwordBytes[i] = (byte) (password[i] - 48);
				}
			}
			data = passwordBytes;
		} else {
			throw new PasswordErrorException("请输入4位数的密码");
		}
		
		command.setData(data);
		issueCommand(command, callback);
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
								if(task != null){
									task.cancel();
								}
								task = new TimerTask() {   
									public void run() {   
										disconnect(mResponseCallback);
										task = null;
										timer.purge();
									}
								};
								timer.schedule(task, 30000);   
							}
							
							Command resp = new Command();
							resp.parseResponse((byte[]) data);
							byte[] passwordBytes = resp.getPassword();
							if(passwordBytes[0] == 0xFF && passwordBytes[1] == 0xFF && passwordBytes[2] == 0xFF && passwordBytes[3] == 0xFF ){
								if(mResponseCallback != null){
									PasswordErrorException errorException = new PasswordErrorException("密码错误, " + getTypeName(resp) + " 指令发送失败");
									mResponseCallback.error(Device.this, errorException);
								}
							} else {
								if(mIsRememberPassword){
									// 如果密码正确的话，保存密码
									String password = mSharedPreferences.getString("password", "0000");
									if(!password.equals(mPassword)){
										mSharedPreferences.edit().putString(mBtDevice.getAddress(), mPassword).commit();
									}
								}
								if(mResponseCallback != null){
									mResponseCallback.success(Device.this, resp);
								}
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
