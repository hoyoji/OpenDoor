package com.hoyoji.ibluetooth;

import java.util.ArrayList;
import java.util.Set;
import android.os.Bundle;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoyoji.ibluetooth.R;

public class MainActivity extends ListActivity {

//	private static final int REQUEST_CONNECT_DEVICE = 0;
	final int REQUEST_ENABLE_BT = 1;
    
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private DoorDevice mSelectedDevice;
	
	private TextView mTextViewStatus;
	private EditText mEditTextPassword;
	private Button mBtnStop;
	private Button mBtnClose;
	private Button mBtnOpen;
//	private TextView mTextViewEmpty;
	private TextView mTextViewFooter;
	
	private ArrayList<DoorDevice> mDevicesArray = new ArrayList<DoorDevice>();

	private byte mCurrentCommand = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextViewStatus = (TextView) findViewById(R.id.mainTextViewStatus);
		mEditTextPassword = (EditText) findViewById(R.id.mainEditTextPassword);
		mBtnStop = (Button) findViewById(R.id.mainBtnStop);
		mBtnClose = (Button) findViewById(R.id.mainBtnClose);
		mBtnOpen = (Button) findViewById(R.id.mainBtnOpen);
    	
		mBtnOpen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doBtnOpen();
			}
		});
		
		mBtnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doBtnClose();
			}
		});
		
		mBtnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doBtnStop();
			}
		});
		
		ListAdapter adapter = new ArrayAdapter<DoorDevice>(this, android.R.layout.simple_list_item_checked, mDevicesArray);
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

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		getListView().setFooterDividersEnabled(true);
        getListView().addFooterView(mTextViewFooter, null, false);
        
        IntentFilter foundfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	this.registerReceiver(foundReceiver, foundfilter);

        IntentFilter discoverfilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    	this.registerReceiver(discoverReceiver, discoverfilter);
		
   		discoverDevices();
	}


	protected void doBtnStop() {
		mEditTextPassword.setEnabled(false);
		mEditTextPassword.setEnabled(true);

		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mCurrentCommand = DoorDevice.CMD_STOP;
            return;
    	}
		
		if(mSelectedDevice == null){
			ToastUtils.showMessage(getApplicationContext(), "请先选择一个设备！");
			return;
		}
		mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
		mSelectedDevice.stop(new AsyncCallback(){
			@Override
			public void success(Device device, Object data) {
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": 停止指令已发送");
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(errorMsg instanceof Command.PasswordErrorException){
					mEditTextPassword.setError(errorMsg.getMessage());
				}
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
			}
			@Override
			public void progress(Device device, String progressMsg) {
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + progressMsg);
			}
			
		});
		
	}


	protected void doBtnClose() {
		mEditTextPassword.setEnabled(false);
		mEditTextPassword.setEnabled(true);
		
		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mCurrentCommand = DoorDevice.CMD_CLOSE;
            return;
    	}
		
		if(mSelectedDevice == null){
			ToastUtils.showMessage(getApplicationContext(), "请先选择一个设备！");
			return;
		}
		mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
		mSelectedDevice.close(new AsyncCallback(){
			@Override
			public void success(Device device, Object data) {
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": 关门指令已发送");
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(errorMsg instanceof Command.PasswordErrorException){
					mEditTextPassword.setError(errorMsg.getMessage());
				}
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
			}
			@Override
			public void progress(Device device, String progressMsg) {
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + progressMsg);
				
			}
			
		});
		
	}


	protected void doBtnOpen() {
		mEditTextPassword.setEnabled(false);
		mEditTextPassword.setEnabled(true);

		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mCurrentCommand = DoorDevice.CMD_OPEN;
            return;
    	}

		if(mSelectedDevice == null){
			ToastUtils.showMessage(getApplicationContext(), "请先选择一个设备！");
			return;
		}
		
		mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
		mSelectedDevice.open(new AsyncCallback(){
			@Override
			public void success(Device device, Object data) {
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": 开门指令已发送");
				mBtnOpen.setEnabled(true);
			}
			@Override
			public void progress(Device device, String msg){
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + msg);
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(errorMsg instanceof Command.PasswordErrorException){
					mEditTextPassword.setError(errorMsg.getMessage());
				} 
				ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
			}
		});
	}


	private void populatePariedDevices() {
    	mDevicesArray.clear();
		((ArrayAdapter)getListAdapter()).notifyDataSetChanged();

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){
			for(BluetoothDevice btDevice : pairedDevices){
				mDevicesArray.add(createDoorDevcie(btDevice.getName(), btDevice, mBluetoothAdapter));
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
						DoorDevice dev = mDevicesArray.get(i);
						if(dev.getBtDevice().getAddress().equals(device.getAddress())){
							// 该设备已经在列表中
							TextView textView = (TextView)getListView().getChildAt(i).findViewById(android.R.id.text1);
							textView.setTextColor(Color.BLUE);
							return;
						}
					}
					return;
				}
				mDevicesArray.add(createDoorDevcie(device.getName(), device, mBluetoothAdapter));
				
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
//            	mTextViewStatus.setText("正在开启蓝牙...");
                ToastUtils.showMessage(getApplicationContext(), "正在开启蓝牙...");
            }
        }
		
	}
	
	private AsyncCallback mDoorDeviceCallback = new AsyncCallback(){
		@Override
		public void success(Device device, Object response) {
			Command command = (Command)response;
			ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + device.getTypeName(command) + " 指令已送达");
		}
		
		@Override
		public void progress(Device device, String msg){
			ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + msg);
		}
		
		@Override
		public void error(Device device, Exception errorException) {
			if(errorException instanceof Command.PasswordErrorException){
				mEditTextPassword.setError(errorException.getMessage());
			} 
			ToastUtils.showMessage(getApplicationContext(), device.getName() + ": " + errorException.getMessage());
		}
	};

	protected DoorDevice createDoorDevcie(String name, BluetoothDevice device,
			BluetoothAdapter mBluetoothAdapter) {
		DoorDevice dev = new DoorDevice(name, device, mBluetoothAdapter);
		dev.setResponseCallback(mDoorDeviceCallback);
		return dev;
	}


	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		mSelectedDevice = mDevicesArray.get(position);
		String password = mSelectedDevice.getPassword();
		if(password != null && password.length() > 0){
			mEditTextPassword.setText(password);
		} else {
			mEditTextPassword.setText("0000");
		}
    	mEditTextPassword.setError(null);
    	
        if (mBluetoothAdapter.isEnabled()) {
//    		mSelectedDevice.connect(new ConnectCallback(){
//				@Override
//				public void connectSuccess(Object device) {
//					mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
//					ToastUtils.showMessage(getApplicationContext(), "已成功连接到设备: " + device.getName());
//				}
//				@Override
//				public void connectError(Exception errorMsg) {
//					ToastUtils.showMessage(getApplicationContext(), errorMsg.getMessage());
//				}
//    		});
    	} else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        	mTextViewStatus.setText("正在连接到设备: " + mSelectedDevice.getName());
