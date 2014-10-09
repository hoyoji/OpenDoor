package com.hoyoji.ibluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DoorDevice extends Device {

	public DoorDevice(String name, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter) {
		super(name, btDevice, bluetoothAdapter);
	}

	public void open(AsyncCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_OPEN);
		issueCommand(command, callback);
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
