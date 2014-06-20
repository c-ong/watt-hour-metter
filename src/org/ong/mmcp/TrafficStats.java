package org.ong.mmcp;

public class TrafficStats {

	//HttpTransportMetricsImpl->bytesTransferred
	
    private long bytesTransferred = 0;
    
    public TrafficStats() {
    }
    
    public long getBytesTransferred() {
        return bytesTransferred;
    }

    public void setBytesTransferred(long count) {
        bytesTransferred = count;
    }

    public void incrementBytesTransferred(long count) {
        bytesTransferred += count;
    }

    public void reset() {
        bytesTransferred = 0;
    }
}