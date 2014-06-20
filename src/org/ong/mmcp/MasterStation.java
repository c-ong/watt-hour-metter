package org.ong.mmcp;

import org.ong.mmcp.Bus.ManageProxy;
import org.ong.mmcp.io.BusTransmitterAgent;


/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface MasterStation {
	void getQueueManager();
	
	// TODO 诸如如类方法应该隐藏 
	
	long enqueue(Operation operation) throws MmcpException;
	
	void setBusTransmitterAgent(BusTransmitterAgent agent);
	
	void setOperationListener(OperationListener listener);

	int obtainBusesCount();

	Bus current();

	void listen();
	
	void deliverResult(Operation which);
	
	ManageProxy getBusesManageProxy();
}