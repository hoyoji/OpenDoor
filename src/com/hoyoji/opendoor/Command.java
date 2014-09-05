package com.hoyoji.opendoor;

public class Command {
	
	public final static byte CMD_OPEN = 0x01;
	public final static byte CMD_CLOSE = 0x02;
	public final static byte CMD_STOP = 0x03;
	public final static byte RPLY_CLOSED = 0x04;
	public final static byte RPLY_OPENNING = 0x05;
	public final static byte RPLY_OPENED = 0x06;
	
	private final static byte[] HEADING = {(byte)0xA5, 0x5A};
	private final static byte TAILING = (byte)0xAA;
	
	
	byte mCommand;
	byte[] mPassword;
	byte[] mData = {};
	
	public Command(){
		
	}
	
	public String toString(){
		StringBuffer strBuffer = new StringBuffer();
		strBuffer.append(HEADING);
		strBuffer.append(mCommand);
		strBuffer.append((byte)(mData.length + 9));
		strBuffer.append(mPassword);
		strBuffer.append(mData);
		strBuffer.append(TAILING);
		
		return strBuffer.toString();
	}
	
	public void setPassword(byte[] password){
		mPassword = password;
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
			case CMD_OPEN : return "开门";
			case CMD_CLOSE : return "关门";
			case CMD_STOP : return "停止";
			default : return "";
		}
	}
}
