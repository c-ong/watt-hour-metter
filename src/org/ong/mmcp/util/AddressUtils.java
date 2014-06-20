package org.ong.mmcp.util;

import org.ong.mmcp.protocl.MMCP;

import java.util.regex.Pattern;

/**
 * AddressUtils
 * 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 * @created 2013/11/18 09:14:15 GMT + 08:00
 * @updated 2013/11/21 09:07:11 GMT + 08:00
 */
public class AddressUtils {
	
	private static Pattern PATTERN_SLAVE_ADDR = Pattern.compile( "\\d{10,12}+" );
		
	public static boolean validate(String address) {
		return PATTERN_SLAVE_ADDR.matcher( address ).find();
	}
	
	/**
	 * 对电表的地址进行编码，如: 1200000000 将会被以 Byte array 形式返回 HEX 地址
	 * 为 { 0x00, 0x00, 0x00, 0x00, 0x12, 0xAA } 地址最长为 12 位，不足 12 位则补
	 * 足 12 位，以 HEX 存储则占用 6 个 Byte。
	 * @param address
	 * @return
	 */
	public static byte[] encode(String address) {
		final int size = MMCP.ADDRESS_LENGHT >> 1;
		byte []full = new byte[ size ];
		
		byte []bytes = ByteUtils.hexStringToBytes( address );
		
		if ( bytes.length < full.length ) {
			ByteUtils.arrayCopy( bytes, 0, 
					full, size - bytes.length, bytes.length );
			
			// 对不足 6 字节的地址补上 0xAA
			for ( int i = 0; i < size - bytes.length; i++ ) {
				full[ i ] = MMCP.ADDRESS_REST_FILLED;			
			}			
		} else {
			ByteUtils.arrayCopy( bytes, 0, full, 0, size );
		}
		
		bytes = null;
				
		return ByteUtils.reverse( full );		
	}
	
	/**
	 * 决断是否为 Broadcast 地址。
	 * @param address
	 * @return
	 */
	public static boolean isBroadcastAddress(String address) {
		return MMCP.BROADCAST_ADDRESS.equals( address );
	}
}