package org.ong.mmcp;

import org.apache.http.util.ExceptionUtils;

public class BroadcastUnsupportedOperationException extends RuntimeException {
    /**
     * Creates a new BroadcastUnsupportedOperationException with a <tt>null</tt> detail message.
     */
    public BroadcastUnsupportedOperationException() {
        super();
    }

    /**
     * Creates a new BroadcastUnsupportedOperationException with the specified detail message.
     *
     * @param message the exception detail message
     */
    public BroadcastUnsupportedOperationException(final String message) {
        super( message );
    }

    /**
     * Creates a new BroadcastUnsupportedOperationException with the specified detail message and cause.
     * 
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public BroadcastUnsupportedOperationException(final String message, final Throwable cause) {
        super( message );
        ExceptionUtils.initCause( this, cause );
    }
}