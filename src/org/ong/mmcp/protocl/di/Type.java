package org.ong.mmcp.protocl.di;

import org.ong.mmcp.protocl.MMCP;

/**
 * Type
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public enum Type implements DataIdentifier {
	
	/** 电能量 */
	POWER_ENERGY	( MMCP.NIBBLE_1001 ),
	
	/** 最大需量 */
	MAX_DEMAND		( MMCP.NIBBLE_1010 ),
	
	/** 变量 */
	VARIABLE		( MMCP.NIBBLE_1011 ),
	
	/** 参变量 */
	PARAMETRIC_VARIABLE	( MMCP.NIBBLE_1100 ),
	
	/** 负荷曲线 */
	LOAD_CURVE			( MMCP.NIBBLE_1101 ),
	
	/** 用户自定义 */
	USER_SELF_DEFINED	( MMCP.NIBBLE_1110 ),
	
	/** 保留 */
	RESERVED			( MMCP.NIBBLE_1111 );
	
	public int offset() { return 0; }
	
	public int length() { return 0; }
	
	Type(byte bits) {
		BIT_DI1_D7_TO_D4 = (byte) (bits << 4);
	}
	
	public final byte BIT_DI1_D7_TO_D4;
	
	// 询问写入位置 offset
	// 询问位长度
	// 询问是否为保留设计或未实现的
	
//	boolean needsArgument();
//	
//	boolean obtainArgsNumber();
}