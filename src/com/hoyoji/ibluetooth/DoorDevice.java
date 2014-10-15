package com.hoyoji.ibluetooth;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DoorDevice extends Device {

	public final static byte CMD_OPEN = 0x01;
	public final static byte CMD_CLOSE = 0x02;
	public final static byte CMD_STOP = 0x04;
	
	public DoorDevice(String name, BluetoothDevice btDevice, BluetoothAdapter bluetoothAdapter) {
		super(name, btDevice, bluetoothAdapter);
	}

	public void open(final AsyncCallback callback){
		Command command = new Command();
		command.setType(Device.TYPE_OUTPUT);
		byte[] data = {DoorDevice.CMD_OPEN};
		command.setData(data);
		issueCommand(command, callback);
	}
	
	public void close(final AsyncCallback callback){
		Command command = new Command();
		command.setType(Device.TYPE_OUTPUT);
		byte[] data = {DoorDevice.CMD_CLOSE};
		command.setData(data);
		issueCommand(command, callback);
		
	}
	
	public void stop(final AsyncCallback callback){
		Command command = new Command();
		command.setType(Device.TYPE_OUTPUT);
		byte[] data = {DoorDevice.CMD_STOP};
		command.setData(data);
		issueCommand(command, callback);
	}


	@Override
	public String getTypeName(Command command) {
		if(command.getData() != null){
			if(command.getType() == Device.TYPE_OUTPUT){
				switch(command.getData()[0])
				{
					case CMD_OPEN : return "开门";
					case CMD_CLOSE : return "关门";
					case CMD_STOP : return "停止";
					default : return "";
				}
			}
		}
		return super.getTypeName(command);
	}

}
