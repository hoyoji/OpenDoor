package com.hoyoji.ibluetooth;

public class Command {
	
	public final static byte CMD_OPEN = 0x01;
	public final static byte CMD_CLOSE = 0x02;
	public final static byte CMD_STOP = 0x03;
	public final static byte RPLY_CLOSED = 0x04;
	public final static byte RPLY_OPENNING = 0x05;
	public final static byte RPLY_OPENED = 0x06;
	
	private final static byte[] HEADING = {(byte)0xA5, 0x5A};
	private final static byte TAILING = (byte)0xAA;
	
	
	private byte mCommand;
	private byte[] mPassword;
	private byte[] mData = {};
	
	public Command(){
		
	}
	
//	public String toString(){
//		StringBuffer strBuffer = new StringBuffer();
//		strBuffer.append(HEADING);
//		strBuffer.append(mCommand);
//		strBuffer.append((byte)(mData.length + 9));
//		strBuffer.append(mPassword);
//		strBuffer.append(mData);
//		strBuffer.append(TAILING);
//		
//		return strBuffer.toString();
//	}
	
	public byte[] getBytes(){
		byte[] buffer = new byte[mData.length + 10];
		buffer[0] = HEADING[0];
		buffer[1] = HEADING[1];
		buffer[2] = (byte)buffer.length;
		buffer[3] = mCommand;
		buffer[4] = mPassword[0];
		buffer[5] = mPassword[1];
		buffer[6] = mPassword[2];
		buffer[7] = mPassword[3];
		buffer[buffer.length-1] = TAILING;
		for(int i = 0; i < mData.length; i++){
			buffer[8+i] = mData[i];
		}
		buffer[buffer.length-2] = checkSum(buffer);
		
//		buffer = {(byte)0xA5, (byte)0x5A, (byte)0x08, (byte)0x02, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0x03, (byte)0x09, (byte)0xAA};
		return buffer;
	}
	
	private byte checkSum(byte[] buffer) {
		int sum = 0;
		for(int i = 2; i < mData.length-2; i++){
			sum += buffer[i];
		}
		return (byte) (sum % 256);
	}

	public void setPassword(byte[] password) throws Exception{
		if(password.length == 4) {
			byte[] passwordBytes = new byte[4];
			for(int i = 0; i < 4; i++){
				if(password[i] < '0' || password[i] > '9'){
					throw new Exception("密码只能包含数字");
				} else {
					passwordBytes[i] = (byte) (password[i] - 48);
				}
			}
			mPassword = passwordBytes;

		} else {
			throw new PasswordErrorException("请输入4位数的密码");
		}
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

	public byte[] getPassword() {
		return mPassword;
	}
	
	public static class PasswordErrorException extends Exception{

		public PasswordErrorException(String string) {
			super(string);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = -9119473625039929740L;
		
	}
}
