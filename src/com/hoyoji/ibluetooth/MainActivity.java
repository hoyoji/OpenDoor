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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
	private CheckBox mIsRememberPassword;
	private TextView mTextViewPortNumber;
	private TextView mTextViewTimeOpen;
	private TextView mTextViewTimeMultiple;
	private TextView mTextViewTimeDelay;
	
	
	private ArrayList<DoorDevice> mDevicesArray = new ArrayList<DoorDevice>();

	private byte mCurrentCommand = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextViewPortNumber = (TextView) findViewById(R.id.mainPortNumber);
		mTextViewTimeOpen = (TextView) findViewById(R.id.mainTimeOpen);
		mTextViewTimeDelay = (TextView) findViewById(R.id.mainTimeDelay);
		mTextViewTimeMultiple = (TextView) findViewById(R.id.mainTimeMultiple);
		
		mTextViewStatus = (TextView) findViewById(R.id.mainTextViewStatus);
		mEditTextPassword = (EditText) findViewById(R.id.mainEditTextPassword);
		mEditTextPassword.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable s) {
				if(mSelectedDevice != null){
					mSelectedDevice.setPassword(s.toString());
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		});
		
		mIsRememberPassword = (CheckBox) findViewById(R.id.mainCheckBoxRememberPasword);
		mIsRememberPassword.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if(mSelectedDevice != null){
					mSelectedDevice.setIsRememberPassword(isChecked);
					if(isChecked){
						mEditTextPassword.setText(mSelectedDevice.getPassword());
					} else {
						mEditTextPassword.setText("");
					}
					mEditTextPassword.setError(null);
				}
			}
		});
		
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
		
	}


	@Override
	protected void onResume() {
		super.onResume();
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
			ToastUtils.showMessageLong(getApplicationContext(), "请先选择一个设备！");
			return;
		}
		if(mTextViewPortNumber.getText() == null || mTextViewPortNumber.getText().length() == 0){
			ToastUtils.showMessageLong(getApplicationContext(), "请输入端口号！");
			return;
		}
		int portNumber = Integer.parseInt(mTextViewPortNumber.getText().toString());
		int timeMultiple = Integer.parseInt(mTextViewTimeMultiple.getText().toString());
		int timeDelay = Integer.parseInt(mTextViewTimeDelay.getText().toString());
		int timeOpen = Integer.parseInt(mTextViewTimeOpen.getText().toString());
		
//		mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
//		mSelectedDevice.setIsRememberPassword(mIsRememberPassword.isChecked());
		mSelectedDevice.stop(new AsyncCallback(){
			@Override
			public void success(Device device, Object data) {
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": 停止指令已发送");
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(errorMsg instanceof Command.PasswordErrorException){
					mEditTextPassword.setError(device.getName() + ": " + errorMsg.getMessage());
				}
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
			}
			@Override
			public void progress(Device device, String progressMsg) {
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + progressMsg);
			}
			
		}, portNumber, timeMultiple, timeDelay, timeOpen);
		
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
			ToastUtils.showMessageLong(getApplicationContext(), "请先选择一个设备！");
			return;
		}
		if(mTextViewPortNumber.getText() == null || mTextViewPortNumber.getText().length() == 0){
			ToastUtils.showMessageLong(getApplicationContext(), "请输入端口号！");
			return;
		}
		int portNumber = Integer.parseInt(mTextViewPortNumber.getText().toString());
		int timeMultiple = Integer.parseInt(mTextViewTimeMultiple.getText().toString());
		int timeDelay = Integer.parseInt(mTextViewTimeDelay.getText().toString());
		int timeOpen = Integer.parseInt(mTextViewTimeOpen.getText().toString());
		
//		mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
		mSelectedDevice.close(new AsyncCallback(){
			@Override
			public void success(Device device, Object data) {
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": 关门指令已发送");
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(errorMsg instanceof Command.PasswordErrorException){
					mEditTextPassword.setError(errorMsg.getMessage());
				}
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
			}
			@Override
			public void progress(Device device, String progressMsg) {
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + progressMsg);
				
			}
			
		}, portNumber, timeMultiple, timeDelay, timeOpen);
		
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
			ToastUtils.showMessageLong(getApplicationContext(), "请先选择一个设备！");
			return;
		}
		
		if(mTextViewPortNumber.getText() == null || mTextViewPortNumber.getText().length() == 0){
			ToastUtils.showMessageLong(getApplicationContext(), "请输入端口号！");
			return;
		}
		int portNumber = Integer.parseInt(mTextViewPortNumber.getText().toString());
		int timeMultiple = Integer.parseInt(mTextViewTimeMultiple.getText().toString());
		int timeDelay = Integer.parseInt(mTextViewTimeDelay.getText().toString());
		int timeOpen = Integer.parseInt(mTextViewTimeOpen.getText().toString());
		
