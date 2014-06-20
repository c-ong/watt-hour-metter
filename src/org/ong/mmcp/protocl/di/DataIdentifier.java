package org.ong.mmcp.protocl.di;

import static org.ong.mmcp.util.ByteUtils.binaryStringToByte;;

/**
 * Marker Interface
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface DataIdentifier {
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
	
	public static final byte MASK = binaryStringToByte( "1111" );
	
	int offset();
	
	int length();
}