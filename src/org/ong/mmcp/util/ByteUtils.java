package org.ong.mmcp.util;

/**
 * ByteUtils
 * 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 * @created 2013/11/18 09:14:15 GMT + 08:00
 * @updated 2013/11/19 14:52:29 GMT + 08:00
 */
public class ByteUtils {
	// TODO 添加注释...
	
	static final char []HEXS = new char[] { 
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
		'A', 'B', 'C', 'D', 'E', 'F' };
	
	public static byte binaryStringToByte(String binary) {
		return Integer.valueOf( binary, 2 ).byteValue();
	}
	
	public static void arrayCopy(byte []src, int srcPos, byte []dest,
            int destPos, int length) {
		if ( length - destPos > src.length - srcPos ) return;
		
		for ( int i = destPos, j = srcPos; i < destPos + length; i++, j++ ) {
			dest[ i ] = src[ j ];
		}
	}

	public static String bytesToHex(byte []bytes) {
		return toHexString( bytes, 0, bytes.length );
//	    StringBuilder hex = new StringBuilder( 2 * bytes.length );
//	    for ( final byte b : bytes ) {
//	        hex
//	        	.append( HEXES[ (b & 0xF0) >>> 4 ] )
//	            .append( HEXES[ b & 0x0F ] );
//	    }
//	    return hex.toString();
	}
	
    private static String toHexString(byte[] data, int offset, int length) {
    	StringBuilder s = new StringBuilder( length * 2 );
        int end = offset + length;
        
        int high_nibble;
        int low_nibble;

        for ( int i = offset; i < end; i++ ) {
            high_nibble = ( data[ i ] & 0xF0 ) >>> 4;
            low_nibble = ( data[ i ] & 0x0F );
            
            s.append( HEXS[ high_nibble ] );
            s.append( HEXS[ low_nibble ] );
        }

        return s.toString();
    }
    
    private static String toHexString(byte byt) {
    	String result = null;
    	
        int high_nibble;
        int low_nibble;
        
        high_nibble = ( byt & 0xF0 ) >>> 4;
        low_nibble = ( byt & 0x0F );
        
        result = "" + HEXS[ high_nibble ] + HEXS[ low_nibble ];
    	
    	return result;
    }
    
    public static String[] toHexStringArray(byte[] data, int offset, int length) {
    	String []result = new String[ length ];
    	StringBuilder s = new StringBuilder( length * 2 );
        int end = offset + length;
        
        int high_nibble;
        int low_nibble;

        for ( int i = offset, j = 0, k = 0; i < end; i++, j++, k+=2 ) {
            high_nibble = ( data[ i ] & 0xF0 ) >>> 4;
            low_nibble = ( data[ i ] & 0x0F );
            
            s.append( HEXS[ high_nibble ] );
            s.append( HEXS[ low_nibble ] );
            
            result[ j ] = s.substring( k );
        }
        
        s.delete( 0, s.length() - 1 );
        s = null;

        return result;
    }
    
//	As a standard number (default):	 					 101.01 
//	Leading/trailing zeros, to match hexadecimal:	 	0101.0100
	public static String dumpHex(byte[] bytes) {
		return bytesToHex( bytes );
		
//		StringBuilder buffer = new StringBuilder();
//		for ( byte b : bytes ) {
//			buffer.append( String.format( " 0x%x", b ) );
//		}
//		System.out.println( buffer );
	} 
	
    public static byte[] toHexByte(String str, int offset, int length) {
        byte[] data = new byte[ ( length - offset ) * 2 ];
        
        int end = offset + length;
        
        int high_nibble;
        int low_nibble;

        for ( int i = offset; i < end; i++ ) {
            char ch = str.charAt( i );
            high_nibble = (ch & 0xF0) >>> 4;
            low_nibble 	= ch & 0x0F;
            
            data[ i] 		= (byte) high_nibble;
            data[ i + 1 ] 	= (byte) low_nibble;
        }
       return data;
    }
    
    private static final int DEDUCTING_OVER_256 = 0x00FF;
    
