package com.hoyoji.opendoor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.hoyoji.opendoor.R;

public class MainActivity extends ListActivity {

    private final UUID MY_UUID =  UUID.fromString( "E35C83BD-34EA-4C13-90BE-195D1134253A");
	private static final int REQUEST_CONNECT_DEVICE = 0;
	final int REQUEST_ENABLE_BT = 1;
    
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mSelectedDevice;
	private String mSelectedDeviceName;
	
	private BluetoothServiceThread mBluetoothService;
	
	private TextView mTextViewStatus;
	private EditText mEditTextPassword;
	private Button mBtnStop;
	private Button mBtnClose;
	private Button mBtnOpen;
//	private TextView mTextViewEmpty;
	private TextView mTextViewFooter;
	
	private ArrayList<String> mDevicesTitleArray = new ArrayList<String>();
	private ArrayList<BluetoothDevice> mDevicesArray = new ArrayList<BluetoothDevice>();
	private Command mCurrentCommand;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextViewStatus = (TextView) findViewById(R.id.mainTextViewStatus);
		mEditTextPassword = (EditText) findViewById(R.id.mainEditTextPassword);
		mBtnStop = (Button) findViewById(R.id.mainBtnStop);
		mBtnClose = (Button) findViewById(R.id.mainBtnClose);
		mBtnOpen = (Button) findViewById(R.id.mainBtnOpen);
    	
//		mBtnConnect.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				connect();
//			}
//		});
		
		mBtnOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Command openCommand = new Command();
				openCommand.setType(Command.CMD_OPEN);
				issueCommand(openCommand);
			}
		});
		
		mBtnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Command openCommand = new Command();
				openCommand.setType(Command.CMD_CLOSE);
				issueCommand(openCommand);
			}
		});
		
		mBtnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Command openCommand = new Command();
				openCommand.setType(Command.CMD_STOP);
				issueCommand(openCommand);
			}
		});
		
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDevicesTitleArray);
        setListAdapter(adapter);
        
//        mTextViewEmpty = (TextView)findViewById(android.R.id.empty);
//        mTextViewEmpty.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				discoverDevices();
//			}
//		});
        
        mTextViewFooter = new TextView(this);
        mTextViewFooter.setText("请点击搜索更多设备");
        mTextViewFooter.setGravity(Gravity.CENTER);
        mTextViewFooter.setPadding(0, 20, 0, 20);
        mTextViewFooter.setTextSize(16f);
        mTextViewFooter.setBackgroundColor(Color.LTGRAY);
        ListView.LayoutParams layoutParams = new ListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTextViewFooter.setLayoutParams(layoutParams);
        mTextViewFooter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				discoverDevices();
			}
		});

		getListView().setFooterDividersEnabled(true);
        getListView().addFooterView(mTextViewFooter, null, false);
        
        IntentFilter foundfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	this.registerReceiver(foundReceiver, foundfilter);

        IntentFilter discoverfilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    	this.registerReceiver(discoverReceiver, discoverfilter);
		
    	IntentFilter connectionfilter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
    	this.registerReceiver(connectionReceiver, connectionfilter);
		
   		discoverDevices();
	}
	
	private void issueCommand(Command command){
		if(mSelectedDevice == null){
			mTextViewStatus.setText("请选择要连接的设备。");
			Toast.makeText(getApplicationContext(), "请选择要连接的设备。", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String password = mEditTextPassword.getText().toString();
		
		if(password.length() == 4) {
		
			byte[] passwordBytes = new byte[4];
			for(int i = 0; i < 4; i++){
				if(password.charAt(i) < '0' || password.charAt(i) > '9'){
//					Toast.makeText(getApplicationContext(), "密码只能包含数字", Toast.LENGTH_SHORT).show();
					mEditTextPassword.setError("密码只能包含数字");
					return;
				} else {
					passwordBytes[i] = (byte) (password.charAt(i) - 48);
				}
			}

			mCurrentCommand = command;
			if(mBluetoothService != null && mBluetoothService.isConnected()){
				command.setPassword(passwordBytes);
				mBluetoothService.write(command.toString().getBytes());
			} else {
				connectToBtDevice(mSelectedDeviceName, mSelectedDevice);
			}
		} else {
			Toast.makeText(getApplicationContext(), "请输入4位数的密码", Toast.LENGTH_SHORT).show();
			mEditTextPassword.setError("请输入4位数的密码");
		}
	}
	
	private void populatePariedDevices() {
    	mDevicesArray.clear();
    	mDevicesTitleArray.clear();
		((ArrayAdapter)getListAdapter()).notifyDataSetChanged();

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){
			for(BluetoothDevice device : pairedDevices){
				mDevicesTitleArray.add(device.getName());	
				mDevicesArray.add(device);
			}
			((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
		}
		
	}

	private final BroadcastReceiver foundReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if(device.getBondState() == BluetoothDevice.BOND_BONDED){
					for(int i=0; i < mDevicesArray.size(); i++){
						BluetoothDevice dev = mDevicesArray.get(i);
						if(dev.getAddress().equals(device.getAddress())){
							// 该设备已经在列表中
							TextView textView = (TextView)getListView().getChildAt(i).findViewById(android.R.id.text1);
							textView.setTextColor(Color.BLUE);
							return;
						}
					}
					return;
				}
				mDevicesTitleArray.add(device.getName());	
				mDevicesArray.add(device);
				
				((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
			}
			
		}
		
	};
	private final BroadcastReceiver discoverReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
//				mTextViewEmpty.setText("请点击搜索设备");
				mTextViewFooter.setText("请点击搜索更多设备");
			}
			
		}
		
	};
	
	private final BroadcastReceiver connectionReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
