package org.ong.mmcp.protocl;

import static org.ong.mmcp.util.ByteUtils.binaryStringToByte;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * MMCP
 * 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 * @created 2013/11/15 15:50:28 GMT + 08:00
 * @updated 2013/11/15 15:50:28 GMT + 08:00
 */
public final class MMCP {
	// 参考 WidgetHost 实现
	// 有些变量是可读不可写的。
	
	/** Broadcast 地址 */
	public static final String BROADCAST_ADDRESS = "999999999999";
	
	/** 电表的地址长度非 HEX 形式 */
	public static final int ADDRESS_LENGHT = BROADCAST_ADDRESS.length();
	
    public static final int FRONT_LEADING_BYTES_LENGTH 	= 2; // 0xEF 0xEF
    public static final int FRAME_START_FLAG_LENGTH 		= 1;
    public static final int ADDRESS_IN_FRAME_LENGHT 		= ADDRESS_LENGHT >> 1;
    public static final int FRAME_STOP_FLAG_LENGTH 		= 1;
    public static final int CONTROL_CODE_LENGTH 			= 1;
    public static final int DATA_LENGTH_FLAG_LENGTH 		= 1;
    public static final int DATA_IDENTIFIER_LENGTH 		= 2;
    public static final int CHECK_CODE_LENGTH 			= 1;
    public static final int END_FLAG_LENGTH 				= 1;
    
    /** 常用用的‘读’操作 Frame 的长度，注意 DATA segment 未包含参数的情况下 */
    public static final int BASIC_READ_OPERATION_FRAME_LENGTH 
    		= FRONT_LEADING_BYTES_LENGTH 
    		+ FRAME_START_FLAG_LENGTH 
    		+ ADDRESS_IN_FRAME_LENGHT
    		+ FRAME_STOP_FLAG_LENGTH
    		+ CONTROL_CODE_LENGTH
    		+ DATA_LENGTH_FLAG_LENGTH
    		+ DATA_IDENTIFIER_LENGTH
    		+ CHECK_CODE_LENGTH
    		+ END_FLAG_LENGTH;
	
    /** 在发送帧信息之前，先发送 1～4 个字节 0xFE，以唤醒接收方 */
    public static final byte FRONT_LEADING_BYTE 		= (byte) 0xFE;
    public static final byte FRAME_START_FLAG_BYTE 	= 0x68;
	public static final byte ADDRESS_REST_FILLED 		= (byte) 0xAA;
	public static final byte FRAME_STOP_FLAG_BYTE 	= FRAME_START_FLAG_BYTE;
	public static final byte END_FLAG_BYTE 			= 0x16;
	
	public static final byte DATA_BYTE_TRANSFER_PROCESS_SEED = 0x33;
	
//	/**
//     * Get a appWidgetId for a host in the calling process.
//     *
//     * @return a appWidgetId
//     */
//    public int allocateQueueId() {
//        try {
//            if (mPackageName == null) {
//                mPackageName = mContext.getPackageName();
//            }
//            return sService.allocateAppWidgetId(mPackageName, mHostId);
//        }
//        catch (RemoteException e) {
//            throw new RuntimeException("system server dead?", e);
//        }
//    }
	
    /**
     * Returns the raw bytes of the parcel.
     *
     * <p class="note">The data you retrieve here <strong>must not</strong>
     * be placed in any kind of persistent storage (on local disk, across
     * a network, etc).  For that, you should use standard serialization
     * or another kind of general serialization mechanism.  The Parcel
     * marshalled representation is highly optimized for local IPC, and as
     * such does not attempt to maintain compatibility with data created
     * in different versions of the platform.
     */
    public final native byte[] marshall();

    /**
     * Set the bytes in data to be the raw bytes of this Parcel.
     */
    public final native void unmarshall(byte[] data, int offest, int length);
    
    /**
     * Move the current read/write position in the parcel.
     * @param pos New offset in the parcel; must be between 0 and
     * {@link #dataSize}.
     */
    public final native void setDataPosition(int pos);
    
