package com.hoyoji.ibluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DoorDevice extends Device {

	public DoorDevice(String name, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter) {
		super(name, btDevice, bluetoothAdapter);
	}

	public void open(final AsyncCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_OPEN);
		issueCommand(command, new AsyncCallback(){
			@Override
			public void success(final Object device) {
				waitForResponse(new AsyncCallback(){
					@Override
					public void success(Object response) {						
						disconnect(null);
						Response resp = (Response)response;
						if(callback != null){
							if(resp.getType() == Command.CMD_OPEN){
								callback.success(DoorDevice.this);
							} else {
								Exception errorException = new Exception("设备回复错误，开门可能未成功。");
								callback.error(errorException );
							}
						}
					}
					@Override
					public void progress(String msg){
						if(callback != null){
							callback.progress(msg);
						}
					}
					@Override
					public void error(Exception errorException) {
						disconnect(null);
						if(callback != null){
							callback.error(errorException);
						}
					}
				});
			}

			@Override
			public void error(Exception errorException) {
				disconnect(null);
				if(callback != null){
					callback.error(errorException);
				}
			}
			
		});
	}
	
	public void close(AsyncCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_CLOSE);
		issueCommand(command, callback);
		
	}
	
	public void stop(AsyncCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_STOP);
		issueCommand(command, callback);
		
	}



}