//	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  
	            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);  
	            if (state == BluetoothAdapter.STATE_CONNECTED) {  
	            } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {  
					mBluetoothService = null;
	                mTextViewStatus.setText("连接已断开，请重新连接设备。");
	            }  
			}
			
		}
		
	};
	private ConnectThread mConnectThread;
	
	protected void discoverDevices() {
		if (mBluetoothAdapter == null) {
			mTextViewStatus.setTextColor(Color.parseColor("#FF0000"));
	        mTextViewStatus.setText("找不到蓝牙模块！");
		} else {
            if (mBluetoothAdapter.isEnabled()) {
            	populatePariedDevices();
        		if(!mBluetoothAdapter.isDiscovering()){
        			if(mBluetoothAdapter.startDiscovery()){
//	        			mTextViewEmpty.setText("正在搜索设备...");
	        			mTextViewFooter.setText("正在搜索设备...");
        			}
        		}
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            	mTextViewStatus.setText("正在开启蓝牙...");
            }
        }
		
	}
	
	
//	protected void connect() {
//		if(mBluetoothService != null){
//			mBluetoothService.cancel();
//			mBluetoothService = null;
//			enableCommandButtons(false);
//		} else {
//			if (mBluetoothAdapter == null) {
//				mTextViewStatus.setTextColor(Color.parseColor("#FF0000"));
//		        mTextViewStatus.setText("找不到蓝牙模块！");
//			}else{
//	            if (!mBluetoothAdapter.isEnabled()) {
//	                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//	                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//	            	mTextViewStatus.setText("正在开启蓝牙...");
//	            } else {
//	            	if(mSelectedDevice == null){
//	            		mTextViewStatus.setText("蓝牙已开启，请选择要连接的设备。");
//	            	}
//		        	selectBtDevice();
//	            }
//	        }
//		}
//	}

	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
    	BluetoothDevice selectedDevice = mDevicesArray.get(position);
    	String deviceName = ((TextView)v).getText().toString();
    	mCurrentCommand = null;
    	connectToBtDevice(deviceName, selectedDevice);
    }  
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == REQUEST_ENABLE_BT){
	        if(resultCode == RESULT_OK) {
	        	mTextViewStatus.setText("蓝牙已开启，请选择要连接的设备。");
	    		discoverDevices();
	        } else {
	        	mTextViewStatus.setText("请开启蓝牙后再尝试连接！");
	        }
    	} else if(requestCode == REQUEST_CONNECT_DEVICE){
	        if(resultCode == RESULT_OK) {
	        	connectToBtDevice(mSelectedDeviceName, mSelectedDevice);
	        } else {
	        	mTextViewStatus.setText("请开启蓝牙后再尝试连接！");
	        }
    	}
//    	else if(requestCode == SELECT_BT_DEVICE){
//	        if(resultCode == RESULT_OK) {
//	        	BluetoothDevice selectedDevice = data.getParcelableExtra("DEVICE");
//	        	connectToBtDevice(data.getStringExtra("DEVICE_NAME"), selectedDevice);
//	        } else {
//	        }
//    	}

    }
    
    private void connectToBtDevice(String deviceName, BluetoothDevice device) {
    	mSelectedDevice = device;
    	mSelectedDeviceName = deviceName;
         if (mBluetoothAdapter.isEnabled()) {
    	   	if(mConnectThread != null){
        		if(mConnectThread.getDevice() == mSelectedDevice){
        			return;
        		} else {
        			mConnectThread.close();
        		}
        	}

        	if(mBluetoothService != null){
				mBluetoothService.cancel();
				mBluetoothService = null;
        	}
        	mConnectThread = new ConnectThread(deviceName, mSelectedDevice, mHandler);
        	mConnectThread.start();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_CONNECT_DEVICE);
        	mTextViewStatus.setText("正在开启蓝牙...");
        }
    }
