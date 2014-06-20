/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.hnsi.android.apps.metterreader;

//import com.android.settings.R;

import org.ong.mmcp.BluetoothBus;
import org.ong.mmcp.MmcpBluetoothMasterStation;
import org.ong.mmcp.MmcpService;
import org.ong.mmcp.util.BluetoothExpert;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

/**
 * BluetoothPairingRequest is a receiver for any Bluetooth pairing request. It
 * checks if the Bluetooth Settings is currently visible and brings up the PIN, the passkey or a
 * confirmation entry dialog. Otherwise it puts a Notification in the status bar, which can
 * be clicked to bring up the Pairing entry dialog.
 */
public class BluetoothPairingRequest extends BroadcastReceiver {
	
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "BluetoothPairingRequest";
	
	 /** The user will be prompted to confirm the passkey displayed on the screen
     * @hide */
    public static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    /** The user will be prompted to enter the passkey displayed on remote device
     * @hide */
    public static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
	
    /** @hide */
    public static final String EXTRA_REASON = "android.bluetooth.device.extra.REASON";
    /** @hide */
    public static final String EXTRA_PAIRING_VARIANT =
            "android.bluetooth.device.extra.PAIRING_VARIANT";
    /** @hide */
    public static final String EXTRA_PASSKEY = "android.bluetooth.device.extra.PASSKEY";

    public static final int NOTIFICATION_ID = android.R.drawable.stat_sys_data_bluetooth;
    
    /**
     * Broadcast Action: Indicates a failure to retrieve the name of a remote
     * device.
     * <p>Always contains the extra field {@link #EXTRA_DEVICE}.
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} to receive.
     * @hide
     */
    // is this actually useful?
    public static final String ACTION_NAME_FAILED =
            "android.bluetooth.device.action.NAME_FAILED";

    public static final String ACTION_PAIRING_REQUEST =
            "android.bluetooth.device.action.PAIRING_REQUEST";
    public static final String ACTION_PAIRING_CANCEL =
            "android.bluetooth.device.action.PAIRING_CANCEL";

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i( "BluetoothPairingRequest", "## I'm working..." );
    	
        final String action = intent.getAction();
        
        if ( action.equals( ACTION_PAIRING_REQUEST ) ) {

//          LocalBluetoothManager localManager = LocalBluetoothManager.getInstance( context );

            BluetoothDevice device =
                    intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
            
            String deviceAddress = device != null 
            		? device.getAddress() 
            		: null;
            		
            
            MmcpBluetoothMasterStation master = (MmcpBluetoothMasterStation) MmcpService
            		.getMasterStation();
            BluetoothBus bus = master.current();
            			
            		
            String pin = bus.debugGetPIN();
            		
            Log.i( "BluetoothPairingRequest", "## Device addr: " + deviceAddress );
            if ( DEBUG && bus.address.equals( deviceAddress ) ) {
            	Log.i( LOG_TAG, "--> the PIN is: " + pin );
            }
            
            /**
             * Indicates the remote device is not bonded (paired).
             * <p>There is no shared link key with the remote device, so communication
             * (if it is allowed at all) will be unauthenticated and unencrypted.
             */
            //public static final int BOND_NONE = 10;
            /**
             * Indicates bonding (pairing) is in progress with the remote device.
             */
            //public static final int BOND_BONDING = 11;
            /**
             * Indicates the remote device is bonded (paired).
             * <p>A shared link keys exists locally for the remote device, so
             * communication can be authenticated and encrypted.
             * <p><i>Being bonded (paired) with a remote device does not necessarily
             * mean the device is currently connected. It just means that the ponding
             * procedure was compeleted at some earlier time, and the link key is still
             * stored locally, ready to use on the next connection.
             * </i>
             */
            //public static final int BOND_BONDED = 12;
            Log.i( "BluetoothPairingRequest", "## BondState: " + device.getBondState() );
            if ( BluetoothDevice.BOND_BONDED != device.getBondState() ) {
            
	        	try {
	//        		BluetoothExpert.setPairingConfirmation( device, false );
	
	        		// 在 Level 16 该项操作仅限 Firmware 中的 App 可使用
	        		BluetoothExpert.setPin( device, pin );
	        		BluetoothExpert.cancelPairingUserInput( device );
	        		
	            	BluetoothExpert.createBond( device );
	            	
	            	// 在不用时需要 removeBonded
	            	return;
				} catch (Exception e) {
					e.printStackTrace();
				} 
            }
            
            int type = intent.getIntExtra( EXTRA_PAIRING_VARIANT,
                    BluetoothDevice.ERROR );
            
            Intent pairingIntent = new Intent();
            
//          pairingIntent.setClass( context, BluetoothPairingDialog.class );
            pairingIntent.putExtra( BluetoothDevice.EXTRA_DEVICE, device );
            pairingIntent.putExtra( EXTRA_PAIRING_VARIANT, type );
            
            if ( type == PAIRING_VARIANT_PASSKEY_CONFIRMATION 
            		|| type == PAIRING_VARIANT_DISPLAY_PASSKEY ) {
            	
                int passkey = intent.getIntExtra( EXTRA_PASSKEY, 
                		BluetoothDevice.ERROR );
                
                pairingIntent.putExtra( EXTRA_PASSKEY, passkey );
            }
            
//          pairingIntent.setAction( ACTION_PAIRING_REQUEST );
            pairingIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            
            if ( false ) {
//          if (localManager.shouldShowDialogInForeground(deviceAddress)) {
                // Since the BT-related activity is in the foreground, just open the dialog
                context.startActivity(pairingIntent);

            } else {
            	if ( device.getBondState() != BluetoothDevice.BOND_BONDED ) {
//            		BluetoothExpert.setPin(device, value);
//            		BluetoothExpert.createBond(device);
            	} else {
//            		BluetoothExpert.createBond(device);
//            		BluetoothExpert.setPin(device, value);
//            		BluetoothExpert.createBond(device);
            	}
            	
            	try {
					BluetoothExpert.setPin( device, pin );
	            	BluetoothExpert.createBond( device );
	            	BluetoothExpert.cancelPairingUserInput( device );
				} catch (Exception e) {
					e.printStackTrace();
				} 
            	
//                // Put up a notification that leads to the dialog
//                Resources res = context.getResources();
//                Notification notification = new Notification(
//                        android.R.drawable.stat_sys_data_bluetooth,
//                        res.getString(R.string.bluetooth_notif_ticker),
//                        System.currentTimeMillis());
//
//                PendingIntent pending = PendingIntent.getActivity(context, 0,
//                        null/*pairingIntent*/, PendingIntent.FLAG_ONE_SHOT);
//
//                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
//                if (TextUtils.isEmpty(name)) {
//                    name = device.getName();
//                }
//
//                notification.setLatestEventInfo(context,
//                        res.getString(R.string.bluetooth_notif_title),
//                        res.getString(R.string.bluetooth_notif_message) + name,
//                        pending);
//                notification.flags |= Notification.FLAG_AUTO_CANCEL;
//
//                NotificationManager manager = (NotificationManager)
//                        context.getSystemService(Context.NOTIFICATION_SERVICE);
//                manager.notify(NOTIFICATION_ID, notification);
            }

        } else if ( action.equals( ACTION_PAIRING_CANCEL ) ) {

            // Remove the notification
            NotificationManager manager = (NotificationManager) context
                    .getSystemService( Context.NOTIFICATION_SERVICE );
            manager.cancel( NOTIFICATION_ID );
        }
    }
}