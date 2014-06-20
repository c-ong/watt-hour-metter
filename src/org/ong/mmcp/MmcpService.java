package org.ong.mmcp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import cn.hnsi.android.apps.metterreader.BuildConfig;
import cn.hnsi.android.apps.metterreader.R;

import org.ong.mmcp.impl.DefaultMmcpMasterStation;
import org.ong.mmcp.op.ResultGenerator;
import org.ong.mmcp.util.ByteUtils;

import java.util.List;
import java.util.Map;

public final class MmcpService {
	
	public interface OperationResultReceiver {
		/**
		 * 接收到设备响应的结果。
		 * @param which
		 */
		void onReceive(ResultGenerator generator);
	}
	
	// Based
	
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "MmcpService";
	
    // Message types sent from the MasterStation Handler
    public static final int MSG_STATE_CHANGE 	= 1;
    public static final int MSG_WRITE 		= 2;
    public static final int MSG_READ 			= 3;    
    public static final int MSG_DEVICE_NAME 	= 4;
    public static final int MSG_TOAST 		= 5;
    /*package*/ static final int MSG_OP_RESULT_RECEIVED = Integer.MAX_VALUE;
    
    static public final int REQUEST_ENABLE_BT = 0xBE;
	
	private static final Object LOCK = new Object();
	
	/* -------------------------------------------------------- */	
	
	private Context ctx;
	
	private MasterStationService masterStationService;
	
	private Handler receiver;
	
	private boolean listening = false;
	
	private OperationResultReceiver opResultreceiver;
	
	/* -------------------------------------------------------- */
	
	// which@bus
	
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if ( DEBUG ) Log.i( LOG_TAG, "--> handleMessage: " 
					+ MasterStationService.debugGetMsgWhatString( msg ) + " " + msg );
			
            switch ( msg.what ) {
            
            case MSG_STATE_CHANGE: {
            	
                if ( DEBUG ) Log.i( LOG_TAG, "MESSAGE_STATE_CHANGE: " 
                		+ MasterStationService.debugGetStateString( msg.arg1 ) );
                
                switch ( msg.arg1 ) {
                case MasterStationService.STATE_CONNECTED:
                	
                    break;
                    
                case MasterStationService.STATE_CONNECTING:
                	
                    break;
                    
                case MasterStationService.STATE_SCANNING: // Bus not associate
                	
                	break;
                	
                /*case BluetoothMasterStationService.STATE_LISTEN:*/
                case MasterStationService.STATE_NONE:
                	
                    break;
                }
            }
            
            	break;
            	
            case MSG_WRITE:
            	
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                Log.i( LOG_TAG, "SENT:  " + ByteUtils.bytesToHex( writeBuf ) );
                
                break;
                
            case MSG_READ:
            	
                byte[] readBuf = (byte[]) msg.obj;                
                // construct a string from the valid bytes in the buffer
                Log.i( LOG_TAG, "Received: " + ByteUtils.bytesToHex( readBuf ) );
                
                break;
                
            case MSG_DEVICE_NAME:
            	
                // save the connected device's name
                String connectedDeviceName = msg.getData().getString( 
                		MasterStationService.DEVICE_NAME );
                Toast.makeText( ctx, "Connected to "
                               + connectedDeviceName, Toast.LENGTH_SHORT ).show();
                
                break;
                
            case MSG_TOAST:
            	
                Toast.makeText( ctx, msg.getData().getString( MasterStationService.TOAST ),
                               Toast.LENGTH_SHORT ).show();
                
                break;
                
            case MSG_OP_RESULT_RECEIVED:
            	
            	Operation op = (Operation) msg.obj;
            	// TODO Cloning the OP
            	if ( null != opResultreceiver ) 
            		opResultreceiver.onReceive( (ResultGenerator) op );
            	
            	break;
            }
		}
	};	
	
	private void dispatchMessageIfNeeded(Message msg) {
				
	}
	
	private boolean isBusMsg(Message Msg) {
		
		
		return false;
	}
	
	static public void clearAll() {
		throw new RuntimeException( "Not implemented!" );
	}
	
