package com.hoyoji.ibluetooth;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class ChangePasswordActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Device mDevice;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_password);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		

		final EditText editOldPassword = (EditText) findViewById(R.id.edit_old_password);
		final EditText editNewPassword = (EditText) findViewById(R.id.edit_new_password);
		final EditText editConfirmPassword = (EditText) findViewById(R.id.edit_confirm_password);
		
		Intent intent = getIntent();
		String address = intent.getStringExtra("DEVICE_ADDRESS");
		BluetoothDevice btDevice = mBluetoothAdapter.getRemoteDevice(address);
		mDevice = new Device(this, btDevice, mBluetoothAdapter);
		mDevice.setResponseCallback(new AsyncCallback(){
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
					editOldPassword.setError(errorException.getMessage());
				} 
				ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorException.getMessage());
			}
		});

		
		findViewById(R.id.btn_change_password).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				boolean hasError = false;
				editOldPassword.setError(null);
				editNewPassword.setError(null);
				editConfirmPassword.setError(null);
				
				if(editOldPassword.getText().length() == 0){
					ToastUtils.showMessageLong(ChangePasswordActivity.this, editOldPassword.getHint().toString());
					editOldPassword.setError(editOldPassword.getHint());
					hasError = true;
				} 
				if(editNewPassword.getText().length() == 0){
					ToastUtils.showMessageLong(ChangePasswordActivity.this, editNewPassword.getHint().toString());
					editNewPassword.setError(editNewPassword.getHint());
					hasError = true;
				}
				if(editConfirmPassword.getText().length() == 0){
					ToastUtils.showMessageLong(ChangePasswordActivity.this, editConfirmPassword.getHint().toString());
					editConfirmPassword.setError(editConfirmPassword.getHint());
					hasError = true;
				}
				if(hasError){
					return;
				}
				mDevice.setPassword(editOldPassword.getText().toString());
				try {
					mDevice.changePassword(editNewPassword.getText().toString().getBytes(), new AsyncCallback(){
						@Override
						public void success(Device device, Object data) {
							ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": 修改密码指令已发送");
						}
						@Override
						public void progress(Device device, String msg){
							ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + msg);
						}
						@Override
						public void error(Device device, Exception errorMsg) {
							if(errorMsg instanceof Command.PasswordErrorException){
								editOldPassword.setError(errorMsg.getMessage());
							} 
							ToastUtils.showMessageLong(getApplicationContext(), device.getName() + ": " + errorMsg.getMessage());
						}
					});
				} catch (Exception e) {
					ToastUtils.showMessageLong(getApplicationContext(), mDevice.getName() + ": " + e.getMessage());
					editNewPassword.setError(e.getMessage());
				}
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
