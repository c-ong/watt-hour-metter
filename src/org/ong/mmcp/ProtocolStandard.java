package org.ong.mmcp;

import java.io.Serializable;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class ProtocolStandard implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4988996194011528440L;
	
	protected final String protocol;
	
	public ProtocolStandard() {
		protocol = "DL/T 654-1997";
	}
}