    /**
     * 计算偶校验位。
     * <p>
     * 校验码 CS：从帧起始符开始到校验码之前的所有各字节的模 256 的和，即各字节二
	 * 进制算术和，不计超过 256 的溢出值。</p>
     * @param sequence
     * @param from
     * @param to
     * @return
     */
    public static byte computeParityCheck(byte []sequence, int from, int to) {
    	byte result;
    	int sum = 0;
    	for ( int i = from; i < to; i++ ) {
    		sum += sequence[ i ];
    	}
    	
    	result = (byte) (sum & DEDUCTING_OVER_256);
    	
    	return result;
    }
    
	/**
	 * 
	 * @param original
	 * @return
	 */
	public static byte[] reverse(byte []original) {
		byte []result = new byte[ original.length ];
		
		final int stop = original.length >> 1;
		byte x, y;
		
		for ( int i = 0; i < stop; i++ ) {
			x = original[ i ];
			y = original[ original.length - i - 1 ];
			
			if ( 0 != (x ^ y) ) {
				x ^= y;
				y ^= x;
				x ^= y;
			}
			
			result[ i ] = x;
			result[ original.length - i - 1 ] = y;
		}
		if ( 1 == original.length % 2 ) {
			result[ stop ] = original[ stop ];
		}
		
		return result;
	}
    
	public static byte[] hexStringToBytes(String hex) {
		if ( 0 != hex.length() % 2 ) throw new IllegalArgumentException( 
				hex + " not be a valid HEX String!" );
		
		byte []result = new byte[ hex.length() >> 1 ];
		
        int high_nibble;
        int low_nibble;
		
		for ( int i = 0, j = 0; i < result.length; i++,j += 2 ) {
			high_nibble = Character.digit( hex.charAt( j ), 16 ) << 4;
			low_nibble 	= Character.digit( hex.charAt( j + 1 ), 16 );
			
			result[ i ] = (byte) (high_nibble + low_nibble);
		}
		
		return result;
	}
	
//    final void reverse0() {
//        if (count < 2) {
//            return;
//        }
//        if (!shared) {
//            int end = count - 1;
//            char frontHigh = value[0];
//            char endLow = value[end];
//            boolean allowFrontSur = true, allowEndSur = true;
//            for (int i = 0, mid = count / 2; i < mid; i++, --end) {
//                char frontLow = value[i + 1];
//                char endHigh = value[end - 1];
//                boolean surAtFront = allowFrontSur && frontLow >= 0xdc00
//                        && frontLow <= 0xdfff && frontHigh >= 0xd800
//                        && frontHigh <= 0xdbff;
//                if (surAtFront && (count < 3)) {
//                    return;
//                }
//                boolean surAtEnd = allowEndSur && endHigh >= 0xd800
//                        && endHigh <= 0xdbff && endLow >= 0xdc00
//                        && endLow <= 0xdfff;
//                allowFrontSur = allowEndSur = true;
//                if (surAtFront == surAtEnd) {
//                    if (surAtFront) {
//                        // both surrogates
//                        value[end] = frontLow;
//                        value[end - 1] = frontHigh;
//                        value[i] = endHigh;
//                        value[i + 1] = endLow;
//                        frontHigh = value[i + 2];
//                        endLow = value[end - 2];
//                        i++;
//                        end--;
//                    } else {
//                        // neither surrogates
//                        value[end] = frontHigh;
//                        value[i] = endLow;
//                        frontHigh = frontLow;
//                        endLow = endHigh;
//                    }
//                } else {
//                    if (surAtFront) {
//                        // surrogate only at the front
//                        value[end] = frontLow;
//                        value[i] = endLow;
//                        endLow = endHigh;
//                        allowFrontSur = false;
//                    } else {
//                        // surrogate only at the end
//                        value[end] = frontHigh;
//                        value[i] = endHigh;
//                        frontHigh = frontLow;
//                        allowEndSur = false;
//                    }
//                }
//            }
//            if ((count & 1) == 1 && (!allowFrontSur || !allowEndSur)) {
//                value[end] = allowFrontSur ? endLow : frontHigh;
//            }
//        } else {
//            char[] newData = new char[value.length];
//            for (int i = 0, end = count; i < count; i++) {
//                char high = value[i];
//                if ((i + 1) < count && high >= 0xd800 && high <= 0xdbff) {
//                    char low = value[i + 1];
//                    if (low >= 0xdc00 && low <= 0xdfff) {
//                        newData[--end] = low;
//                        i++;
//                    }
//                }
//                newData[--end] = high;
//            }
//            value = newData;
//            shared = false;
//        }
//    }
	
	private ByteUtils() {}
}