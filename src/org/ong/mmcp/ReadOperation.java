package org.ong.mmcp;

import org.ong.mmcp.protocl.MMCP;
import org.ong.mmcp.protocl.di.Classification;
import org.ong.mmcp.protocl.di.PowerDirection;
import org.ong.mmcp.protocl.di.Tariff;
import org.ong.mmcp.protocl.di.TimeDomain;
import org.ong.mmcp.protocl.di.Type;
import org.ong.mmcp.queue.QueueEntry;
import org.ong.mmcp.util.AddressUtils;
import org.ong.mmcp.util.ByteUtils;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public abstract class ReadOperation<T extends org.ong.mmcp.op.IResult> extends AbstractReadOperation 
		implements org.ong.mmcp.op.ResultGenerator<T> {
	
	protected final String address;
	
	protected Type type;
	
	protected TimeDomain _TimeDomain;
	
	protected Classification classific;
	
	protected PowerDirection direction;
	
	protected Tariff tariff;
	
	private MmcpMasterStation masterStation;		
	
//	protected abstract void onReceiveReply(byte []reply);
	
	protected abstract void receiveReply(final QueueEntry task, byte []reply);
	
	protected final void with(MmcpMasterStation masterStation) {
		this.masterStation = masterStation;
	}
	
	protected final void sendReceiveResponsFinishedNotice() {
		if ( null == masterStation ) throw new RuntimeException( "Not yet enqueue." );
		
		( (AbstractMmcpMasterStation) masterStation ).notifyOperationReceiveFinished();
	}
	
	protected void setType(Type type) {
		this.type = type;
	}
	
	protected ReadOperation(String address) {
		this.address = address;
	}
	
	@Override
	protected final byte[] wrapDatasByAdded(byte []original) {
		byte []wrapped = new byte[ original.length ];
		System.arraycopy( original, 0, wrapped, 0, original.length );
		
		for ( int i = 0; i < wrapped.length; i++ ) {
			wrapped[ i ] += SEED;
		}
		
		return wrapped;
	}

	@Override
	protected final byte[] wrapDatas(byte []original) {
		return wrapDatasByAdded( original );
	}

	@Override
	protected final byte calculateCheckCode() {
		byte []sample = buffer();
		// Length of current Frame
		final int frameLength = calculateLength();
		
		return ByteUtils.computeParityCheck( sample, 
				MMCP.FRONT_LEADING_BYTES_LENGTH, 
				frameLength - MMCP.CHECK_CODE_LENGTH - MMCP.END_FLAG_LENGTH );
	}	
	
	@Override
	/*protected */final void prepareMarshall() {
		////////////
		// Step 1:
		////////////
		allocate( calculateLength() );
		
		////////////
		// Step 2: settle bytes stream
		////////////
		put( MMCP.FRONT_LEADING_BYTE ); 		// Front-leading bytes(Two byte)
		put( MMCP.FRONT_LEADING_BYTE );
		
		put( MMCP.FRAME_START_FLAG_BYTE ); 		// Frame start flag
		
		put( AddressUtils.encode( address ) );	// Address
		
		put( MMCP.FRAME_STOP_FLAG_BYTE ); 		// Frame stop flag
		
		put( generateControlCode() );			// Control code
		
		put( obtainDataLength() );				// Data length
		
		if ( 0 != obtainDataLength() ) {		// Data
			// 原如 DI
			byte []di = generateDataIdentifier();
			
			// D0 是字节的最低有效位，D7 是字节的最高有效位。先传低位，后传高位。
			di = ByteUtils.reverse( di );
			// 加 33H 
			di = wrapDatas( di );
			
			put( di );
		}
		
		put( calculateCheckCode() );			// Check code
		
		put( MMCP.END_FLAG_BYTE ); 				// End flag
	}
	
	@Override
	/*protected */final byte[] marshall() {
		return buffer();
	}
	
	/* ---------------------------------------------------------------------- */
	
//	protected void receiveReply(byte []reply) {
//		ensureReceiver();
//		
//		receiver.append( reply, 0, reply.length );
//		onReceiveReply( reply );
//	}
	
	protected final void ensureReceiver(final int estimateCapacity) {
		if ( null == receiver ) {
			receiver = new ResponseFrame();
			receiver.initReceivePool( estimateCapacity );
		}
	}
	
	protected final void putIntoBuffer(byte []reply) {
		receiver.append( reply, 0, reply.length );
	}
	
	protected ResponseFrame receiver;
}