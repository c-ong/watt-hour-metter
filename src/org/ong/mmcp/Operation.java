package org.ong.mmcp;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public interface Operation {
	// packable
	
	/** Cancel the operation, if it has not been processed. */
	boolean cancel();
}