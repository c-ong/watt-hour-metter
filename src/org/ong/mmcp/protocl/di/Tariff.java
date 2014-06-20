package org.ong.mmcp.protocl.di;

import org.ong.mmcp.protocl.MMCP;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public enum Tariff implements DataIdentifier {
	/** 总能量 */
	TOTAL_ENERGY( MMCP.NIBBLE_0000 ),
	
	/** 费率1 */
	TARIFF_1( MMCP.NIBBLE_0001 ),
	/** 费率2 */
	TARIFF_2( MMCP.NIBBLE_0010 ),
	
	/** 费率k */
	TARIFF_K( MMCP.NIBBLE_1110 ),
	/** 本数据块集合 */
	COLLECTION_OF_LOCAL_DATA_BLOCK( MMCP.NIBBLE_1111 );
	
	Tariff(byte bits) {
		BIT_DI0_D3_TO_D0 = bits;		
	}
	
	public final byte BIT_DI0_D3_TO_D0;

	@Override
	public int offset() {
		return 0;
	}

	@Override
	public int length() {
		return 0;
	}
}