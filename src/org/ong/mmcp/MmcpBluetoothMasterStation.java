package org.ong.mmcp;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import cn.hnsi.android.apps.metterreader.DeviceListActivity;

import java.util.HashMap;

public abstract class MmcpBluetoothMasterStation extends MmcpMasterStation {
	
	@Override
	public BluetoothBus current() {
		return (BluetoothBus) super.current();	
	}
	
	/* -------------------------------------------------------- */
	
	/*package*/ Bus registerBus(String bluetoothUUID) {
		// TODO Verify the BluetoothUUID
		
		Bus result = null;
		
		if ( buses.containsKey( bluetoothUUID ) ) {
			result = buses.get( bluetoothUUID );
		} else {
			Bus bus = new BluetoothBus( bluetoothUUID, this );			
			result = buses.put( bluetoothUUID, bus );
			
			if ( null == result ) {
				// New device
				result = bus;
			} else {
				// That's insignificance operation
			}
		}
		
		return result;
	}
	
	/*package*/ void unregisterBus(String bluetoothUUID) {
		buses.remove( bluetoothUUID );
	}
	
	private void onBusFound(BluetoothDevice bus) {
		futureBuses.add( bus.getAddress() );
		notifyDeviceFound();
	}
	
	/*package*/ boolean isBusDevice(BluetoothDevice device) {
		return buses.containsKey( device.getAddress() );
	}
	
	/*package*/ void onDeviceFound(BluetoothDevice device) {
		if ( isBusDevice( device ) ) {
			onBusFound( device );
		}
	}
	
	/*package*/ MmcpBluetoothMasterStation() {
		super();
	}
}