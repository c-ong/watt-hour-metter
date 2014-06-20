/*
 * Copyright (C) 2009 The Android Open Source Project
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

package org.ong.mmcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.ong.mmcp.impl.DefaultMmcpMasterStation;
import org.ong.mmcp.io.BusTransmitterAgent;
import org.ong.mmcp.util.ByteUtils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;
import cn.hnsi.android.apps.metterreader.MainActivity;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public final class MasterStationService {
	// TODO 对该实现提供保护...
	
    // Debugging
    private static final String LOG_TAG = "MasterStationService";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    
    // Key names received from the Incoming Handler
    static public final String DEVICE_NAME 	= "DEVICE_NAME";
    static public final String TOAST 			= "TOAST";
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE 			= 0; // we're doing nothing
    public static final int STATE_SCANNING 		= 1; // now we're attempt to finding an our remote device
//    public static final int STATE_LISTEN 			= 1; // now listening for get into range devices(BluetoothBus)
    public static final int STATE_CONNECTING 		= 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED 		= 3; // now connected to a remote device

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothMasterStationSecure";
    private static final String NAME_INSECURE = "BluetoothMasterStationInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString( "fa87c0d0-afac-11de-8a39-0800200c9a66" );
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString( "8ce255c0-200a-11e0-ac64-0800200c9a66" );    
    
    private static UUID UUID_SECURE;
    
    private Context ctx;

    // Member fields
    private final BluetoothAdapter adapter;
    private final Handler msgDispatcher;
    
//    private AcceptThread secureAcceptThread;
//    private AcceptThread insecureAcceptThread;
    
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    
    private int state;
    
//    private MmcpMasterStation masterStatoin;
    private MasterStation masterStation;    
    
    private BusScanner scanner = null;
    
    // The Bus device scanner
    private DeviceScanner.Listener scannerListener = new DeviceScanner.Listener() {

		@Override
		public void onDiscoveryFinished() {
			if ( DEBUG ) Log.i( LOG_TAG, "++ onDiscoveryFinished" ); 
		}

		@Override
		public void onFound(BluetoothDevice device) {
			if ( DEBUG ) Log.i( LOG_TAG, "++ onFound: One device were found " + device );
			( (MmcpBluetoothMasterStation) masterStation ).onDeviceFound( device );
		}

		@Override
		public void onPairedFound(BluetoothDevice device) {
			if ( DEBUG ) Log.i( LOG_TAG, "++ onPairedFound: One alreay paired device were found " + device );
			( (MmcpBluetoothMasterStation) masterStation ).onDeviceFound( device );
		}
    };
    
	private BusTransmitterAgent agent = new BusTransmitterAgent() {
		
		@Override
		public boolean onTranact(byte[] data) {
			write( data );
			
			return false;
		}
		
		@Override
		public boolean isOpen() {
			return false;
		}
		
		@Override
		public boolean isClosed() {
			return false;
		}
	};

    private UUID obtainUUID() {
    	if ( null != UUID_SECURE ) return UUID_SECURE;
    	
		TelephonyManager tManager = (TelephonyManager) ctx.getSystemService( 
				Context.TELEPHONY_SERVICE );
		String uuid = tManager.getDeviceId();
		UUID result = null;
		try {
			UUID_SECURE = result = UUID.nameUUIDFromBytes( uuid.getBytes( "UTF-8" ) );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return result;
    }
    
    /*package*/ MasterStation masterStation() {
    	return masterStation;
    }

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public MasterStationService(Context context, Handler handler) {
    	ctx = context;
        state = STATE_NONE;        
        
        this.msgDispatcher = handler;
        
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if ( DEBUG ) Log.i( LOG_TAG, "--> setState() " + debugGetStateString( this.state ) 
        		+ " -> " + debugGetStateString( state ) + " " + Thread.currentThread() );
        
        this.state = state;

        // Give the new state to the Handler so the UI Activity Or other Process can update
        msgDispatcher.obtainMessage( MmcpService.MSG_STATE_CHANGE, state, -1 )
        	.sendToTarget();
        
        // These will be accept
        if ( STATE_NONE == state || STATE_CONNECTING == state || STATE_CONNECTED == state ) {
        	masterStation.current().setState( state );	
        }        
    }
    
    static public String debugGetStateString(final int state) {
    	switch ( state ) {
    	default: 				return "UNKNOWN";
    	
    	case STATE_NONE: 		return "STATE_NONE";
    	case STATE_SCANNING:	return "STATE_SCANNING";
    	case STATE_CONNECTING: 	return "STATE_CONNECTING";
    	case STATE_CONNECTED: 	return "STATE_CONNECTED";
    	}
    }
    
    static public String debugGetMsgWhatString(final Message msg) {
    	switch ( msg.what ) {
    	default: 				return "UNKNOWN";
    	
    	case MmcpService.MSG_STATE_CHANGE: 	return "MSG_STATE_CHANGE";
    	case MmcpService.MSG_DEVICE_NAME:	return "MSG_DEVICE_NAME";
    	case MmcpService.MSG_WRITE: 		return "MSG_WRITE";
    	case MmcpService.MSG_READ: 			return "MSG_READ";
    	case MmcpService.MSG_TOAST: 		return "MSG_TOAST";
    	}
    }   

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return state;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if ( DEBUG ) Log.d( LOG_TAG, "To starting the Service..." );

        // Cancel any thread attempting to make a connection
        if ( connectThread != null ) { 
        	connectThread.cancel(); 
        	connectThread = null; 
        }

        // Cancel any thread currently running a connection
        if ( connectedThread != null ) { 
        	connectedThread.cancel(); 
        	connectedThread = null; 
        }
        
        masterStation = DefaultMmcpMasterStation.from( this );        
        masterStation.setBusTransmitterAgent( agent );
        
        scanner = new BusScanner( ctx, scannerListener );
        scanner.start();

        setState( STATE_SCANNING );
        
