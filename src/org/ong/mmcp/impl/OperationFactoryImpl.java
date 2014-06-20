package org.ong.mmcp.impl;

import org.ong.mmcp.OperationFactory;
import org.ong.mmcp.SlaveStation;
import org.ong.mmcp.op.PowerEnergyReadOperatoin;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public final class OperationFactoryImpl extends OperationFactory {

	@Override
	public PowerEnergyReadOperatoin newEnergyReadOperation(SlaveStation slave) {
		return new PowerEnergyReadOperatoin( slave.address );
	}
}