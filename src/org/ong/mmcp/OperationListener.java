package org.ong.mmcp;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface OperationListener {
	// obtainResult
	void onReceivedReply(long queueId);
	
	void onTimedout(long queueId, Operation op);
}