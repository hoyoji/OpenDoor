package com.example.opendoor;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView mTextViewState;
	private Button mBtnConnect;
	private Button mBtnStop;
	private Button mBtnClose;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTextViewState = (TextView) findViewById(R.id.mainTextViewState);
		mBtnConnect = (Button) findViewById(R.id.mainBtnConnect);
		mBtnStop = (Button) findViewById(R.id.mainBtnStop);
		mBtnClose = (Button) findViewById(R.id.mainBtnClose);
		
		mBtnConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "hello", Toast.LENGTH_LONG).show();
				
			}
		});
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