//        setState( STATE_LISTEN );

//        // Start the thread to listen on a BluetoothServerSocket
//        if ( secureAcceptThread == null ) {
//            secureAcceptThread = new AcceptThread( true );
//            secureAcceptThread.start();
//        }
//        if ( insecureAcceptThread == null) {
//            insecureAcceptThread = new AcceptThread( false );
//            insecureAcceptThread.start();
//        }
    }
    
    private void restart() {
        if ( DEBUG ) Log.d( LOG_TAG, "restart" );

        // Cancel any thread attempting to make a connection
        if ( connectThread != null ) { 
        	connectThread.cancel(); 
        	connectThread = null; 
        }

        // Cancel any thread currently running a connection
        if ( connectedThread != null ) { 
        	connectedThread.cancel(); 
        	connectedThread = null; 
        }
        
        scanner.requestSuspend( false );
        
        setState( STATE_SCANNING );
        
//      setState( STATE_LISTEN );

//      // Start the thread to listen on a BluetoothServerSocket
//      if ( secureAcceptThread == null ) {
//          secureAcceptThread = new AcceptThread( true );
//          secureAcceptThread.start();
//      }
//      if ( insecureAcceptThread == null) {
//          insecureAcceptThread = new AcceptThread( false );
//          insecureAcceptThread.start();
//      }
    }
    
    /*package*/ void listen() {
    	scanner.listen();
    	masterStation.listen();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
    	// I'm in Main threading...Thread.currentThread()
        if ( DEBUG ) Log.d( LOG_TAG, "connect to: " + device );

        // Cancel any thread attempting to make a connection
        if ( state == STATE_CONNECTING ) {
            if ( connectThread != null ) { 
            	connectThread.cancel(); 
            	connectThread = null; 
            }
        }

        // Cancel any thread currently running a connection
        if ( connectedThread != null ) { 
        	connectedThread.cancel(); 
        	connectedThread = null; 
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread( device, secure );
        connectThread.start();
        
        setState( STATE_CONNECTING );
    }
    
    public void connect(String address, boolean secure) {
    	// Get the device MAC address        
    	// Get the BluetoothDevice object
    	// Attempt to connect to the device    	
    	BluetoothDevice device = adapter.getRemoteDevice( address );
    	connect( device, secure );    	
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        if ( DEBUG ) Log.d( LOG_TAG, "connected, Socket Type:" + socketType );

        // Cancel the thread that completed the connection
        if ( connectThread != null ) {
        	connectThread.cancel(); 
        	connectThread = null; 
        }

        // Cancel any thread currently running a connection
        if ( connectedThread != null ) { 
        	connectedThread.cancel(); 
        	connectedThread = null; 
        }

//        // Cancel the accept thread because we only want to connect to one device
//        if ( secureAcceptThread != null ) {
//            secureAcceptThread.cancel();
//            secureAcceptThread = null;
//        }
//        if ( insecureAcceptThread != null ) {
//            insecureAcceptThread.cancel();
//            insecureAcceptThread = null;
//        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread( socket, socketType );
        connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = msgDispatcher.obtainMessage( MmcpService.MSG_DEVICE_NAME );
        Bundle bundle = new Bundle();
        String nameOrAddr = null != device.getName() ? device.getName() : device.getAddress();
        bundle.putString( MainActivity.DEVICE_NAME, nameOrAddr );
        msg.setData( bundle );
        
        msgDispatcher.sendMessage( msg );

        setState( STATE_CONNECTED );
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if ( DEBUG ) Log.d( LOG_TAG, "stop" );

        if ( connectThread != null ) {
            connectThread.cancel();
            connectThread = null;
        }

        if ( connectedThread != null ) {
            connectedThread.cancel();
            connectedThread = null;
        }

//        if ( secureAcceptThread != null ) {
//            secureAcceptThread.cancel();
//            secureAcceptThread = null;
//        }
//
//        if ( insecureAcceptThread != null ) {
//            insecureAcceptThread.cancel();
//            insecureAcceptThread = null;
//        }
        
        setState( STATE_NONE );
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized ( this ) {
            if ( state != STATE_CONNECTED ) return;
            r = connectedThread;
        }
        
        // Perform the write unsynchronized
        r.write( out );
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
    	if ( DEBUG ) Log.i( LOG_TAG, "-- connectionFailed" ); 
    	
        // Send a failure message back to the Activity or other Process
        Message msg = msgDispatcher.obtainMessage( MmcpService.MSG_TOAST );
        Bundle bundle = new Bundle();
        bundle.putString( MainActivity.TOAST, "Unable to connect device" );
        msg.setData( bundle );
        msgDispatcher.sendMessage( msg );

        // Start the service over to restart listening mode
        MasterStationService.this.restart();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
    	if ( DEBUG ) Log.i( LOG_TAG, "-- connectionLost" ); 
    	
        // Send a failure message back to the Activity Or other Process
        Message msg = msgDispatcher.obtainMessage( MmcpService.MSG_TOAST );
        Bundle bundle = new Bundle();
        bundle.putString( MainActivity.TOAST, "Device connection was lost" );
        msg.setData( bundle );
        msgDispatcher.sendMessage( msg );

        // Start the service over to restart listening mode
        MasterStationService.this.restart();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
//    private class AcceptThread extends Thread {
//        // The local server socket
//        private final BluetoothServerSocket mmServerSocket;
//        private String mSocketType;
//
//        public AcceptThread(boolean secure) {
//            BluetoothServerSocket tmp = null;
//            mSocketType = secure ? "Secure" : "Insecure";
//
//            // Create a new listening server socket
//            try {
//                if ( secure ) {
//                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
//                    		/*UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")*//*obtainUUID()*/MY_UUID_SECURE);
//                } else {
//                	tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_INSECURE,
//                            MY_UUID_INSECURE );
//                    ///tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
//                    ///        NAME_INSECURE, MY_UUID_INSECURE);                	
//                }
//            } catch (IOException e) {
//                Log.e(LOG_TAG, "Socket Type: " + mSocketType + "listen() failed", e);
//            }
//            mmServerSocket = tmp;
//        }
//
//        public void run() {
//            if ( DEBUG ) Log.d(LOG_TAG, "Socket Type: " + mSocketType +
//                    "BEGIN AcceptThread" + this);
//            setName( "AcceptThread" + mSocketType );
//
//            BluetoothSocket socket = null;
//
//            // Listen to the server socket if we're not connected
//            while ( state != STATE_CONNECTED ) {
//                try {
//                    // This is a blocking call and will only return on a
//                    // successful connection or an exception
//                	System.out.println( "## " + mmServerSocket );
//                    socket = mmServerSocket.accept();
//                } catch (IOException e) {
//                    Log.e(LOG_TAG, "Socket Type: " + mSocketType + " accept() failed", e);
//                    e.printStackTrace();
//                    break;
//                }
//
//                // If a connection was accepted
//                if ( socket != null ) {
//                    synchronized (BluetoothMasterStationService.this) {
//                        switch ( state ) {
    
//                        case STATE_LISTEN:
//                        case STATE_CONNECTING:
//                            // Situation normal. Start the connected thread.
//                            connected( socket, socket.getRemoteDevice(),
//                                    mSocketType );
//                            break;
//                        case STATE_NONE:
//                        case STATE_CONNECTED:
//                            // Either not ready or already connected. Terminate new socket.
//                            try {
//                                socket.close();
//                            } catch (IOException e) {
//                                Log.e(LOG_TAG, "Could not close unwanted socket", e);
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//            if ( DEBUG ) Log.i( LOG_TAG, "END AcceptThread, socket Type: " + mSocketType );
//
//        }
//
//        public void cancel() {
//            if ( DEBUG ) Log.d( LOG_TAG, "Socket Type" + mSocketType + "cancel " + this );
//            try {
//                mmServerSocket.close();
//            } catch (IOException e) {
//                Log.e( LOG_TAG, "Socket Type" + mSocketType + "close() of server failed", e );
//            }
//        }
//    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if ( secure ) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" )/*MY_UUID_SECURE*/ );
                } else {
                	tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE );
//                    tmp = device.createInsecureRfcommSocketToServiceRecord(
//                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e( LOG_TAG, "Socket Type: " + mSocketType + "create() failed", e );
            }
            
            mmSocket = tmp;
        }

        public void run() {
            Log.i( LOG_TAG, "BEGIN ConnectThread SocketType:" + mSocketType );
            setName( "ConnectThread" + mSocketType );

            // Always cancel discovery because it will slow down a connection
            // FIXME We should be at least found One or more device after,  
            //        to begin the process. Jun. 12, 2014 09:29
//            adapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
                
            } catch (IOException e) {
            	
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e( LOG_TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2 );
                }
                
                connectionFailed();
                
                if ( DEBUG ) {
                	e.printStackTrace();
                }
                
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized ( MasterStationService.this ) {
                connectThread = null;
            }

            // Start the connected thread
            connected( mmSocket, mmDevice, mSocketType );
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private /*final*/ InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
        	// Cancel discovery
        	scanner.requestSuspend( true );
        	
            Log.d( LOG_TAG, "create ConnectedThread: " + socketType );
            mmSocket = socket;
            
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
            	tmpIn = socket.getInputStream();
            	tmpOut = socket.getOutputStream();                
            	
            } catch (IOException e) {
                Log.e(LOG_TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i( LOG_TAG, "BEGIN ConnectedThread" );
            
            // 5.2.4 数据长度 L：L为数据域的字节数。读数据时 L≤200，写数据时 L≤50，L=0表示无 数据域
            // 68H A0 A1 A2 A3 A4 A5 68H C L DATA CS 16H
            byte[] buffer = new byte[ 30 ];
            //byte[] buffer = new byte[ 1 + 6 + 1 + 1 + 1 + 200 + 1 + 1 ];
            int bytes;

            // Keep listening to the InputStream while connected
           
            while ( true ) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read( buffer );
                    
                    byte []dump = new byte[ bytes ];
                    ByteUtils.arrayCopy( buffer, 0, dump, 0, bytes );
                    
                	if ( DEBUG ) Log.i( LOG_TAG, "## Run:RX: " + ByteUtils.dumpHex( dump ) );
                	
                	// FXIME 暂时不使用消息队列，因其不能保证顺序，注意这样是 IO 阻塞模式...
                	agent.getReceiver().receive( dump );

                    // Send the obtained bytes to the UI Activity
                    msgDispatcher.obtainMessage( MmcpService.MSG_READ, bytes, -1, buffer )
                            .sendToTarget();                    
                } catch (IOException e) {
                    Log.e( LOG_TAG, "disconnected", e );
                    
                    connectionLost();
                    
                    // Start the service over to restart listening mode
                    MasterStationService.this.restart();
                    
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write( buffer );
                mmOutStream.flush();
                
                // Share the sent message back to the UI Activity
                Message msg = msgDispatcher.obtainMessage( 
                		MmcpService.MSG_WRITE, -1, -1, buffer );
                msg.sendToTarget();
                
            } catch (IOException e) {
                Log.e( LOG_TAG, "Exception during write", e );
            } finally {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e( LOG_TAG, "close() of connect socket failed", e );
            }
        }
    }

	public void deliverResult(Operation which) {
		Message msg = msgDispatcher.obtainMessage( 
				MmcpService.MSG_OP_RESULT_RECEIVED, which );
		msg.sendToTarget();
	}
}

// Pair
// http://stackoverflow.com/questions/4989902/how-to-programmatically-pair-a-bluetooth-device-on-android
// Uppair Bluetooth on Android
// http://stackoverflow.com/questions/3462968/how-to-unpair-bluetooth-device-using-android-2-1-sdk
// http://stackoverflow.com/questions/9608140/how-to-unpair-or-delete-paired-bluetooth-device-programmatically-on-android/11147911#11147911