    /**
     * Returns the total amount of data contained in the parcel.
     */
    public final native int dataSize();

    /**
     * Returns the amount of data remaining to be read from the
     * parcel.  That is, {@link #dataSize}-{@link #dataPosition}.
     */
    public final native int dataAvail();

    /**
     * Returns the current position in the parcel data.  Never
     * more than {@link #dataSize}.
     */
    public final native int dataPosition();
    
//    /**
//     * Read a byte value from the parcel at the current dataPosition().
//     */
//    public final byte readByte() {
//        return (byte)(readInt() & 0xff);
//    }
    
//    /**
//     * Put a Parcel object back into the pool.  You must not touch
//     * the object after this call.
//     */
//    public final void recycle() {
//        if (DEBUG_RECYCLE) mStack = null;
//        freeBuffer();
//        final Parcel[] pool = mOwnObject != 0 ? sOwnedPool : sHolderPool;
//        synchronized (pool) {
//            for (int i=0; i<POOL_SIZE; i++) {
//                if (pool[i] == null) {
//                    pool[i] = this;
//                    return;
//                }
//            }
//        }
//    }
    
    public static final String INSTRUCTION 		= "INSTRUCTION";
    public static final String CODE 				= "CODE";
    
    // Structure of Frame
    public static final String FRONT_LEADING_BYTES = "Front-Leading-Bytes";
    public static final String FRAME_START_FLAG	= "Frame-Start-Flag";
    public static final String ADDRESS_FIELD 		= "Address-Field";
    
    public static final String FRAME_STOP_FLAG 	= "Frame-Stop-Flag";
    public static final String CONTROL_CODE 		= "Control-Code";
    public static final String DATA_LENGTH 		= "Data-Length";
    public static final String DATA 				= "Data";
    public static final String CHECK_CODE 			= "Check-Code";
    public static final String END_FLAG 			= "End-Flag";
    
    public static final String DATA_IDENTIFIER 					= "DI";
    public static final String DATA_IDENTIFIER_TYPE 				= "DI.1";
    public static final String DATA_IDENTIFIER_ATTRIBUTE 			= "DI.0";
    
    public static final String DATA_IDENTIFIER_TYPE_MOST 			= "DI.1.H";
    public static final String DATA_IDENTIFIER_TYPE_LEAST 			= "DI.1.L";
    
    public static final String DATA_IDENTIFIER_ATTRIBUTE_MOST 		= "DI.0.H";
    public static final String DATA_IDENTIFIER_ATTRIBUTE_LEAST 	= "DI.0.L";
    
    public static final String TRANSMITTED_TRAFFIC	 = "TRANSMITTED_TRAFFIC";
    public static final String RECEIVED_TRAFFIC 	 = "RECEIVED_TRAFFIC";
    
	public static final byte NIBBLE_0000 = binaryStringToByte( "0000" );
	public static final byte NIBBLE_1111 = binaryStringToByte( "1111" );
	
	public static final byte NIBBLE_0010 = binaryStringToByte( "0010" );
	public static final byte NIBBLE_1101 = binaryStringToByte( "1101" );
	
	public static final byte NIBBLE_0011 = binaryStringToByte( "0011" );
	public static final byte NIBBLE_1100 = binaryStringToByte( "1100" );
	
	public static final byte NIBBLE_1000 = binaryStringToByte( "1000" );
	public static final byte NIBBLE_0111 = binaryStringToByte( "0111" );
	
	public static final byte NIBBLE_0001 = binaryStringToByte( "0001" );
	public static final byte NIBBLE_1110 = binaryStringToByte( "1100" );
	
	public static final byte NIBBLE_0101 = binaryStringToByte( "0101" );
	public static final byte NIBBLE_1010 = binaryStringToByte( "1010" );
	
	public static final byte NIBBLE_0110 = binaryStringToByte( "0110" );
	public static final byte NIBBLE_1001 = binaryStringToByte( "1001" );
	
