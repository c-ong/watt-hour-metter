package org.ong.mmcp;

import org.ong.mmcp.impl.OperationFactoryImpl;
import org.ong.mmcp.op.PowerEnergyReadOperatoin;

import android.graphics.Bitmap;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public abstract class OperationFactory {
	
	private static OperationFactoryImpl defaultInstance;
	
	public final static OperationFactory getInstance() {
		if ( null == defaultInstance ) {
			defaultInstance = new OperationFactoryImpl(); 
		}
		
		return defaultInstance;
	}
	
	// begin
	public abstract PowerEnergyReadOperatoin newEnergyReadOperation(SlaveStation slave);
	
//	abstract Frame decodeFile();
//	
//	abstract Frame decodeStreame();
//	
//	abstract Frame decodeResource();
//	
//	abstract Thread newThread();
	
//	SocketImpl createSocketImpl();
	
//	abstract Frame generateFrame();
	
//	public final <T> T fromInputStream(InputStream inputStream, Class<T> destinationClass)
//	      throws IOException {
//	    return createJsonParser( inputStream ).parseAndClose( destinationClass );
//	}
}