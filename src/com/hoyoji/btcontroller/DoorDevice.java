package com.hoyoji.btcontroller;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DoorDevice extends Device {

	public DoorDevice(String name, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter) {
		super(name, btDevice, bluetoothAdapter);
	}

	public void open(ConnectCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_OPEN);
		issueCommand(command, callback);
	}
	
	public void close(ConnectCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_CLOSE);
		issueCommand(command, callback);
		
	}
	
	public void stop(ConnectCallback callback){
		Command command = new Command();
		command.setType(Command.CMD_STOP);
		issueCommand(command, callback);
		
	}


}
