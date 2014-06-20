package org.ong.mmcp;

import java.nio.ByteOrder;

import org.ong.mmcp.protocl.MMCP;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public abstract class Frame {
	
	protected static final byte SEED = MMCP.DATA_BYTE_TRANSFER_PROCESS_SEED;
	
    // Invariants: mark <= position <= limit <= capacity
    private int mark = -1;
    
	private int position = 0;
  	private int limit;
  	private int capacity;
  	
  	private byte[] buffer;
  	
	private boolean bigEndian = true;
	
	private boolean nativeByteOrder = ByteOrder.nativeOrder() 
			== ByteOrder.BIG_ENDIAN;
	
	/** 
	 * 估算 Framge 长度。
	 * @return
	 */
	protected abstract int calculateLength();
  	
    /** Returns the raw bytes of the frame. */
    /*protected */abstract byte[] marshall();
    
    /*protected */abstract void prepareMarshall();
    
    protected abstract byte[] wrapDatas(byte []original);
    
    /**
     * 两 Byte 的数据标识。
     * @return
     */
    protected abstract byte[] generateDataIdentifier();
	
//	protected abstract int allocate(int capacity);
//	
//	protected abstract boolean grow(int grow);
//	
//    protected abstract int byteAt(int index);
//	
//    protected abstract Packer rewind();
//    
//    protected abstract Packer clear();
//    
//    protected abstract boolean hasRemaining();
//    
//	protected abstract int remaining();
    
    protected final byte[] buffer() {
//    	rewind(); // It's so bad
        return buffer;
    }
    
    protected Frame append(byte b) {
    	final int newPosition = position + 1;
    	
    	buffer[ position ] = b;
    	
    	position = newPosition;
    	
    	return this;
    }
    
    protected Frame append(byte []src, int offset, int length) {
    	final int newPosition = position + length;
    	
    	System.arraycopy( src, offset, buffer, position, length );     	
    	
    	position = newPosition;
    	
    	return this;
    }
	
	protected Frame put(int index, byte b) {
		buffer[ index ] = b;
		return this;
	}
	
	protected Frame put(byte b) {
		append( b );
		return this;
	}
	
	protected Frame put(byte []bytes) {
		append( bytes, 0, bytes.length );
		return this;
	}
	
	protected Frame put(byte []src, int offset, int length) {
		throw new java.lang.RuntimeException( "Not implemented!" );
//		return this;
	}
	
	protected final void allocate(int capacity) {
		if ( null != buffer ) return;
		
		this.capacity = capacity;
		buffer = new byte[ capacity ];
	}
	
	protected boolean grow(int grow) {
		throw new java.lang.RuntimeException( "Not implemented!" );
//		return false;
	}
	
	private void expand(int newlen) {
		throw new java.lang.RuntimeException( "Not implemented!" );
	}

	protected final byte byteAt(int index) {
		return buffer[ index ];
	}
	
	public final Frame rewind() {
		position = 0;
//	    mark = -1;
//		throw new java.lang.RuntimeException( "Not implemented!" );
		
	    return this;
	}
	
    protected boolean hasRemaining() {
    	return position < limit;
    }
    
	public final int remaining() {
		return limit - position;
	}
	
    public final Frame clear() {
	    position = 0;
	    limit = capacity;
	    mark = -1;
	    
	    return this;
    }
    
    protected final int position() {
    	return position;
    }
    
    protected final int capacity() {
    	return capacity;
    }
    
	protected final ByteOrder order() {
		return bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
	}

	/**
	 * Modifies this buffer's byte order.
	 * Parameters:   
	 * bo The new byte order, either BIG_ENDIAN or LITTLE_ENDIAN   
	 */
	protected final Frame order(ByteOrder byteOrder) {
        bigEndian = byteOrder == ByteOrder.BIG_ENDIAN;
        nativeByteOrder =
            ( bigEndian == ( ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ) );
        
        return this;
	}
//		
//		// Unchecked accessors, for use by Operation classes
//		//
//		abstract byte _get(int i);              // package-private
//		abstract void _put(int i, byte b);   	// package-private
}