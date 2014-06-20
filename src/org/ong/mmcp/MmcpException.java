package org.ong.mmcp;

import org.apache.http.util.ExceptionUtils;

public class MmcpException extends Exception {
	private static final long serialVersionUID = -1056235561788577364L;
	
    /**
     * Creates a new HttpException with a <tt>null</tt> detail message.
     */
    public MmcpException() {
        super();
    }

    /**
     * Creates a new HttpException with the specified detail message.
     *
     * @param message the exception detail message
     */
    public MmcpException(final String message) {
        super( message );
    }

    /**
     * Creates a new HttpException with the specified detail message and cause.
     * 
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public MmcpException(final String message, final Throwable cause) {
        super( message );
        ExceptionUtils.initCause( this, cause );
    }
}