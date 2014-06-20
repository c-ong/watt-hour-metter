package org.ong.mmcp;

import android.bluetooth.BluetoothDevice;

public abstract class DeviceScanner {
	/*package*/ interface Listener {
		void onDiscoveryFinished();
		
		void onPairedFound(BluetoothDevice device);
		
		void onFound(BluetoothDevice device);
	}
	
	protected abstract void start();
	
	protected abstract void listen();
	
	protected abstract void stop();
	
	protected abstract void dispose();
}