//
//	private void enableCommandButtons(boolean b){
//		mBtnStop.setEnabled(b);
//		mBtnClose.setEnabled(b);
//		mBtnOpen.setEnabled(b);
//    }
//    
//	private void selectBtDevice() {
//		Intent intent = new Intent(this, DevicesActivity.class);
//		startActivityForResult(intent, SELECT_BT_DEVICE);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
           String data = (String)msg.obj;
           mTextViewStatus.setText(data);
           
           if(msg.what == 2){
        	   // 发送成功
           } else if(msg.what == 1){
        	   // 连接成功
        	   mConnectThread = null;
   	           if(mCurrentCommand != null){
   	        	   issueCommand(mCurrentCommand);
   	           }
           } else if(msg.what == 0){
        	   // 正在连接
           } else if(msg.what == -1){
        	   mConnectThread = null;
        	   // 连接错误
           } else if(msg.what == -2){
        	   // 发送错误
                mTextViewStatus.setText("连接已断开，请重新连接设备。");
	   			mBluetoothService.cancel();
	   			mBluetoothService = null;
   	           if(mCurrentCommand != null){
   	        	   issueCommand(mCurrentCommand);
   	           }
           }
        }

    };

    private class ConnectThread extends Thread{
    	private BluetoothSocket mSocket;
    	private final BluetoothDevice mDevice;
        private final Handler mHandler;
        private final String mDeviceName;
    	
    	public ConnectThread(String deviceName, BluetoothDevice device, Handler handler){
    		mHandler = handler;
    		mDeviceName = deviceName;
    		mDevice = device;
    		
//    		BluetoothSocket tmpSocket = null;
//    		try {
//    			tmpSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
//    	   } catch (IOException e) {
//               mHandler.obtainMessage(1, -1, -1, "无法连接到设备：" + e.getMessage()).sendToTarget();
//    		}
//			mSocket = tmpSocket;
    	}
    	
    	public void run(){
            mHandler.obtainMessage(0, -1, -1, "正在连接到设备：" + mDeviceName + "...").sendToTarget();
            if(mBluetoothAdapter.isDiscovering()){
            	mBluetoothAdapter.cancelDiscovery();
            }
    		try {
    			mSocket = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
    			mSocket.connect();
    		} catch (IOException connectException){
                mHandler.obtainMessage(-1, -1, -1, "无法连接到设备：" + mDeviceName + "\n设备未开启或不在附近").sendToTarget();
    			try{
    				if(mSocket != null){
    					mSocket.close();
    				}
    			} catch (IOException closeException){
    			}
    			return;
        	}
    		
//    		if(mSocket.isConnected()){
    			mBluetoothService = new BluetoothServiceThread(mSocket, mHandler);
    			mBluetoothService.start();
    			mHandler.obtainMessage(1, -1, -1, "已连接到设备：" + mDeviceName).sendToTarget();
//    		}

    	}
    	
    	public BluetoothDevice getDevice(){
    		return mDevice;
    	}
    	
    	public void close(){
    		if(mSocket != null){
    			try {
					mSocket.close();
				} catch (IOException e) {
				}
    		}
    	}
    }
    
    private class BluetoothServiceThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler mHandler;

        public BluetoothServiceThread(BluetoothSocket socket, Handler handler) {
            mmSocket = socket;
            mHandler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[259];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(2, bytes, -1, new String(buffer)).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public boolean isConnected(){
        	return mmSocket.isConnected();
        }
        
        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
                String cmdName = "";
                if(mCurrentCommand != null){
                	cmdName = mCurrentCommand.getTypeName();
                }
                mHandler.obtainMessage(2, -1, -1, cmdName + " 命令发送成功。").sendToTarget();
				mCurrentCommand = null;
            } catch (IOException e) { 
                mHandler.obtainMessage(-2, -1, -1, e.getMessage()).sendToTarget();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
            	mmSocket.close();
            } catch (IOException e) { }
        }
    }


	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		unregisterReceiver(foundReceiver);
		unregisterReceiver(discoverReceiver);
		unregisterReceiver(connectionReceiver);
	}

}
