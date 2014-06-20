package org.ong.mmcp.io;

/** THIS IS BAD DESIGN */
public class GuardReceiver implements Receiver {
	final BusTransmitter busTransmitter;
	
	GuardReceiver(BusTransmitter busTransmitter) {
		this.busTransmitter = busTransmitter;		
	}

	@Override
	public void receive(byte[] data) {
		busTransmitter.receive( data );
	}
}