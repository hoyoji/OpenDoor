package com.hoyoji.ibluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
	private AsyncCallback mCallbackResponse;
	private boolean mClosing;
	private static Handler handler = new Handler(Looper.getMainLooper());

    public ConnectedThread(BluetoothSocket socket) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams, using temp objects because
        // member streams are final
    	mClosing = false;
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {
        final byte[] buffer = new byte[256];  // buffer store for the stream
        int bytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs
        while (mmSocket.isConnected()) {
            try {
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                // Send the obtained bytes to the UI activity
//                mHandler.obtainMessage(2, bytes, -1, new String(buffer)).sendToTarget();
                if(mCallbackResponse != null){
					handler.post(new Runnable() {
						public void run() {
		                	mCallbackResponse.success(null, buffer);
		                }
		            });
                }
            } catch (final IOException e) {
	                if(mCallbackResponse != null){
						handler.post(new Runnable() {
							public void run() {
				            	if(!mClosing){
				                	mClosing = false;
				                	mCallbackResponse.error(null, e);
				            	}
			                }
			            });
	                }
                break;
            }
        }
    }

    public boolean isConnected(){
    	return mmSocket.isConnected();
    }
    
    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) throws IOException {
            mmOutStream.write(bytes);
    }
    
    public void setResponseCallback(AsyncCallback callback){
    	mCallbackResponse = callback;
    }

    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
        	mClosing = true;
        	if(mmInStream != null){
        		mmInStream.close();
        	}
        	if(mmOutStream != null){
        		mmOutStream.close();
        	}
        		
    		if(mmSocket != null){
    			mmSocket.close();
    		}
        } catch (IOException e) { 
    	}
    }

}
