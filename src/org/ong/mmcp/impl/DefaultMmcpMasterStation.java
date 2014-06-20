package org.ong.mmcp.impl;

import org.ong.mmcp.AbstractMmcpMasterStation;
import org.ong.mmcp.BluetoothBus;
import org.ong.mmcp.Bus;
import org.ong.mmcp.Bus.ManageProxy;
import org.ong.mmcp.MasterStationService;
import org.ong.mmcp.MmcpException;
import org.ong.mmcp.MmcpMasterStation;
import org.ong.mmcp.Operation;
import org.ong.mmcp.RequestFrame;
import org.ong.mmcp.ResponseInterpreter;
import org.ong.mmcp.io.BusTransmitter;
import org.ong.mmcp.io.Deliver;
import org.ong.mmcp.op.DefaultResponseInterpreter;
import org.ong.mmcp.protocl.BasicMmcpProcessor;
import org.ong.mmcp.queue.QueueExecutor;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class DefaultMmcpMasterStation extends AbstractMmcpMasterStation {
	
	private MasterStationService masterStationService;
	
//	private static MmcpMasterStation singleton;
	
	/*private*/ DefaultMmcpMasterStation() {}
	
	public static MmcpMasterStation from(MasterStationService service) {
		DefaultMmcpMasterStation result = new DefaultMmcpMasterStation();
		result.masterStationService = service;
		
		return result;
	}
	
//	public static MmcpMasterStation getInstance(MasterStationService service) {
//		if ( null == singleton ) {
//			singleton = new DefaultMmcpMasterStation();
//			singleton.masterStationService = service;
//		}
//		
//		return singleton;
//	}

	@Override
	public void getQueueManager() {
		throw new RuntimeException( "Not implemented!" );
	}

	@Override
	public long enqueue(Operation operation) throws MmcpException {
		if ( operation instanceof RequestFrame ) {
			return doEnqueue( operation );
		} else {
			throw new MmcpException( "Your operation#" + operation 
					+ " does not define any Read or Write action!" );
		}
	}

	@Override
	protected BasicMmcpProcessor createMmcpProcessor() {
		return new BasicMmcpProcessor();
	}

	@Override
	protected BusTransmitter createBusTransmitter() {
		// Using Local classes for the secure delivery of data...
		final Deliver deliver = new Deliver() {
			@Override
			public void delivering(byte[] received) {
				DefaultMmcpMasterStation.this.onReceive( received );
			}
		};
		
		return new BusTransmitter( deliver );
	}

	@Override
	protected QueueExecutor createQueueExecutor() {
		return new QueueExecutor( this );
	}

	@Override
	protected ResponseInterpreter createResponseInterpreter() {
		return new DefaultResponseInterpreter();
	}

	@Override
	protected ManageProxy createBusesManageProxy() {
		ManageProxy result = new ManageProxy() {
					
			@Override
			public void shutdown() {
				// TODO
			}
			
			@Override
			public void run(Bus device, boolean secure ) {
				String address = ( (BluetoothBus) device ).address;
				masterStationService.connect( address, secure );
			}
		};
		
		return result;
	}

	@Override
	public void deliverResult(Operation which) {
		masterStationService.deliverResult( which );
	}
}