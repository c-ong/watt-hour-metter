package org.ong.mmcp.protocl;


import org.ong.mmcp.MmcpContext;
import org.ong.mmcp.MmcpException;
import org.ong.mmcp.Operation;

public interface MmcpRequestInterceptor {
    /**
     * Processes a operation.
     * On the Master station, this step is performed before the operation is
     * sent to the bus. On the bus side, this step is performed
     * on incoming messages before the message body is evaluated and encoded.
     *
     * @param operation   the operation to preprocess
     * @param context   the context for the operatoin
     *
     * @throws HttpException    in case of a protocol or other problem
     */
    void process(Operation operation, MmcpContext context) 
        throws MmcpException;
}