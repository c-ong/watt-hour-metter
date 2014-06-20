package org.ong.mmcp.op;

import org.ong.mmcp.Operation;
import org.ong.mmcp.ReadOperation;
import org.ong.mmcp.TrafficStats;
import org.ong.mmcp.protocl.ControlCode;
import org.ong.mmcp.protocl.FunctionCode;
import org.ong.mmcp.protocl.MMCP;
import org.ong.mmcp.protocl.di.Classification;
import org.ong.mmcp.protocl.di.ClassificationAttrSetter;
import org.ong.mmcp.protocl.di.PowerDirection;
import org.ong.mmcp.protocl.di.PowerDirectionAttrSetter;
import org.ong.mmcp.protocl.di.Tariff;
import org.ong.mmcp.protocl.di.TariffAttrSetter;
import org.ong.mmcp.protocl.di.TimeDomain;
import org.ong.mmcp.protocl.di.TimeDomainAttrSetter;
import org.ong.mmcp.protocl.di.Type;
import org.ong.mmcp.queue.QueueEntry;
import org.ong.mmcp.util.AddressUtils;
import org.ong.mmcp.util.ByteUtils;
import org.ong.mmcp.BroadcastUnsupportedOperationException;

import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class PowerEnergyReadOperatoin extends ReadOperation<PowerEnergyReadOperatoin.Result> 
	implements
			TimeDomainAttrSetter<PowerEnergyReadOperatoin>, 
			ClassificationAttrSetter<PowerEnergyReadOperatoin>, 
			PowerDirectionAttrSetter<PowerEnergyReadOperatoin>,
			TariffAttrSetter<PowerEnergyReadOperatoin> {
	
	public static class Result implements IResult {
		public final double value;
		
		/*package*/ Result(double measured) {
			this.value = measured;
		}
		
		private Result() { value = 0.0D; }

		@Override
		public Operation getOperation() {
			return null;
		}
	}
	
	// 入队
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "PowerEnergyReadOperatoin";
	
	/** 返回的电量数据用 4 个 Byte 来表示 */
	private static final int MEASUREMENT_VALUE_LENGTH = 4;
	
    /** 常用的‘读’操作 Frame 的长度，注意 DATA segment 未包含参数的情况下 */
    private static final int RESPONSE_FRAME_LENGTH 
    		= MMCP.FRONT_LEADING_BYTES_LENGTH 
    		+ MMCP.FRAME_START_FLAG_LENGTH 
    		+ MMCP.ADDRESS_IN_FRAME_LENGHT
    		+ MMCP.FRAME_STOP_FLAG_LENGTH
    		+ MMCP.CONTROL_CODE_LENGTH
    		+ MMCP.DATA_LENGTH_FLAG_LENGTH
    		+ MMCP.DATA_IDENTIFIER_LENGTH
    		+ MEASUREMENT_VALUE_LENGTH
    		+ MMCP.CHECK_CODE_LENGTH
    		+ MMCP.END_FLAG_LENGTH;
    
    private volatile boolean processing = false;
	
	public long getQueueId() {
		return -1;
	}
	
	@Override
	public Result generateResult() {
		return measuredResult;
	}
	
	public PowerEnergyReadOperatoin(String address) {
		super( address );
		
		if ( AddressUtils.isBroadcastAddress( address ) )
			throw new BroadcastUnsupportedOperationException( 
					"Current operation is not supported for broadcasting!" );
		
		setType( Type.POWER_ENERGY );
	}

	@Override
	public PowerEnergyReadOperatoin setTariff(Tariff tariff) {
		this.tariff = tariff;
		return this;
	}

	@Override
	public PowerEnergyReadOperatoin setPowerDirection(PowerDirection direction) {
		this.direction = direction;
		return this;
	}

	@Override
	public PowerEnergyReadOperatoin setClassification(Classification classific) {
		this.classific = classific;
		return this;
	}

	@Override
	public PowerEnergyReadOperatoin setTimeDomain(TimeDomain time_domain) {
		this._TimeDomain = time_domain;
		return this;
	}

	@Override
	public boolean cancel() {
		return false;
	}
	
	@Override
	protected int calculateLength() {
		return MMCP.BASIC_READ_OPERATION_FRAME_LENGTH;
	}
	
	@Override
	protected byte obtainDataLength() {
		return MMCP.DATA_IDENTIFIER_LENGTH;
	}
	
	@Override
	protected byte generateControlCode() {
		byte result = 0;
		
		// Transfer direction(post:D7, And always 1)
		byte requestDirection 			= ControlCode.TRANSFER_DIRECTION_D7_VAL_0_REQUEST << 7;
		
		// Flag indicates whether slave station is abnormal(pst:D6, And always 0)
		byte noNeededSlaveStationFlag 	= 0 << 6;
		
		// Flag of subsequent frame(pos:D5, And always 0)
		byte noSubsequent 				= ControlCode.HAS_SUBSEQUENT_FRAME_FLAG_D5_VAL_0_NO << 5;
		
		// Function code(pos:D4~D0, And always 00001)
		byte read 						= FunctionCode.READ_DATA;
		
		result = (byte) ( requestDirection + noNeededSlaveStationFlag + noSubsequent + read );
		
		return result;
	}
	
	@Override
	protected byte[] generateDataIdentifier() {
		byte []result = new byte[ MMCP.DATA_IDENTIFIER_LENGTH ];
		byte di1 = 0, di0 = 0;
		
		// Definition shift
		di1 += type.BIT_DI1_D7_TO_D4;		// DI1H

		di1 += _TimeDomain.BIT_D3_TO_D2;	// DI1L
		di1 += classific.BIT_DI1_D1_TO_D0;
		
		di0 += direction.BIT_DI0_D7_TO_D4;	// DI0H
		di0 += tariff.BIT_DI0_D3_TO_D0;		// DI0L
				
		result[ 0 ] = di1;
		result[ 1 ] = di0;
		
		return result;
	}

	// TODO 在不繁忙的时候数据可能是多个 Byte 被响应...
	// FIXME 目前我们屏弃消息队列来传送响应数据，从而避免了数据扰乱问题...
	@Override
	protected void receiveReply(final QueueEntry task, byte[] reply) {
		if ( DEBUG ) Log.i( LOG_TAG, "## RECEIVED:QueueId= " + task.getId() 
				+ " bytes=" + ByteUtils.dumpHex( reply ) );
		
		going = false;
		
		ensureReceiver( RESPONSE_FRAME_LENGTH );
		putIntoBuffer( reply );
		
		if ( ! processing ) {
			processing = true;
			resolveStreamReply( task );
		}
	}
	
	/*
	 * Step 1: Control code;
	 * Step 2: Check code -> Extracting result -> Create result...
	 * Step 3: End flag.
	 */
	private void resolveStreamReply(final QueueEntry task) {
		if ( null == cursor ) cursor = ReceiveCursor.FRONT_LEAD; 
		if ( DEBUG ) Log.i( LOG_TAG, "## streamReplyResolve: cursor= " + cursor );
		
		// 通常响应的数据都是这样的结构
		switch ( cursor ) {
		
		case FRONT_LEAD:
			
			if ( readFrontLeadByte() ) 
				setNextCursor( ReceiveCursor.FRAME_START_FLAG );
			
			break;
			
		case FRAME_START_FLAG:
			
			if ( readFrameStartFlag() ) 
				setNextCursor( ReceiveCursor.ADDRESS_FIELD );
			
			break;
			
		case ADDRESS_FIELD:
			
			if ( readAddress() ) 
				setNextCursor( ReceiveCursor.FRAME_STOP_FLAG );
			
			break;
			
		case FRAME_STOP_FLAG:
			
			if ( readFrameStopFlag() )
				setNextCursor( ReceiveCursor.CONTROL_CODE );
			
			break;
			
		case CONTROL_CODE:
			
			if ( readControlCode() )
				setNextCursor( ReceiveCursor.DATA_LENGTH );
			
			break;
			
		case DATA_LENGTH:
			
			if ( readDataLength() )
				setNextCursor( ReceiveCursor.DATA );
			
			break;
			
		case DATA:
			
			if ( readData() )
				setNextCursor( ReceiveCursor.CHECK_CODE );
			
			break;
			
		case CHECK_CODE:
			
			if ( readCheckCode() ) 
				setNextCursor( ReceiveCursor.END_FLAG );
			
			break;
			
		case END_FLAG:
			
			if ( readEndFlag() ) { 
				setNextCursor( ReceiveCursor.DONE );
				
				onReceiveCompleted( task );
			}
			
			break;
			
		case DONE:
			// do nothing...
			return;
		}
		
		if ( canResolvingNextCursor() ) {
//			going = true;

			if ( DEBUG ) Log.i( LOG_TAG, "## Has more unresolved..." );
			
			doNextCursor( task );
			
		} else {
			
			processing = false;
		} 

		
//		if ( receiver.hasUnresolvedReply() && ! going ) {
//			going = true;
//			if ( DEBUG ) Log.i( LOG_TAG, "## Has more unresolved..." );
//			doNextCursor( task );
//		}
	}
	
	// Can do next...
	private boolean canResolvingNextCursor() {
		return receiver.hasUnresolvedReply() 
				&& receiver.unresovedRemaining() > currentSegmentSize()/* && ! going*/;
	}
	
	private int currentSegmentSize() {
		if ( DEBUG ) Log.i( LOG_TAG, "## currentSegmentSize: " + currentSegmentSize ); 
		return currentSegmentSize;
	}
	
	private void onCursorMoved() {
		resetCurrentSegmentSize();
	}
	
	private void resetCurrentSegmentSize() {
		updateCurrentSegmentSize( 0 );
	}
	
	private void updateCurrentSegmentSize(int size) {
		currentSegmentSize = size;
	}
	
	private volatile int currentSegmentSize = 0;
	
	/** 可能有些情况下对方一次发送一个以上 Byte，我们需要继续处理... */
	private boolean going = false;
	
	private void onReceiveCompleted(final QueueEntry task) {
		if ( DEBUG ) Log.i( LOG_TAG, "## onReceiveCompleted QueueId: " + task.getId() );
		
		// Generating Result -> validation
		if ( validateCheckCodeOnResponsed() ) {
			extractMeasurementValue();
			if ( DEBUG ) Log.i( LOG_TAG, "## Responsed raw: " + 
					ByteUtils.dumpHex( receiver.raw() ) );
			
		} else {
			
			// TODO 告之数据有误...
			if ( DEBUG ) Log.w( LOG_TAG, "!! Data frame is invalid, raw = " 
					+ ByteUtils.dumpHex( receiver.raw() ) ); 
		}
		
		notifyReceiveIsCompleted( task );
//		cursor = ReceiveCursor.DONE;
	}
	
	private void notifyReceiveIsCompleted(final QueueEntry task) {
		task.end();		
	}
	
	/*-----------------------------------<------------------------------*/
	
	// TODO move to Interpreter
	private boolean readFrontLeadByte() {
		// consumed -> readStartFlag
		boolean received = receiver.hasRead( 0, 2 );
		if ( ! received ) {
			updateCurrentSegmentSize( 2 );
			return false;
		}
		byte []bytes = receiver.readSegment( 0, 2 );
		for ( byte b : bytes ) {
			if ( 0 != (MMCP.FRONT_LEADING_BYTE ^ b) ) {
				// TODO Flag error And find out this
				return false;
			}
		}
		
		return true;
	}
	
	private boolean readFrameStartFlag() {
		boolean received = receiver.hasRead( 2 );
		if ( ! received ) {
			updateCurrentSegmentSize( 1 );
			return false;
		}
		byte byt = receiver.read( 2 );
		if ( 0 != (MMCP.FRAME_START_FLAG_BYTE ^ byt) ) {
			return false;
		}
		
		return true;
	}
	
	private boolean readAddress() {
		// skipped 6 byte
		boolean received = receiver.hasRead( 3, 6 );
		if ( ! received ) {
			updateCurrentSegmentSize( 6 );
			return false;
		}
		byte []bytes = receiver.readSegment( 3, 6 );
//		for ( byte b : bytes ) {
//		}
		
		return true;
	} 
	
	private boolean readFrameStopFlag() {
		boolean received = receiver.hasRead( 9 );
		if ( ! received ) {
			updateCurrentSegmentSize( 1 );
			return false;
		}
		byte byt = receiver.read( 9 );
		if ( 0 != (MMCP.FRAME_STOP_FLAG_BYTE ^ byt) ) {
			return false;
		}
		
		return true;
	}
	
	private boolean readControlCode() {
		boolean received = receiver.hasRead( 10 );
		if ( ! received ) {
			updateCurrentSegmentSize( 1 );
			return false;
		}
		byte byt = receiver.read( 10 );
//		if ( 0 != (MMCP.FRAME_STOP_FLAG_BYTE ^ byt) ) {
//			return false;
//		}
		
		return true;
	}
	
	/** 标识响应帧中的数据长度 */
	private int dataLengthOfResponsed = 0;
	
	private boolean readDataLength() {
		boolean received = receiver.hasRead( 11 );
		if ( ! received ) {
			updateCurrentSegmentSize( 1 );
			return false;
		}
		byte byt = receiver.read( 11 );
		dataLengthOfResponsed = byt;
		if ( DEBUG ) Log.i( LOG_TAG, "## Data length: " + dataLengthOfResponsed );
//		if ( 0 != (MMCP.FRAME_STOP_FLAG_BYTE ^ byt) ) {
//			return false;
//		}
		
		return true;
	}
	
	/* 
	 * 0 1  2  3 4 5 6 7 8  9  10 11 1213 14151617 18 19
	 * EFEF 68 340020031200 68 81 06 43C3 5B343333 BB 16
	 */
	private boolean readData() {
		boolean received = receiver.hasRead( 12, 6/*replyDataLength*/ );
		if ( ! received ) {
			updateCurrentSegmentSize( 6 );
			return false;
		}
		
		byte []bytes = receiver.readSegment( 12, 6 );
		// 记录备最后使用...
		dataOfResponsed = bytes;
		
//		for ( byte b : bytes ) {
//		}
		
//		byte []resultRaw = new byte[ 4 ]; 
//		ByteUtils.arrayCopy( bytes, 2, resultRaw, 0, resultRaw.length );
//		extractMeasurementValue( resultRaw );
		
		return true;
	}
	
	private boolean readCheckCode() {
		boolean received = receiver.hasRead( 18 );
		if ( ! received ) {
			updateCurrentSegmentSize( 1 );
			return false;
		}
		byte byt = receiver.read( 18 );
		// 记录备最后校验...
		checkCodeOfResponsed = byt;
		
		return true;
	}
	
	private byte checkCodeOfResponsed;
	
	// FIXME 在本次响应结后再调用该方法
	private boolean validateCheckCodeOnResponsed() {
		byte []sample = receiver.raw();
		
		byte computedResult = ByteUtils.computeParityCheck( sample, 
				MMCP.FRONT_LEADING_BYTES_LENGTH, 
				sample.length - MMCP.CHECK_CODE_LENGTH - MMCP.END_FLAG_LENGTH );
		
		return 0 == (computedResult ^ checkCodeOfResponsed);
	}
	
	private int calculateResponsedLength() {
		return 0;
	}
	
	private boolean readEndFlag() {
		boolean received = receiver.hasRead( 19 );
		if ( ! received ) {
			updateCurrentSegmentSize( 1 );
			return false;
		}
		byte byt = receiver.read( 19 );
		if ( 0 != (MMCP.END_FLAG_BYTE ^ byt) ) {
			return false;
		}
		
		return true;
		// consumed -> notifyFinished
	}
	
	/*----------------------------------->------------------------------*/
	// 正确响应情况下应该为 6 个 byte
	private byte []dataOfResponsed = null;
	
	/**
	 * 提取数据标识。
	 */
	private void extractDI() {
		
	}
	
	/**
	 * 提取度量值。
	 * @param raw
	 */
	private void extractMeasurementValue() {
		byte []raw = new byte[ 4 ]; 
		ByteUtils.arrayCopy( dataOfResponsed, 2, raw, 0, raw.length );
		
		byte []decodeed = new byte[ raw.length ];
		for ( int i = 0; i < decodeed.length; i++ ) {
			decodeed[ i ] = (byte) (raw[ i ] - 0x33);
		}
		// 反转低位与高位
		decodeed = ByteUtils.reverse( decodeed );
		
		// FIXME 这里取 Hex 字面值，例如：0x12, 我们期望 Dec 的结果就是 12 而并非 18，
		// 		  这或许有些匪夷所思但规范是这么定制的。
		String result = getMeasurementStringFromByte( decodeed );
		
		if ( DEBUG ) {
			Log.i( LOG_TAG, "## Result: " + result );
			Log.i( LOG_TAG, "" + ByteUtils.dumpHex( decodeed ) );
		}
		
		try {
			double measured = Double.parseDouble( result );
			if ( DEBUG ) Log.i( LOG_TAG, "Measurement: " + measured ); 
			measuredResult = new Result( measured );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Result measuredResult = null;
	
	/**
	 * 返回 String 形式测量结果。
	 * @param bytes
	 * @return
	 */
	private String getMeasurementStringFromByte(byte []bytes) {
		// 000000.00
		String result = String.format( "%02X%02X%02X.%02X",
				bytes[ 0 ], bytes[ 1 ], 
				bytes[ 2 ], bytes[ 3 ] );
		
		return result;
	}
	
	private void setNextCursor(ReceiveCursor which) {
		cursor = which;
		onCursorMoved();
	}
	
	private void doNextCursor(final QueueEntry task) {
		resolveStreamReply( task );
	}
	
	/** Cursor of the Response Frame */
	private ReceiveCursor cursor;
	
	// ParserCursor
	private enum ReceiveCursor {
		/** [2]前导字节 */
		FRONT_LEAD,			
		
		/** [1]帧开始标识 */
		FRAME_START_FLAG,	
		/** [6]设备地址 */
		ADDRESS_FIELD,		 
		/** [1]帧停止标识 */
		FRAME_STOP_FLAG,	
		
		/** [1]控制码 */
		CONTROL_CODE,		
		
		/** [1]数据长度标识 */
		DATA_LENGTH,		 
		/** [X]包括数据标识两个字节与默认数据四个字节 */
		DATA,				
		
		/** [1]校验码 */
		CHECK_CODE,			
		
		/** [1]结束标识 */
		END_FLAG,	
		
		DONE;
	}
}