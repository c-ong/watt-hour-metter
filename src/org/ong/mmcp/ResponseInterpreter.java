package org.ong.mmcp;

import org.ong.mmcp.queue.QueueEntry;

public abstract class ResponseInterpreter {
	public abstract void process(QueueEntry task, byte[] reply);
}