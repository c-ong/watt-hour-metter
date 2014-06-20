package org.ong.mmcp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;

import java.util.HashSet;
import java.util.Set;

public class BusScanner extends DeviceScanner {
	
	private class Receiver extends BroadcastReceiver {
		
		// When a device is lost	

		@Override
		public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // When discovery finds a device
            if ( BluetoothDevice.ACTION_FOUND.equals( action ) ) {
            	
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra( 
                		BluetoothDevice.EXTRA_DEVICE );
                
                if ( newDevices.contains( device.getAddress() ) ) {
                	
                } else {
                	listener.onFound( device );
                }
                
                // If it's already paired, skip it, because it's been listed already
                if ( BluetoothDevice.BOND_BONDED != device.getBondState() ) {
                    newDevices.add( device.getAddress() );
                }
                
            // When discovery is finished, change the Activity title
            } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
            	listener.onDiscoveryFinished();
            	
                if ( newDevices.size() == 0 ) {
                	                	
                }
            }
		}
	}

	private static final boolean DEBUG = BuildConfig.DEBUG;

	private static final String LOG_TAG = "BusScanner";
	
	private Context ctx;
	private Receiver reciver;
	
	private Listener listener;
	
	private BluetoothAdapter bluetoothAdapter;
	
    private Set<String> pairedDevices;
    private Set<String> newDevices;
	
	/*package*/ BusScanner(Context context, Listener listener) {
		ctx = context;		
		this.listener = listener;
		
		init();
	}
	
	/// Wait until listening

	@Override
	protected void start() {		
		// If we're already discovering, stop it
        if ( bluetoothAdapter.isDiscovering() ) {
        	bluetoothAdapter.cancelDiscovery();
        }
	}

	@Override
	protected void listen() {
		if ( BuildConfig.DEBUG ) Log.i( "BusScanner", "++ Starting the Bus scanner..." );
		
		ensureReceiver();
		
		settlePairedDevices();
		
		// Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
        ctx.registerReceiver( reciver, filter );

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
        ctx.registerReceiver( reciver, filter );
        
		 // If we're already discovering, stop it
        if ( bluetoothAdapter.isDiscovering() ) {
        	if ( DEBUG ) Log.i( LOG_TAG, "HO, the local Bluetooth adapter is " 
        			+ "currently in the device discovery process." );
//        	bluetoothAdapter.cancelDiscovery();
        } else {
        	// Request discover from BluetoothAdapter
            bluetoothAdapter.startDiscovery();
        }
	}
	
	public void requestSuspend(boolean suspend) {
		if ( suspend ) {
			if ( bluetoothAdapter.isDiscovering() ) 
				bluetoothAdapter.cancelDiscovery();
		} else {
			if ( bluetoothAdapter.isDiscovering() ) { 
//				bluetoothAdapter.cancelDiscovery();
			} else {
				bluetoothAdapter.startDiscovery();
			}
		}
	}
	
	private void settlePairedDevices() {
		if ( pairedDevices.isEmpty() ) {
			
		} else {
			for ( String addr : pairedDevices ) {
				BluetoothDevice device = bluetoothAdapter.getRemoteDevice( addr );
				listener.onPairedFound( device );
			}			
		}
	}
	
	/*package*/ void cancelListen() {
		if ( null != reciver ) {
			 // Unregister broadcast listeners
	        ctx.unregisterReceiver( reciver );
	        reciver = null;
		}
		
		if ( null != pairedDevices && ! pairedDevices.isEmpty() ) {
			pairedDevices.clear();
		}
		
		if ( null != newDevices && ! newDevices.isEmpty() ) {
			newDevices.clear();
		}
	}	

	@Override
	protected void stop() {
        // Cancel discovery because it's costly and we're about to connect
		if ( null != bluetoothAdapter ) {
			bluetoothAdapter.cancelDiscovery();			
		}
	}

	@Override
	protected void dispose() {
		// Make sure we're not doing discovery anymore
        if ( null != bluetoothAdapter ) {
            bluetoothAdapter.cancelDiscovery();
        }		
	}
	
//	android.bluetooth.a2dp.action.SINK_STATE_CHANGED
//	android.bluetooth.adapter.action.DISCOVERY_FINISHED
//	android.bluetooth.adapter.action.DISCOVERY_STARTED
//	android.bluetooth.adapter.action.LOCAL_NAME_CHANGED
//	android.bluetooth.adapter.action.SCAN_MODE_CHANGED
//	android.bluetooth.adapter.action.STATE_CHANGED
//	android.bluetooth.device.action.ACL_CONNECTED
//	android.bluetooth.device.action.ACL_DISCONNECTED
//	android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED
//	android.bluetooth.device.action.BOND_STATE_CHANGED
//	android.bluetooth.device.action.CLASS_CHANGED
//	android.bluetooth.device.action.FOUND
//	android.bluetooth.device.action.NAME_CHANGED
//	android.bluetooth.devicepicker.action.DEVICE_SELECTED
//	android.bluetooth.devicepicker.action.LAUNCH
//	android.bluetooth.headset.action.AUDIO_STATE_CHANGED
//	android.bluetooth.headset.action.STATE_CHANGED
	
	/* -------------------------------------------------------- */
	
	private void ensureReceiver() {
		if ( null == reciver ) {
			reciver = new Receiver();
		}
	}
	
	private void init() {
        // Initialize list. One for already paired devices and
        // one for newly discovered devices
		pairedDevices = new HashSet<String>();
		newDevices = new HashSet<String>();
		
		// Get the local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // Get a set of currently paired devices
        Set<BluetoothDevice> paired = bluetoothAdapter.getBondedDevices();
        
        // If there are paired devices, add each one to the List
        if ( paired.isEmpty() ) {

        } else {
        	for ( BluetoothDevice device : paired ) {
        		if ( DEBUG ) Log.i( LOG_TAG, "Found an bonded device " + device.getAddress() ); 
        		pairedDevices.add( device.getAddress() );
        		// TODO 如果不取消 Bonded 状态则下次可能无法检测到此类设备
        	}
        }
	}
}