//			ToastUtils.showMessage(getApplicationContext(), "正在连接到设备: " + mSelectedDevice.getName());
    	}
    }  
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == REQUEST_ENABLE_BT){
	        if(resultCode == RESULT_OK) {
	        	mTextViewStatus.setText("蓝牙已开启，请选择设备。");
	        	if(mCurrentCommand == -1){
	        		discoverDevices();
	        	} else if(mCurrentCommand == DoorDevice.CMD_OPEN){
	        		doBtnOpen();
	        	} if(mCurrentCommand == DoorDevice.CMD_CLOSE){
	        		doBtnClose();
	        	} if(mCurrentCommand == DoorDevice.CMD_STOP){
	        		doBtnStop();
	        	} 
	        } else {
//	        	mTextViewStatus.setText("请开启蓝牙后再尝试连接！");
	        	ToastUtils.showMessage(getApplicationContext(), "请开启蓝牙！");
	        }
    	} 
//    	else if(requestCode == REQUEST_CONNECT_DEVICE){
//	        if(resultCode == RESULT_OK) {
//	        	mSelectedDevice.connect(new AsyncCallback(){
//					@Override
//					public void success(Object device) {
//						mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
//						if(mSelectedDevice.getCurrentCommand() != null){
//							ToastUtils.showMessage(getApplicationContext(), mSelectedDevice.getCurrentCommand().getTypeName() + "指令已发送到设备: " + device.getName());
//						} else {
//							mSelectedDevice.disconnect(null);
////							ToastUtils.showMessage(getApplicationContext(), "已成功连接到设备: " + device.getName());
//						}
//					}
//					@Override
//					public void error(Exception errorMsg) {
//						ToastUtils.showMessage(getApplicationContext(), errorMsg.getMessage());
//					}
//					@Override
//					public void progress(String progressMsg) {
//						ToastUtils.showMessage(getApplicationContext(), progressMsg);
//						
//					}
//	    			
//	    		});
//	        } else {
////	        	mTextViewStatus.setText("请开启蓝牙后再尝试连接！");
//				ToastUtils.showMessage(getApplicationContext(), "请开启蓝牙后再尝试连接！");
//	        }
//    	}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
		unregisterReceiver(foundReceiver);
		unregisterReceiver(discoverReceiver);
	}
}