	public static final byte NIBBLE_0100 = binaryStringToByte( "0100" );
	public static final byte NIBBLE_1011 = binaryStringToByte( "1011" );
	
	public static final byte FOURTH_BYTE_00 = NIBBLE_0000;
	public static final byte FOURTH_BYTE_01 = NIBBLE_0001;
	public static final byte FOURTH_BYTE_10 = NIBBLE_0010;
	public static final byte FOURTH_BYTE_11 = NIBBLE_0011;
    
    private MMCP() {}
    // Frame->Sequence->Segment
    // Frame.Length
    // SEQ/ACK analysis: bytes in flight
    // checksum
    // 
    
    // http://hc.apache.org/httpcomponents-core-ga/tutorial/html/nio.html#d5e946
    /**
     * 
     * 	generateResponse:  Invoked to generate a HTTP response message header.
	 *	produceContent:
     * 
     * I/O event dispatchers
     * HTTP I/O event dispatchers serve to convert generic I/O events triggered by an I/O reactor to HTTP protocol specific events. They rely on NHttpClientEventHandler and NHttpServerEventHandler interfaces to propagate HTTP protocol events to a HTTP protocol handler.
     * Server side HTTP I/O events as defined by the NHttpServerEventHandler interface:
     * connected:  Triggered when a new incoming connection has been created.
     * requestReceived:  Triggered when a new HTTP request is received. The connection passed as a parameter to this method is guaranteed to return a valid HTTP request object. If the request received encloses a request entity this method will be followed a series of inputReady events to transfer the request content.
     * inputReady:  Triggered when the underlying channel is ready for reading a new portion of the request entity through the corresponding content decoder. If the content consumer is unable to process the incoming content, input event notifications can temporarily suspended using IOControl interface (super interface of NHttpServerConnection). Please note that the NHttpServerConnection and ContentDecoder objects are not thread-safe and should only be used within the context of this method call. The IOControl object can be shared and used on other thread to resume input event notifications when the handler is capable of processing more content.
     * responseReady:  Triggered when the connection is ready to accept new HTTP response. The protocol handler does not have to submit a response if it is not ready.
     * outputReady:  Triggered when the underlying channel is ready for writing a next portion of the response entity through the corresponding content encoder. If the content producer is unable to generate the outgoing content, output event notifications can be temporarily suspended using IOControl interface (super interface of NHttpServerConnection). Please note that the NHttpServerConnection and ContentEncoder objects are not thread-safe and should only be used within the context of this method call. The IOControl object can be shared and used on other thread to resume output event notifications when more content is made available.
     * exception:  Triggered when an I/O error occurrs while reading from or writing to the underlying channel or when an HTTP protocol violation occurs while receiving an HTTP request.
     * timeout:  Triggered when no input is detected on this connection over the maximum period of inactivity.
     * closed:  Triggered when the connection has been closed.
     * Client side HTTP I/O events as defined by the NHttpClientEventHandler interface:
     * connected:  Triggered when a new outgoing connection has been created. The attachment object passed as a parameter to this event is an arbitrary object that was attached to the session request.
     * requestReady:  Triggered when the connection is ready to accept new HTTP request. The protocol handler does not have to submit a request if it is not ready.
     * outputReady:  Triggered when the underlying channel is ready for writing a next portion of the request entity through the corresponding content encoder. If the content producer is unable to generate the outgoing content, output event notifications can be temporarily suspended using IOControl interface (super interface of NHttpClientConnection). Please note that the NHttpClientConnection and ContentEncoder objects are not thread-safe and should only be used within the context of this method call. The IOControl object can be shared and used on other thread to resume output event notifications when more content is made available.
     * responseReceived:  Triggered when an HTTP response is received. The connection passed as a parameter to this method is guaranteed to return a valid HTTP response object. If the response received encloses a response entity this method will be followed a series of inputReady events to transfer the response content.
     * inputReady:  Triggered when the underlying channel is ready for reading a new portion of the response entity through the corresponding content decoder. If the content consumer is unable to process the incoming content, input event notifications can be temporarily suspended using IOControl interface (super interface of NHttpClientConnection). Please note that the NHttpClientConnection and ContentDecoder objects are not thread-safe and should only be used within the context of this method call. The IOControl object can be shared and used on other thread to resume input event notifications when the handler is capable of processing more content.
     * exception:  Triggered when an I/O error occurs while reading from or writing to the underlying channel or when an HTTP protocol violation occurs while receiving an HTTP response.
     * timeout:  Triggered when no input is detected on this connection over the maximum period of inactivity.
     * closed:  Triggered when the connection has been closed.
     */
    
