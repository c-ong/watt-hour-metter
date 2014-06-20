package org.ong.mmcp.io;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public abstract class BusTransmitterAgent {
	private BusTransmitter busTransmitter;
	private GuardReceiver guard;
	
	public abstract boolean isOpen();
	
	public abstract boolean onTranact(byte []data);
	
	public abstract boolean isClosed();
	
	public final void notifyChannelIsOpend() {
		busTransmitter.notifySocketIsOpend();
	}
	
	public final void notifyChannelIsClosed() {
		busTransmitter.notifySocketIsClosed();
	}
	
	void transmit(byte []data) {
		onTranact( data );
	}
	
	void bind(BusTransmitter transmitter) {
		busTransmitter = transmitter;
	}
	
	public final Receiver getReceiver() {
		if ( null == guard ) {
			guard = new GuardReceiver( busTransmitter );
		}
		
		return guard;
	}
}