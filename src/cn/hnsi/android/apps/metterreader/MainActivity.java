package cn.hnsi.android.apps.metterreader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import org.ong.mmcp.BluetoothBus;
import org.ong.mmcp.Bus;
import org.ong.mmcp.MasterStationService;
import org.ong.mmcp.MmcpException;
import org.ong.mmcp.MmcpMasterStation;
import org.ong.mmcp.MmcpService;
import org.ong.mmcp.OperationFactory;
import org.ong.mmcp.SlaveStation;
import org.ong.mmcp.impl.DefaultMmcpMasterStation;
import org.ong.mmcp.io.BusTransmitterAgent;
import org.ong.mmcp.op.PowerEnergyReadOperatoin;
import org.ong.mmcp.op.PowerEnergyReadOperatoin.Result;
import org.ong.mmcp.op.ResultGenerator;
import org.ong.mmcp.protocl.di.Classification;
import org.ong.mmcp.protocl.di.PowerDirection;
import org.ong.mmcp.protocl.di.Tariff;
import org.ong.mmcp.protocl.di.TimeDomain;
import org.ong.mmcp.util.ByteUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import cn.hnsi.android.apps.metterreader.R;

public class MainActivity extends Activity implements View.OnClickListener {
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 0xBE;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME 	= "device_name";
    public static final String TOAST 		= "toast";

	private static final String LOG_TAG = "MainActivity";

	private static final boolean DEBUG = BuildConfig.DEBUG;
    
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	private MasterStationService masterStationService;
	private MmcpMasterStation masterStatoin;
	
	private Button action;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate( savedInstanceState );	
		
		if ( DEBUG ) Log.e(LOG_TAG, "+++ ON CREATE +++");
		
        // Setup the window
        requestWindowFeature( Window.FEATURE_INDETERMINATE_PROGRESS );
        
		setContentView( R.layout.activity_main );
		
        // Set result CANCELED in case the user backs out
		setResult( RESULT_CANCELED );
		
		ensuerBluetooth();	
		
		if ( null == bluetoothAdapter ) {
	        Toast.makeText( this, "Bluetooth is not available", Toast.LENGTH_LONG ).show();
	        
	        finish();
	        
	        return;
		}
        
        action = (Button) findViewById( R.id.button_1 );
        action.setOnClickListener( this );
        