    public static final int CR = 13; // <US-ASCII CR, carriage return (13)>
    public static final int LF = 10; // <US-ASCII LF, linefeed (10)>
    public static final int SP = 32; // <US-ASCII SP, space (32)>
    public static final int HT = 9;  // <US-ASCII HT, horizontal-tab (9)>    
}

/**
[2013-12-11 08:35:42 - ddm-hello] handling FEAT
[2013-12-11 08:35:42 - ddm-hello] Feature: hprof-heap-dump-streaming
[2013-12-11 08:35:42 - ddm-hello] Feature: hprof-heap-dump
[2013-12-11 08:35:42 - ddm-hello] Feature: method-trace-profiling-streaming
[2013-12-11 08:35:42 - ddm-hello] Feature: method-trace-profiling
[2013-12-11 08:35:42 - ddms] Removing req 0x40000006 from set
[2013-12-11 08:35:42 - ddms] Checking 20 bytes
[2013-12-11 08:35:42 - ddms] Found 0x40000007 in request set - com.android.ddmlib.HandleProfiling@5de6f458
[2013-12-11 08:35:42 - ddms] Found 0x40000007 in request set - com.android.ddmlib.HandleProfiling@5de6f458
[2013-12-11 08:35:42 - ddms] Calling handler for MPRQ [com.android.ddmlib.HandleProfiling@5de6f458] (len=1)
[2013-12-11 08:35:42 - ddm-prof] handling MPRQ
[2013-12-11 08:35:42 - ddm-prof] Method profiling is not running
[2013-12-11 08:35:42 - ddms] Removing req 0x40000007 from set
[2013-12-11 08:35:47 - ddms] Read 21 bytes from [Client pid: 13470]
[2013-12-11 08:35:47 - ddms] Checking 21 bytes
[2013-12-11 08:35:47 - ddms] Closing [Client pid: 13470]
[2013-12-11 08:35:47 - ddms] Forwarding client event 0x10000000 to [Debugger 8601-->13470 inactive]
[2013-12-11 08:35:47 - ddms] Saving packet 0x10000000
[2013-12-11 08:35:47 - ddms] broadcast 3: [Client pid: 13470]
[2013-12-11 08:35:47 - ddms] moving 21 bytes
[2013-12-11 08:35:47 - ddm-hello] Now disconnected: [Client pid: 13470]
[2013-12-11 08:35:47 - ddms] execute '/system/bin/uiautomator dump /data/local/tmp/uidump.xml' on '4df7ef0257d9bf89' : EOF hit. Read: -1
[2013-12-11 08:35:47 - ddms] execute: returning
[2013-12-11 08:35:47 - ddms] image params: bpp=32, size=3686400, width=720, height=1280
[2013-12-11 08:36:07 - ddms] execute: running rm /data/local/tmp/uidump.xml
[2013-12-11 08:36:07 - ddms] execute 'rm /data/local/tmp/uidump.xml' on '4df7ef0257d9bf89' : EOF hit. Read: -1
[2013-12-11 08:36:07 - ddms] execute: returning
[2013-12-11 08:36:07 - ddms] execute: running /system/bin/uiautomator dump /data/local/tmp/uidump.xml
[2013-12-11 08:36:07 - ddms] Created: [Debugger 8601-->13733 inactive]
[2013-12-11 08:36:07 - ddm-heap] Sending REAQ
[2013-12-11 08:36:07 - ddms] Adding req 0x40000008 to set
[2013-12-11 08:36:07 - ddms] Adding new client [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Read 14 bytes from [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Good handshake from client, sending HELO to 13733
[2013-12-11 08:36:07 - ddm-hello] Sending HELO ID=0x40000009
[2013-12-11 08:36:07 - ddms] Adding req 0x40000009 to set
[2013-12-11 08:36:07 - ddm-heap] Sending FEAT
[2013-12-11 08:36:07 - ddms] Adding req 0x4000000a to set
[2013-12-11 08:36:07 - ddm-prof] Sending MPRQ
[2013-12-11 08:36:07 - ddms] Adding req 0x4000000b to set
[2013-12-11 08:36:07 - ddms] Read 20 bytes from [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Checking 20 bytes
[2013-12-11 08:36:07 - ddms] Found 0x40000008 in request set - com.android.ddmlib.HandleHeap@55b3ad06
[2013-12-11 08:36:07 - ddms] Found 0x40000008 in request set - com.android.ddmlib.HandleHeap@55b3ad06
[2013-12-11 08:36:07 - ddms] broadcast 2: [Client pid: 13733]
[2013-12-11 08:36:07 - ddm-thread] Now ready: [Client pid: 13733]
[2013-12-11 08:36:07 - ddm-hello] Now ready: [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Calling handler for REAQ [com.android.ddmlib.HandleHeap@55b3ad06] (len=1)
[2013-12-11 08:36:07 - ddm-heap] handling REAQ
[2013-12-11 08:36:07 - ddm-heap] REAQ says: enabled=false
[2013-12-11 08:36:07 - ddms] Removing req 0x40000008 from set
[2013-12-11 08:36:07 - ddms] Read 61 bytes from [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Checking 61 bytes
[2013-12-11 08:36:07 - ddms] Found 0x40000009 in request set - com.android.ddmlib.HandleHello@59bbe49d
[2013-12-11 08:36:07 - ddms] Found 0x40000009 in request set - com.android.ddmlib.HandleHello@59bbe49d
[2013-12-11 08:36:07 - ddms] Calling handler for HELO [com.android.ddmlib.HandleHello@59bbe49d] (len=42)
[2013-12-11 08:36:07 - ddm-hello] handling HELO
[2013-12-11 08:36:07 - ddm-hello] HELO: v=1, pid=13733, vm='Dalvik v1.6.0', app=''
[2013-12-11 08:36:07 - ddms] Removing req 0x40000009 from set
[2013-12-11 08:36:07 - ddms] Read 227 bytes from [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Checking 227 bytes
[2013-12-11 08:36:07 - ddms] Found 0x4000000a in request set - com.android.ddmlib.HandleHello@59bbe49d
[2013-12-11 08:36:07 - ddms] Found 0x4000000a in request set - com.android.ddmlib.HandleHello@59bbe49d
[2013-12-11 08:36:07 - ddms] Calling handler for FEAT [com.android.ddmlib.HandleHello@59bbe49d] (len=208)
[2013-12-11 08:36:07 - ddm-hello] handling FEAT
[2013-12-11 08:36:07 - ddm-hello] Feature: hprof-heap-dump-streaming
[2013-12-11 08:36:07 - ddm-hello] Feature: hprof-heap-dump
[2013-12-11 08:36:07 - ddm-hello] Feature: method-trace-profiling-streaming
[2013-12-11 08:36:07 - ddm-hello] Feature: method-trace-profiling
[2013-12-11 08:36:07 - ddms] Removing req 0x4000000a from set
[2013-12-11 08:36:07 - ddms] Read 20 bytes from [Client pid: 13733]
[2013-12-11 08:36:07 - ddms] Checking 20 bytes
[2013-12-11 08:36:07 - ddms] Found 0x4000000b in request set - com.android.ddmlib.HandleProfiling@5de6f458
[2013-12-11 08:36:07 - ddms] Found 0x4000000b in request set - com.android.ddmlib.HandleProfiling@5de6f458
[2013-12-11 08:36:07 - ddms] Calling handler for MPRQ [com.android.ddmlib.HandleProfiling@5de6f458] (len=1)
[2013-12-11 08:36:07 - ddm-prof] handling MPRQ
[2013-12-11 08:36:07 - ddm-prof] Method profiling is not running
[2013-12-11 08:36:07 - ddms] Removing req 0x4000000b from set
[2013-12-11 08:36:08 - ddms] Read 21 bytes from [Client pid: 13733]
[2013-12-11 08:36:08 - ddms] Checking 21 bytes
[2013-12-11 08:36:08 - ddms] Forwarding client event 0x10000000 to [Debugger 8601-->13733 inactive]
[2013-12-11 08:36:08 - ddms] Saving packet 0x10000000
[2013-12-11 08:36:08 - ddms] moving 21 bytes
[2013-12-11 08:36:08 - ddms] Closing [Client pid: 13733]
[2013-12-11 08:36:08 - ddms] broadcast 3: [Client pid: 13733]
[2013-12-11 08:36:08 - ddm-hello] Now disconnected: [Client pid: 13733]
[2013-12-11 08:36:08 - ddms] null
java.lang.NullPointerException
	at com.android.ddmlib.Client.read(Client.java:698)
	at com.android.ddmlib.MonitorThread.processClientActivity(MonitorThread.java:311)
	at com.android.ddmlib.MonitorThread.run(MonitorThread.java:263)

[2013-12-11 08:36:08 - ddms] null
java.lang.NullPointerException
	at com.android.ddmlib.Client.read(Client.java:698)
	at com.android.ddmlib.MonitorThread.processClientActivity(MonitorThread.java:311)
	at com.android.ddmlib.MonitorThread.run(MonitorThread.java:263)

[2013-12-11 08:36:09 - ddms] execute '/system/bin/uiautomator dump /data/local/tmp/uidump.xml' on '4df7ef0257d9bf89' : EOF hit. Read: -1
[2013-12-11 08:36:09 - ddms] execute: returning
[2013-12-11 08:36:09 - ddms] image params: bpp=32, size=3686400, width=720, height=1280
[2013-12-11 08:40:25 - hnsi-LinZhou-HQQ.apk] Uploading hnsi-LinZhou-HQQ.apk onto device '4df7ef0257d9bf89'
[2013-12-11 08:40:25 - Device] Uploading file onto device '4df7ef0257d9bf89'
[2013-12-11 08:40:27 - ddms] execute: running pm install -r  "/data/local/tmp/hnsi-LinZhou-HQQ.apk"
[2013-12-11 08:40:27 - ddms] Created: [Debugger 8601-->13979 inactive]
[2013-12-11 08:40:27 - ddm-heap] Sending REAQ
[2013-12-11 08:40:27 - ddms] Adding req 0x4000000c to set
[2013-12-11 08:40:27 - ddms] Adding new client [Client pid: 13979]
[2013-12-11 08:40:27 - ddms] Read 14 bytes from [Client pid: 13979]
[2013-12-11 08:40:27 - ddms] Good handshake from client, sending HELO to 13979
[2013-12-11 08:40:27 - ddm-hello] Sending HELO ID=0x4000000d
[2013-12-11 08:40:27 - ddms] Adding req 0x4000000d to set
*/

/**
12-11 08:48:02.780: D/(6620): Setting the property MTP_OBJ_PROPERTYCODE_PERSISTENTGUID!!
12-11 08:48:02.780: D/(6620): mtp_send_response_mtp_handle : [SUCCESS], Opcode[0x9805], ResponseCode[0x2001], NumParams[0]!!
12-11 08:48:02.785: D/(6620): DEVICE_PHASE_IDLE
12-11 08:48:02.785: D/(6620): DataLen:32 dwBytesReceived: 0
12-11 08:48:02.785: D/(6620): Resetting flag g_HostCancel 0
12-11 08:48:02.785: D/(6620): mtp_process_command_mtp_handle : COMMAND[0x9805]!!

*/