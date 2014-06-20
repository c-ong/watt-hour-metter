package org.ong.mmcp.op;

import org.ong.mmcp.Operation;
import org.ong.mmcp.ResponseInterpreter;
import org.ong.mmcp.queue.QueueEntry;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class DefaultResponseInterpreter extends ResponseInterpreter {
	@Override
	public void process(QueueEntry task, byte[] reply) {
		final Operation op = (Operation) task.getOperation();
		
		if ( PowerEnergyReadOperatoin.class.isInstance( op ) ) {
			
			( (PowerEnergyReadOperatoin) op ).receiveReply( task, reply );
			
		} else if ( null == reply ) {
			
		} else {
			
		}
	}
}