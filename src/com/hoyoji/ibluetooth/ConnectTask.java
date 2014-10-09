package com.hoyoji.ibluetooth;

import java.io.IOException;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Build;

public class ConnectTask extends AsyncTask<Object, Integer, Object>{
	private BluetoothSocket mSocket;
	private final Device mDevice;
	AsyncCallback mCallbacks;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static ConnectTask newInstance(AsyncCallback callbacks, Device device, String... params){
		ConnectTask newTask = new ConnectTask(callbacks, device);
		if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			newTask.execute(params);
		} else {
			newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		}
		return newTask;
	}
	
	public ConnectTask(AsyncCallback callbacks, Device device){
		mCallbacks = callbacks;
		mDevice = device;
	}
	
	public void close(){
		if(mSocket != null){
			try {
				mSocket.close();
			} catch (IOException e) {
			}
		}
	}
    @Override
    protected void onPostExecute(Object object) {
    	if(object instanceof Exception){
    		mCallbacks.error((Exception)object);
    	} else {
    		mCallbacks.success(object);
    	}
  }
	
	@Override
	protected Object doInBackground(Object... params) {
		if(mDevice.getBluetoothAdapter().isDiscovering()){
        	mDevice.getBluetoothAdapter().cancelDiscovery();
        }
		try {
			mSocket = mDevice.getBtDevice().createRfcommSocketToServiceRecord(Const.MY_UUID);
			mSocket.connect();
		} catch (IOException connectException){
			try{
				if(mSocket != null){
					mSocket.close();
				}
			} catch (IOException closeException){
			}
			return connectException;
    	}
		
		ConnectedThread bluetoothService = new ConnectedThread(mSocket);
		bluetoothService.start();
			
		return bluetoothService;
	}	
}