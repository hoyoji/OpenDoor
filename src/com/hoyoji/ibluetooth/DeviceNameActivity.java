package com.hoyoji.ibluetooth;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class DeviceNameActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Device mDevice;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_name);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		final EditText editOldPassword = (EditText) findViewById(R.id.edit_old_password);
		final EditText editDeviceName = (EditText) findViewById(R.id.edit_device_name);
		
		Intent intent = getIntent();
		String address = intent.getStringExtra("DEVICE_ADDRESS");
		BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(address);
		mDevice = new Device(this, btDevice, mBluetoothAdapter);
//		mDevice.setResponseCallback(new AsyncCallback(){
//			@Override
//			public void success(Device device, Object response) {
//				Command command = (Command)response;
//				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + device.getTypeName(command) + " 指令已送达");
//			}
//			
//			@Override
//			public void progress(Device device, String msg){
//				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + msg);
//			}
//			
//			@Override
//			public void error(Device device, Exception errorException) {
//				if(errorException instanceof Command.PasswordErrorException){
//					editOldPassword.setError(errorException.getMessage());
//				} 
//				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorException.getMessage());
//			}
//		});
		
		editDeviceName.setText(mDevice.getName());
		
		findViewById(R.id.btn_set_name).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				boolean hasError = false;
				editOldPassword.setError(null);
				
				if(editOldPassword.getText().length() == 0){
					ToastUtils.showMessageLong(DeviceNameActivity.this, editOldPassword.getHint().toString());
					editOldPassword.setError(editOldPassword.getHint());
					hasError = true;
				} 
				if(editDeviceName.getText().length() == 0){
					ToastUtils.showMessageLong(DeviceNameActivity.this, editDeviceName.getHint().toString());
					hasError = true;
				}
				if(hasError){
					return;
				}
				
				mDevice.setName(editDeviceName.getText().toString());
				ToastUtils.showMessageLong(DeviceNameActivity.this, "设备名称已修改");
				
//				mDevice.setPassword(editOldPassword.getText().toString());
//				try {
//					mDevice.changePassword(editNewPassword.getText().toString().getBytes(), new AsyncCallback(){
//						@Override
//						public void success(Device device, Object data) {
//							ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": 修改密码指令已发送");
//						}
//						@Override
//						public void progress(Device device, String msg){
//							ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + msg);
//						}
//						@Override
//						public void error(Device device, Exception errorMsg) {
//							if(errorMsg instanceof Command.PasswordErrorException){
//								editOldPassword.setError(errorMsg.getMessage());
//							} 
//							ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
//						}
//					});
//				} catch (Exception e) {
//					ToastUtils.showMessageLong(getApplicationContext(), mDevice.getName() + ": " + e.getMessage());
//					editNewPassword.setError(e.getMessage());
//				}
			}
			
		});
		
		
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (item.getItemId() == android.R.id.home) {
        	finish();
        	return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }


}
