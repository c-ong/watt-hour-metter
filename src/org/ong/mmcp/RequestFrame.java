package org.ong.mmcp;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public abstract class RequestFrame extends Frame {
	protected abstract byte calculateCheckCode();
	
	protected abstract byte obtainDataLength();
	
	protected abstract byte generateControlCode();
	
	protected abstract byte[] wrapDatasByAdded(byte []original);
}