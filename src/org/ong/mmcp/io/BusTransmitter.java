package org.ong.mmcp.io;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class BusTransmitter implements Sender, Receiver {
	private BusTransmitterAgent agent;
	private Deliver deliver;
	
	public BusTransmitter(Deliver deliver) {
		this.deliver = deliver;
	}
	
	void notifySocketIsOpend() {}
	
	void notifySocketIsClosed() {}
	
	public void setAgent(BusTransmitterAgent agent) {
		this.agent = agent;
		this.agent.bind( this );
	}
	
	@Override
	public void send(byte[] data) {
		agent.transmit( data );
	}
	
	// delivering to master station
	@Override
	public final void receive(byte[] data) {
		deliver.delivering( data );
	}	
}