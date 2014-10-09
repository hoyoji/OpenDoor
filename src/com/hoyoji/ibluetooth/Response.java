package com.hoyoji.ibluetooth;

public class Response {
	
	public final static byte CLOSED = 0x04;
	public final static byte OPENNING = 0x05;
	public final static byte OPENED = 0x06;
	
	private byte mCommand;
	private byte[] mData = {};
	
	public Response(){
		
	}
	
	
	public void parseBytes(byte[] buffer){
		byte dataLength = buffer[2];
		mData = new byte[dataLength];
		for(int i = 0; i < mData.length; i++){
			 mData[i] = buffer[4+i];
		}
		mCommand = buffer[3];
	}
	
	public void setType(byte command){
		mCommand = command;
	}
	
	public void setData(byte[] data){
		mData = data;
	}

	public byte getType() {
		return mCommand;
	}

	public String getTypeName() {
		switch(mCommand)
		{
			case OPENED : return "开门";
			case CLOSED : return "关门";
			case OPENNING : return "正在开门";
			default : return "";
		}
	}

}
