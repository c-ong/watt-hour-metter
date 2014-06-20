package org.ong.mmcp;

import org.apache.http.protocol.BasicHttpProcessor;
import org.ong.mmcp.Bus.ManageProxy;
import org.ong.mmcp.io.BusTransmitter;
import org.ong.mmcp.io.BusTransmitterAgent;
import org.ong.mmcp.protocl.BasicMmcpProcessor;
import org.ong.mmcp.queue.QueueEntry;
import org.ong.mmcp.queue.QueueExecutor;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public abstract class AbstractMmcpMasterStation extends MmcpBluetoothMasterStation {
	
	private BasicMmcpProcessor mmcpProcessor;
	
	private BusTransmitter busTransmitter;
	private BusTransmitterAgent busTransmitterAgent;
	
	private OperationListener notifier;
	private QueueExecutor queueExecutor;
	private ResponseInterpreter responseInterpreter;
	
	private ManageProxy busesManageProxy;

	// 很多成员在这创建...(创建的方法在下层派生类实现)
	
	protected abstract BasicMmcpProcessor createMmcpProcessor();
	
	protected abstract BusTransmitter createBusTransmitter();
	
	protected abstract QueueExecutor createQueueExecutor();
	
	protected abstract ResponseInterpreter createResponseInterpreter();
	
	protected abstract ManageProxy createBusesManageProxy();
	
	protected BusTransmitter getBusTransmitter() {
		if ( null == busTransmitter ) {
			busTransmitter = createBusTransmitter();
		}
		
		return busTransmitter;
	}
	
	protected final BasicMmcpProcessor getMmcpProcessor() {
		if ( null == mmcpProcessor ) {
			mmcpProcessor = createMmcpProcessor();
		}	
		
		return mmcpProcessor;
	}
	
	protected final ResponseInterpreter getResponseInterpreter() {
		if ( null == responseInterpreter ) {
			responseInterpreter = createResponseInterpreter();
		}
		return responseInterpreter;
	}
	
	protected void notifyOperationReceiveFinished() {
		
	}
	
	protected final QueueExecutor getQueueExecutor() {
		if ( null == queueExecutor ) {
			queueExecutor = createQueueExecutor();
		}
		return queueExecutor;
	}
	
	@Override
	public ManageProxy getBusesManageProxy() {
		if ( null == busesManageProxy ) {
			busesManageProxy = createBusesManageProxy();
		}
		
		return busesManageProxy;
	}
	
	@Override
	public final void setBusTransmitterAgent(BusTransmitterAgent agent) {
		busTransmitterAgent = agent;
		getBusTransmitter().setAgent( busTransmitterAgent );
	}
	
	@Override
	public final void setOperationListener(OperationListener listener) {
		notifier = listener;
	}
	
	protected final long doEnqueue(Operation operation) {
		QueueExecutor executor = getQueueExecutor();
		
		if ( operation instanceof ReadOperation ) {
			( (ReadOperation) operation ).with( this );
		}
		
		long queueId = executor.enqueue( (QueueableOperation) operation );
		
		return queueId;
	}
	
	protected final void onReceive(byte []data) {
		final QueueEntry task = getQueueExecutor().current();
		
		getResponseInterpreter().process( task, data );
	}
	
	public void performTask(QueueEntry task) {
		RequestFrame frame = (RequestFrame) task.getOperation();
		
		task.begin();
		
		frame.prepareMarshall();
		
		getBusTransmitter().send( frame.marshall() );
		
//		task.end(); // For testing...
		
		// Waiting for Response
		
		// Recycle
	}
	
	// 一些 Abstract 方法 createRequestExecutor
	// QueueManager
	// Handler
}