//	static public long enqueue(Operation operation) throws MmcpException {
//		return 0L;
//	}
	
	// TODO
	static public void registerAdapter() {
		throw new RuntimeException( "Not implemented!" );
	}
	
	static public void setReceiver(OperationResultReceiver opResultReceiver) {
		singleton.opResultreceiver = opResultReceiver;
	}
	
	static public void listen() {
		if ( singleton.listening ) {
			
		} else {
			singleton.listening = true;
			
			singleton.masterStationService.listen();
		}
	}
	
	static public boolean requestCancelListen() {
		throw new RuntimeException( "Not implemented!" );
		///return singleton.listening;
	}
	
	static public Bus registerBus(String bluetoothUUID) {
		check();
		
		if ( singleton.listening ) {
			throw new RuntimeException( 
					"The service already entered listening mode, so unable to " 
					+ "register Bus again." );
		}
		
		MmcpBluetoothMasterStation master = (MmcpBluetoothMasterStation) singleton
				.masterStationService.masterStation();
		
		return master.registerBus( bluetoothUUID );
	}
	
	static public void unregisterBus(String bluetoothUUID) {
		check();
		
		if ( singleton.listening ) {
			throw new RuntimeException( 
					"The service already entered listening mode, so unable to unregister Bus." );
		}
		
		MmcpBluetoothMasterStation master = (MmcpBluetoothMasterStation) singleton
				.masterStationService.masterStation();
		
		master.unregisterBus( bluetoothUUID );
	}
	
	static public MasterStation getMasterStation() {
		check();
		
		return singleton.masterStationService.masterStation();
	}
	
	static public void start(Context context) {
		singleton.ctx = context;
		
		singleton.internalStart();		
	}
	
	static public void stop() {
		check();
		
		singleton.internalStop();
	}	
	
	static public boolean isStarted() {
		return null != singleton && null != singleton.masterStationService;
	}
	
	static public boolean checkDevice() {
		throw new RuntimeException( "Not implemented!" );
		
		///return false;
	}
	
	static public boolean checkManifest() {
		throw new RuntimeException( "Not implemented!" );
		///return false;
	}
	
	/* -------------------------------------------------------- */
	
	private void internalStart() {
		ensuerBluetooth();
		
		setupMasterStation();
	}
	
	private void internalStop() {
		disposeMasterStation();
	}
	
	/* -------------------------------------------------------- */
	
	private void deliverMsgToReceiver() {
		
	}
	
	private void deliverResultToReceiver() {
		deliverMsgToReceiver();
	}
	
	private void setupMasterStation() {
		// 将 MasterStation 放入 MmcpBluetoothMasterStationService 更为合适
		synchronized ( LOCK ) {
			if ( null == masterStationService ) {
				masterStationService = new MasterStationService( 
						singleton.ctx, new IncomingHandler() );	
				
				masterStationService.start();
			}
		}
	}
	
	private void disposeMasterStation() {
		synchronized ( LOCK ) {
			if ( null != masterStationService ) {
				masterStationService.stop();
			}
		}
	}
	
	private static void check() {
		if ( isStarted() ) {
			
		} else { 
			throw new RuntimeException( 
				"You may not call MmcpService.start() method to starting the Service." );
		}
	}
	
	private void ensuerBluetooth() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if ( null == bluetoothAdapter ) {
			Toast.makeText( ctx, "Device does not support Bluetooth", Toast.LENGTH_LONG )
				.show();
		} else if ( ! bluetoothAdapter.isEnabled() ) {
			Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
			
			Activity host = (Activity) ctx;
		    host.startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT  );
		} 	
	}
	
	private MmcpService() {}
	
	// XXX 
	private static MmcpService singleton = new MmcpService();
}