package org.ong.mmcp.protocl.di;

import org.ong.mmcp.protocl.MMCP;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public enum PowerDirection implements DataIdentifier{
	/** 正向电能 */
	POSITIVE_ENERGY				( MMCP.NIBBLE_0001 ),
	
	/** 反向电能 */
	NEGATIVE_ENERGY				( MMCP.NIBBLE_0010 ),
	
	/** 一象限无功 */
	REACTIVE_ENERGY_QUADRANT_1ST( MMCP.NIBBLE_0011 ),
	/** 四象限无功 */
	REACTIVE_ENERGY_QUADRANT_4TH( MMCP.NIBBLE_0100 ),
	/** 二象限无功 */
	REACTIVE_ENERGY_QUADRANT_2ND( MMCP.NIBBLE_0101 ),
	/** 三象限无功 */
	REACTIVE_ENERGY_QUADRANT_3RD( MMCP.NIBBLE_0110 ),
	
	/** 保留 */
	RESERVED( MMCP.NIBBLE_0111 ),
	/** 集合 */
	COLLECTION( MMCP.NIBBLE_1111 );
	
	PowerDirection(byte bits) {
		BIT_DI0_D7_TO_D4 = (byte) (bits << 4);
	}
	
	public final byte BIT_DI0_D7_TO_D4;
	
	@Override
	public int offset() {
		return 0;
	}

	@Override
	public int length() {
		return 0;
	}
}