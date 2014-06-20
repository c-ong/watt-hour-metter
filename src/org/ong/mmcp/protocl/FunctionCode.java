package org.ong.mmcp.protocl;

import static org.ong.mmcp.protocl.MMCP.*;

/**
 * FunctionCode 功能码占 5 Bit。
 */
public class FunctionCode {
	// FIXME These be should is Enum type...
	
	// 功能决定是否于 DATA 域设置参数项，如：写设备地址、更改通信速率、修改密码
	public static final byte RESERVED 				= NIBBLE_0000;
	public static final byte READ_DATA 				= NIBBLE_0001;
	public static final byte READ_SUBSEQUENT_DATA 	= NIBBLE_0010;
	public static final byte REREAD_DATA 				= NIBBLE_0011;
	public static final byte WRITE_DATA 				= NIBBLE_0100;
	public static final byte CORRECTING_TIME_BROADCAST 	= NIBBLE_1000;
	public static final byte WRITE_DEVICE_ADDRESS 	= NIBBLE_1010;
	public static final byte CHANGE_COMMS_SPEED 		= NIBBLE_1100;
	public static final byte CHANGE_PASSWORD 			= NIBBLE_1111;
	public static final byte CLEAR_MAXIMUM_DEMAND 	= (byte) (NIBBLE_1000 << 1);
}