        findViewById( R.id.button_2 ).setOnClickListener( this );
        findViewById( R.id.button_demo ).setOnClickListener( this );
        findViewById( R.id.button_scan ).setOnClickListener( this );
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if(DEBUG) Log.e(LOG_TAG, "++ ON START ++");
		
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if ( ! bluetoothAdapter.isEnabled() ) {
            Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
            startActivityForResult( enableIntent, REQUEST_ENABLE_BT );
            
        // Otherwise, setup the Conversation display
        } else {
        	if ( masterStationService == null ) setUpConversationDisplay();
        }		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if ( DEBUG ) Log.e( LOG_TAG, "+ ON RESUME +" );
		
//		if ( null != masterStationService ) {
//			if ( MasterStationService.STATE_NONE == masterStationService.getState() )
//				masterStationService.start();
//		}	
		
//        if ( ! pairedDevicesArrayAdapter.isEmpty() ) {
//        	String defaultDevice = "98:D3:31:01:01:0E";
//
//        	String device = null;
//        	String address = null;
//        	for ( int i = 0 ; i < mPairedDevicesArrayAdapter.getCount(); i++ ) {
//        		device = mPairedDevicesArrayAdapter.getItem( i );
//        		address = device.substring( device.length() - 17 );
//        		
//        		System.out.println( "## Paired Device: " + device );
//        		
//        		if ( defaultDevice.equals( address ) ) {
//        			Message msg = handler.obtainMessage();
//        			msg.what = 99;
//        			msg.obj = address;
//        			msg.sendToTarget();
////        			connectDevice( address, true );
//        			
//        			return;
//        		}
//        	}        	
//        } 		
	}
	
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(DEBUG) Log.e(LOG_TAG, "- ON PAUSE -");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(DEBUG) Log.d(LOG_TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if ( resultCode == Activity.RESULT_OK ) {
                connectDevice( data, true );
            }
            
            break;
            
        case REQUEST_CONNECT_DEVICE_INSECURE:
        	
            // When DeviceListActivity returns with a device to connect
            if ( resultCode == Activity.RESULT_OK ) {
                connectDevice( data, false );
            }
            break;
            
        case REQUEST_ENABLE_BT:
        	
            // When the request to enable Bluetooth returns
            if ( resultCode == Activity.RESULT_OK ) {
                // Bluetooth is now enabled, so set up a chat session
            	setUpConversationDisplay();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(LOG_TAG, "Bluetooth not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if ( null != masterStationService ) masterStationService.stop();
		if(DEBUG) Log.e(LOG_TAG, "--- ON DESTROY ---");
	}
	
    @Override
    public void onStop() {
        super.onStop();
        if(DEBUG) Log.e(LOG_TAG, "-- ON STOP --");
    }
    
	@Override
	public void onClick(View v) {
		switch ( v.getId() ) {
		
		case R.id.button_demo:
			
			getStarted();
			
			break;
		
		case R.id.button_1:
			
			if ( null == masterStationService ) return;
			
			switch ( masterStationService.getState() ) {
			case MasterStationService.STATE_SCANNING:
				
				action.setEnabled( false );
				action.setText( "SCANNING..." );
	            // Launch the DeviceListActivity to see devices and do scan
				Intent serverIntent = new Intent( this, DeviceListActivity.class );
	            startActivityForResult( serverIntent, REQUEST_CONNECT_DEVICE_SECURE );
	            
				break;
				
			case MasterStationService.STATE_CONNECTED:
				
				doTest();
				
				break;
			}
			
			break;
			
		case R.id.button_2:
			
			attemptPairding();
			
			break;
			
		case R.id.button_scan:
			
            // Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent = new Intent( this, DeviceListActivity.class );
			startActivity( serverIntent );
			
			break;
		}		
	}
	
	private void sendMessage(byte []send) {
        // Check that we're actually connected before trying anything
        if ( masterStationService.getState() != MasterStationService.STATE_CONNECTED) {
            Toast.makeText( this, R.string.not_connected, Toast.LENGTH_SHORT ).show();
            return;
        }
		
		masterStationService.write( send );
	}
    
    private void ensureDiscoverable() {
        if(DEBUG) Log.d(LOG_TAG, "ensure discoverable");
        if (bluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
	
    private void connectDevice(BluetoothDevice device, boolean secure) {
        // Get the device MAC address        
        // Get the BluetoothDevice object
        // Attempt to connect to the device
        masterStationService.connect( device, secure );
        
		action.setEnabled( true );
		action.setText( "DO TESTING..." );
    }
    
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString( DeviceListActivity.EXTRA_DEVICE_ADDRESS );
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice( address );
        // Attempt to connect to the device
        connectDevice( device, secure );
    }
	
	private void ensuerBluetooth() {
		if ( null == bluetoothAdapter ) {
			Toast.makeText( this, "Device does not support Bluetooth", Toast.LENGTH_LONG )
				.show();
		} else if ( ! bluetoothAdapter.isEnabled() ) {
			Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
		    startActivityForResult( enableBtIntent, REQUEST_ENABLE_BT  );
		} 	
	}
	
	private void setUpConversationDisplay() {
	    // Initialize the array adapter for the conversation thread
	    conversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
	    conversationView = (ListView) findViewById(R.id.in);
	    conversationView.setAdapter(conversationArrayAdapter);
	    
//	    setupMasterStation();		
	}
	
	private void setupMasterStation() {
		// 将 MasterStation 放入 BluetoothMasterStationService 更为合适
		if ( null == masterStationService ) {
			masterStationService = new MasterStationService( this, handler );			
		}
		
//		if ( null == masterStatoin ) masterStatoin = DefaultMmcpMasterStation.getInstance();
	}
	
//	private BusTransmitterAgent agent = new BusTransmitterAgent() {
//		
//		@Override
//		public boolean onTranact(byte[] data) {
//			sendMessage( data );
//			
//			return false;
//		}
//		
//		@Override
//		public boolean isOpen() {
//			return false;
//		}
//		
//		@Override
//		public boolean isClosed() {
//			return false;
//		}
//	};
	
    // Layout Views
    private ListView conversationView;
	
    // Name of the connected device
    private String connectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> conversationArrayAdapter;
	
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
            switch ( msg.what ) {
            case MmcpService.MSG_STATE_CHANGE: {
            	
                if ( DEBUG ) Log.i( LOG_TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1 );
                
                switch ( msg.arg1 ) {
                case MasterStationService.STATE_CONNECTED:
                	
                    setStatus( getString( R.string.title_connected_to, connectedDeviceName ) );
                    setProgressBarIndeterminateVisibility( false );
                    conversationArrayAdapter.clear();
                    
                    break;
                    
                case MasterStationService.STATE_CONNECTING:
                	
                    setStatus( R.string.title_connecting );
                    
                    break;
                    
                case MasterStationService.STATE_SCANNING:
                	
                	setStatus( "READY" );
//                  setStatus( R.string.scanning );
                	
                	break;
                	
                /*case BluetoothMasterStationService.STATE_LISTEN:*/
                case MasterStationService.STATE_NONE:
                	
                    setStatus( R.string.title_not_connected );
                    
                    break;
                }
            }
            
            	break;
            	
            case MmcpService.MSG_WRITE:
            	
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                Log.i( LOG_TAG, "SENT:  " + ByteUtils.bytesToHex( writeBuf ) );
                
                break;
                
            case MmcpService.MSG_READ:
            	
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
//                String readMessage = new String( readBuf, 0, msg.arg1 );
//                conversationArrayAdapter.add( connectedDeviceName+":  " + readMessage );
//                System.out.println( "## Read: " + Arrays.toString( readBuf ) );
//                byte[] bytes = new byte[ msg.arg1 ];
                
//                ByteUtils.arrayCopy( readBuf, 0, bytes, 0, bytes.length );         
//                agent.getReceiver().receive( bytes );
                
                break;
                
            case MmcpService.MSG_DEVICE_NAME:
            	
                // save the connected device's name
                connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText( getApplicationContext(), "Connected to "
                               + connectedDeviceName, Toast.LENGTH_SHORT).show();
                
                break;
                
            case MmcpService.MSG_TOAST:
            	
                Toast.makeText( getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                
                break;
            }
		}
	};
	
	private void setStatus(int resId) {
		setTitle( resId );
	}
	
	private final void setStatus(CharSequence title) {
		setTitle( title );
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate( R.menu.main, menu );
		return true;
	}

	private void attemptPairding() {
        BluetoothDevice b = bluetoothAdapter.getRemoteDevice( "00:15:FF:F2:4F:10" );
        Method []toFind_setPasskey = b.getClass().getDeclaredMethods();
        Object []args = null;
        for ( Method m : toFind_setPasskey ) {
        	if ( "setPasskey".equals( m.getName() ) ) {
        		args = new Object[ 1 ];
        		args[ 0 ] = 2;
        		m.setAccessible( true );
        		try {
        			Log.i( LOG_TAG, "## setPassKey " + m.invoke( b, args ) );
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
        	}
        }
        
        connectDevice( b, true );
	}
	
	private MmcpService.OperationResultReceiver resultReceiver = new MmcpService.OperationResultReceiver() {

		@Override
		public void onReceive(ResultGenerator generator) {
			PowerEnergyReadOperatoin.Result now = (Result) generator.generateResult();
			Log.i( LOG_TAG, 
					String.format( "Received measurement result: %s kWh", now.value ) );
		}
	};
	
	private void getStarted() {
		
		// Step 1: Try to starting The MmcpService
		
		MmcpService.start( this );
		
		// Step 2: 
		
		// Used to creating we needs operation
		OperationFactory factory = OperationFactory.getInstance();
		
		// 33, 34
		// 2 号 位于  00:15:FF:F2:4F:10
		// 3 号 1203200047
		
		// To register the Bus to the Service
		Bus bus = MmcpService.registerBus( "00:15:FF:F2:4F:10" );
		( (BluetoothBus) bus ).debugSetPIN( "1234" );
		
		// TX:
		// CORRECT:   FEFE 68 3400200312AA 68 01 02 43C3 EC 16
		// INCORRECT: FEFE 68 3400200312AA 68 01 02 4FC3 F8 16
		
		// Join a device to the Bus 
		SlaveStation slave = bus.joinDevice( "1203200034" );
		
		PowerEnergyReadOperatoin op = factory.newEnergyReadOperation( slave );
//		PowerEnergyReadOperatoin op = factory.newEnergyReadOperation( "999999999999" );
		
		// 当前正向电能总量
		op
				.setTimeDomain( TimeDomain.CURRENTLY )
				.setClassification( Classification.ACTIVE )
				.setPowerDirection( PowerDirection.POSITIVE_ENERGY )
				.setTariff( Tariff.TOTAL_ENERGY );
		
		// Add a operation for future using
		slave.withOperations( op );
		
		// Set a receiver used for receive result
		MmcpService.setReceiver( resultReceiver );
		
		// Step 3: Enter the listening Mode
		MmcpService.listen();
	}
	
	private boolean did = false;
	
	private void doTest() {
		if ( did ) return;
		
		did = true;
		
		long queueId = -1;
		
		// Used to creating we needs operation
		OperationFactory factory = OperationFactory.getInstance();
		
//		try {
			for ( int i = 0; i < 400; i++ ) {
			
				// 0xFE 0xFE 0x68 0x99 0x99 0x99 0x99 0x99 0x99 0x68 0x01 0x02 0x43 0xC3 0x6F 0x16
				// 3 号
		//		PowerEnergyReadOperatoin op = factory.newEnergyReadOperation( "1203200047" );
					
				// 2 号 位于  00:15:FF:F2:4F:10

//				PowerEnergyReadOperatoin op = factory.newEnergyReadOperation( "1203200034" );
		//		PowerEnergyReadOperatoin op = factory.newEnergyReadOperation( "999999999999" );
				
		//		op
		//				.setTimeDomain( TimeDomain.CURRENTLY )
		//				.setClassification( Classification.ACTIVE )
		//				.setPowerDirection( PowerDirection.POSITIVE_ENERGY )
		//				.setTariff( Tariff.TARIFF_K );
				
				// 当前正向电能总量
//				op
//						.setTimeDomain( TimeDomain.CURRENTLY )
//						.setClassification( Classification.ACTIVE )
//						.setPowerDirection( PowerDirection.POSITIVE_ENERGY )
//						.setTariff( Tariff.TOTAL_ENERGY );
				
//				 queueId = masterStatoin.enqueue( op );
			}
//		} catch (MmcpException e) {
//			e.printStackTrace();
//		}
		
		String last = String.format( "DO TESTING...(%1$s)",  queueId );
		action.setText( last );
		
		// 广播查询结果叠加
//		sendMessage( new byte[] {/*(byte)0xFE, (byte)0xFE, */0x68, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, 0x68, 0x01, 0x02, 0x43, (byte) 0xC3, 0x6F, 0x16 } );
		
		// Parity check 0x00ff
		byte pc = (byte) ((0x68 + 34 + 00 + 20 + 03 + 12 + 00 + 0x68 + 0x01 + 0x02 + 0x43 + 0xC3) & 0x00ff);
//		System.out.println( "## pc: " + pc );

		// 68 34 00 20 03 12 AA 68 01 02 43 C3 EC 16
//		sendMessage( new byte[] {(byte) 0xFE, (byte)0xFE, 0x68, (byte) 0x34, (byte) 0x00, (byte) 0x20, (byte) 0x03, (byte) 0x12, (byte) 0xAA, 0x68, 0x01, 0x02, 0x43, (byte) 0xC3, (byte) 0xEC, 0x16 } );
				
//		byte pc = (byte) ((0x68 + 0x34 + 0x00 + 0x20 + 0x03 + 0x12 + 0x00 + 0x68 + 0x01 + 0x02 + 0x43 + 0xC3) & 0x00ff);
//		System.out.println( "## pc: " + pc );
//		sendMessage( new byte[]{ /*(byte)0xFE, (byte)0xFE,*/ 0x68, (byte)0x34, 0x00, 0x20, 0x03, 0x12, 0x00, 0x68, 0x01, 0x02, 0x43, (byte)0xC3, pc, 0x16 } );
		
//		sendMessage( new byte[]{ (byte)0xFE, (byte)0xFE, 0x68, (byte)0x89, 0x67, 0x45, 0x23, 0x01, 0x00, 0x68, 0x01, 0x02, 0x43, (byte)0xC3, 0x32, 0x16 } );
		
		did = false;
	}
}

/*
 * ByteArrayOutputStream 
 * 
 * import com.google.common.io
private static final byte[] CDRIVES = BaseEncoding.base16().decode("e04fd020ea3a6910a2d808002b30309d");

private static final byte[] CDRIVES = javax.xml.bind.DatatypeConverter.parseHexBinary("e04fd020ea3a6910a2d808002b30309d")

public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                             + Character.digit(s.charAt(i+1), 16));
    }
    return data;
}

String response = "[-47, 1, 16, 84, 2, 101, 110, 83, 111, 109, 101, 32, 78, 70, 67, 32, 68, 97, 116, 97]";      // response from the Python script

String[] byteValues = response.substring(1, response.length() - 1).split(",");
byte[] bytes = new byte[byteValues.length];

for (int i=0, len=bytes.length; i<len; i++) {
   bytes[i] = Byte.valueof(byteValues[i].trim());     
}

String str = new String(bytes);

byte[] b1 = new byte[] {97, 98, 99};

String s1 = Arrays.toString(b1);
String s2 = new String(b1);

System.out.println(s1);        // -> "[97, 98, 99]"
System.out.println(s2);        // -> "abc";
 */