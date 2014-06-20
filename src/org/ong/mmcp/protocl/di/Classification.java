package org.ong.mmcp.protocl.di;

import org.ong.mmcp.protocl.MMCP;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public enum Classification implements DataIdentifier {
	/** 有功 */
	ACTIVE ( MMCP.NIBBLE_0000 ),
	
	/** 无功 */
	REACTIVE ( MMCP.NIBBLE_0001 ),
	
	/** 保留 */
	RESERVED ( MMCP.NIBBLE_0010 ),
	
	/** 集合 */
	COLLECTION ( MMCP.NIBBLE_0011 );

	Classification(byte bits) {
		BIT_DI1_D1_TO_D0 = bits;
	}
	
	public final byte BIT_DI1_D1_TO_D0;
	
	@Override
	public int offset() {
		return 0;
	}

	@Override
	public int length() {
		return 0;
	}
}