//		mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
		mSelectedDevice.open(new AsyncCallback(){
			@Override
			public void success(Device device, Object data) {
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": 开门指令已发送");
				mBtnOpen.setEnabled(true);
			}
			@Override
			public void progress(Device device, String msg){
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + msg);
			}
			@Override
			public void error(Device device, Exception errorMsg) {
				if(errorMsg instanceof Command.PasswordErrorException){
					mEditTextPassword.setError(errorMsg.getMessage());
				} 
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
			}
		}, portNumber, timeMultiple, timeDelay, timeOpen);
	}


	private void populatePariedDevices() {
    	mDevicesArray.clear();
		((ArrayAdapter)getListAdapter()).notifyDataSetChanged();

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		if(pairedDevices.size() > 0){
			for(BluetoothDevice btDevice : pairedDevices){
				mDevicesArray.add(createDoorDevcie(btDevice, mBluetoothAdapter));
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
				mDevicesArray.add(createDoorDevcie(device, mBluetoothAdapter));
				
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
                ToastUtils.showMessageLong(getApplicationContext(), "正在开启蓝牙...");
            }
        }
		
	}
	
	private AsyncCallback mDoorDeviceCallback = new AsyncCallback(){
		@Override
		public void success(Device device, Object response) {
			Command command = (Command)response;
			ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + device.getTypeName(command) + " 指令已送达");
		}
		
		@Override
		public void progress(Device device, String msg){
			ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + msg);
		}
		
		@Override
		public void error(Device device, Exception errorException) {
			if(errorException instanceof Command.PasswordErrorException){
				mEditTextPassword.setError(errorException.getMessage());
			} 
			ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorException.getMessage());
		}
	};

	protected DoorDevice createDoorDevcie(BluetoothDevice device,
			BluetoothAdapter mBluetoothAdapter) {
		DoorDevice dev = new DoorDevice(getApplicationContext(), device, mBluetoothAdapter);
		dev.setResponseCallback(mDoorDeviceCallback);
		return dev;
	}


	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		mSelectedDevice = mDevicesArray.get(position);
		mIsRememberPassword.setChecked(mSelectedDevice.getIsRememberPassword());
		if(mSelectedDevice.getIsRememberPassword()){
			mEditTextPassword.setText(mSelectedDevice.getPassword());
		} else {
			mEditTextPassword.setText("");
		}
		mEditTextPassword.setError(null);
		
        if (mBluetoothAdapter.isEnabled()) {
//    		mSelectedDevice.connect(new ConnectCallback(){
//				@Override
//				public void connectSuccess(Object device) {
//					mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
//					ToastUtils.showMessageLong(getApplicationContext(), "已成功连接到设备: " + device.getName());
//				}
//				@Override
//				public void connectError(Exception errorMsg) {
//					ToastUtils.showMessageLong(getApplicationContext(), errorMsg.getMessage());
//				}
//    		});
    	} else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        	mTextViewStatus.setText("正在连接到设备: " + mSelectedDevice.getName());
//			ToastUtils.showMessageLong(getApplicationContext(), "正在连接到设备: " + mSelectedDevice.getName());
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
	        	ToastUtils.showMessageLong(getApplicationContext(), "请开启蓝牙！");
	        }
    	} 
//    	else if(requestCode == REQUEST_CONNECT_DEVICE){
//	        if(resultCode == RESULT_OK) {
//	        	mSelectedDevice.connect(new AsyncCallback(){
//					@Override
//					public void success(Object device) {
//						mSelectedDevice.setPassword(mEditTextPassword.getText().toString());
//						if(mSelectedDevice.getCurrentCommand() != null){
//							ToastUtils.showMessageLong(getApplicationContext(), mSelectedDevice.getCurrentCommand().getTypeName() + "指令已发送到设备: " + device.getName());
//						} else {
//							mSelectedDevice.disconnect(null);
////							ToastUtils.showMessageLong(getApplicationContext(), "已成功连接到设备: " + device.getName());
//						}
//					}
//					@Override
//					public void error(Exception errorMsg) {
//						ToastUtils.showMessageLong(getApplicationContext(), errorMsg.getMessage());
//					}
//					@Override
//					public void progress(String progressMsg) {
//						ToastUtils.showMessageLong(getApplicationContext(), progressMsg);
//						
//					}
//	    			
//	    		});
//	        } else {
////	        	mTextViewStatus.setText("请开启蓝牙后再尝试连接！");
//				ToastUtils.showMessageLong(getApplicationContext(), "请开启蓝牙后再尝试连接！");
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
	public boolean onOptionsItemSelected(MenuItem item) {
		
		 if(item.getItemId() == R.id.action_changepassword){

			if(mSelectedDevice == null){
				ToastUtils.showMessageLong(getApplicationContext(), "请先选择一个设备！");
				return true;
			}
			
			 Intent intent = new Intent(this, ChangePasswordActivity.class);
			 intent.putExtra("DEVICE_ADDRESS", mSelectedDevice.getBtDevice().getAddress());
			 startActivity(intent);
			 return true;
		 } else if(item.getItemId() == R.id.action_devicename){

			if(mSelectedDevice == null){
				ToastUtils.showMessageLong(getApplicationContext(), "请先选择一个设备！");
				return true;
			}
			
			 Intent intent = new Intent(this, DeviceNameActivity.class);
			 intent.putExtra("DEVICE_ADDRESS", mSelectedDevice.getBtDevice().getAddress());
			 startActivity(intent);
			 return true;
		 }
		
		return super.onOptionsItemSelected(item);
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

