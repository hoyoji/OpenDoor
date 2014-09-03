package com.hoyoji.opendoor;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DevicesActivity extends ListActivity {
	private ArrayList<String> mDevicesTitleArray = new ArrayList<String>();
	private ArrayList<BluetoothDevice> mDevicesArray = new ArrayList<BluetoothDevice>();
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	TextView mTextViewEmpty;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_devices);
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mDevicesTitleArray);
        setListAdapter(adapter);
        
        mTextViewEmpty = (TextView)findViewById(android.R.id.empty);
        mTextViewEmpty.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				discoverDevices();
			}
		});
        
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	this.registerReceiver(btReceiver, filter);
    }

	private final BroadcastReceiver btReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				mDevicesTitleArray.add(device.getName());	
				mDevicesArray.add(device);
				
				((ArrayAdapter)getListAdapter()).notifyDataSetChanged();
			}
			
		}
		
	};
	
	
	protected void discoverDevices() {
		mTextViewEmpty.setText("正在查找设备...");
		
		if(!mBluetoothAdapter.isDiscovering()){
			mBluetoothAdapter.startDiscovery();
		}
		
	}

	
	
	@Override  
    public void onListItemClick(ListView l, View v, int position, long id) { 
		if(this.getCallingActivity() != null){
			Intent intent = new Intent();
			intent.putExtra("DEVICE_NAME", ((TextView)v).getText());
			intent.putExtra("DEVICE", mDevicesArray.get(position));
			this.setResult(Activity.RESULT_OK, intent);
			this.finish();
		} else {
		}
    }  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.devices, menu);
		return true;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		mBluetoothAdapter.cancelDiscovery();
		unregisterReceiver(btReceiver);
	}
}
