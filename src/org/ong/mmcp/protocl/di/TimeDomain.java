package org.ong.mmcp.protocl.di;

import org.ong.mmcp.protocl.MMCP;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public enum TimeDomain implements DataIdentifier {
	/** 当前 */
	CURRENTLY				( MMCP.NIBBLE_0000 ),
	
	/** 上月 */
	LAST_MONTH				( MMCP.NIBBLE_0001 ),
	
	/** 上上月 */
	MONTH_BEFORE_LAST_MONTH	( MMCP.NIBBLE_0010 ),
	
	/** 集合 */
	COLLECTION				( MMCP.NIBBLE_0011 );
	
	TimeDomain(byte bits) {
		BIT_D3_TO_D2 = (byte) (bits << 2);
	}
	
	public final byte BIT_D3_TO_D2;	

	@Override
	public int offset() {
		return 0;
	}

	@Override
	public int length() {
		return 0;
	}
}
