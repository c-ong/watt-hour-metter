package org.ong.mmcp;

import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;

import java.util.Set;

public class BluetoothBus extends Bus {
	
	private static final boolean DEBUG = BuildConfig.DEBUG;

	private static final String LOG_TAG = "BluetoothBus";

	public final String address;
	
	/*package*/ String pin; 
	
	public BluetoothBus(String address, MasterStation master) {
		super( master );
		
		this.address = address;		
	}
	
	@Override
	protected void run() {
		connect();
	}

	@Override
	protected void shutdown() {
		disconnect();
	}
	
	public void debugSetPIN(String pin) {
		this.pin = pin;
	}
	
	public String debugGetPIN() {
		return pin;
	}

	protected void connect() {
		if ( DEBUG ) Log.i( LOG_TAG, "Attempt to connecting... "); 
		masterStation.getBusesManageProxy().run( this, true );
	}

	protected void disconnect() {
	}

	@Override
	protected void onStateChanaged(State previous, State now) {
		switch ( now ) {
		case CONNECTED:
			
			onConnected();
						
			break;
		}
	}
	
	private void onConnected() {
		if ( DEBUG ) Log.i( LOG_TAG, "++ onConnected ++" );
		
		pushExecutionPlansToQueue();
	}
}