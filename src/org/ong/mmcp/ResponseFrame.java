package org.ong.mmcp;

import org.ong.mmcp.util.ByteUtils;

import cn.hnsi.android.apps.metterreader.BuildConfig;

import android.util.Log;

public class ResponseFrame extends Frame {
	
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "ResponseFrame";
	private int lastReadPosition;
	
	protected void initReceivePool(final int estimateCapacity) {
		if ( null == buffer() ) allocate( estimateCapacity ); 
	}
	
	public boolean hasRead(final int offset, final int length) {
		boolean fully = false;
		final int expect = offset + length;
		if ( capacity() < expect ) 
			throw new java.lang.IndexOutOfBoundsException();
		
		fully = position() >= expect;
		
		return fully; 
	}
	
	public boolean hasRead(final int index) {
		if ( capacity() < index ) 
			throw new java.lang.IndexOutOfBoundsException();
		
		return position() >= index; 
	}
	
	public boolean hasUnresolvedReply() {
		if ( DEBUG ) Log.i( LOG_TAG, "## hasUnresolvedReply: position = " + position() 
				+ " last = " + lastReadPosition );
		return 0 == position() ? false : position() - 1 > lastReadPosition;
	}
	
	public int unresovedRemaining() {
		final int result = position() - lastReadPosition - 1;
		if ( DEBUG ) Log.i( LOG_TAG, "## Unresoved remaining: " + result );
		
		return result;
	}
	
	public byte read(final int index) {
		if ( lastReadPosition < index ) updateLastReadPosition( index ); 
		return byteAt( index );
	}
	
	public byte[] readSegment(final int offset, final int length) {
		byte []segment = new byte[ length ];
		
		ByteUtils.arrayCopy( buffer(), offset, segment, 0, length );
		
		final int lastPos = offset + length - 1;
		
		if ( lastReadPosition < lastPos ) updateLastReadPosition( lastPos );
		
		return segment;
	}
	
	private void updateLastReadPosition(final int last) {
		lastReadPosition = last;
	}
	
	public byte[] raw() {
		return buffer();
	}
	
	/* --------------------------------------------- */
	
	protected byte obtainParityCheckByte() {
		return 0x00;
	}

	@Override
	protected int calculateLength() {
		return 0;
	}

	@Override
	byte[] marshall() {
		return null;
	}

	@Override
	void prepareMarshall() {
		
	}

	@Override
	protected byte[] wrapDatas(byte[] original) {
		return null;
	}

	@Override
	protected byte[] generateDataIdentifier() {
		return null;
	}
}