package org.ong.mmcp;

import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;
import cn.hnsi.android.apps.metterreader.R;

import org.ong.mmcp.util.AddressUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Bus {
	
	// TODO 执行多少次、重试次数、连接丢失次数、发送数据统计、接收数据统计、耗时统计、成功统计、无应答统计、错误统计
	
	public interface ManageProxy {
		void shutdown();

		void run(Bus device, boolean secure);
	}
	
	enum State {
		NONE,
		
		CONNECTING,
		
		CONNECTED,
		
		/* 发、收 */
		
		WORKING,
		
		IDLE,
		
		LOST,
		
		CLOSED		
	}
	
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "Bus";
	
	// XXX 这是一个不确切的值
	static public int MAX_SLAVE_STATION = 128;
	
	/*package*/ State state = State.NONE;
	
	protected Map<String, SlaveStation> devices;
	
	// which@bus
	
	protected boolean theseDevicesAreSameOperations = false;
	
	// Execution plan
	protected Set<Operation> batchExecutionPlans;
	
	protected MasterStation masterStation;
	
	protected Bus(MasterStation master) {
		masterStation = master;
		init();		
	}
	
	abstract protected void run();
	
	abstract protected void shutdown();
	
	abstract protected void onStateChanaged(State previous, State now);
	
	/*package*/ void setState(final int state) {
		switch ( state ) {
		   case MasterStationService.STATE_CONNECTED:
           	
			   internalSetState( State.CONNECTED );
			   
               break;
               
           case MasterStationService.STATE_CONNECTING:
        	   
        	   internalSetState( State.CONNECTING );
               
               break;
               
           case MasterStationService.STATE_SCANNING:
           		
           	
           		break;
           	
           /*case BluetoothMasterStationService.STATE_LISTEN:*/
           case MasterStationService.STATE_NONE:
        	   
        	   internalSetState( State.NONE );
           	
               break;
		}
	}
	
	// org.apache.cxf.Bus
	public void sendBroadcast() {
		throw new RuntimeException( "Not implemented!" );
	}
	
	// TODO How can I support batch process. Jun. 11, 2014 17:06
	public void applyAll(Operation operation) {
		throw new RuntimeException( "Not implemented!" );
	}
	
	public boolean isExist(String address) {
		return devices.containsKey( address );
	}
	
	public SlaveStation[] joinDevices(String []addresses) {
		SlaveStation []result = new SlaveStation[ addresses.length ];
		
		for ( int i = 0; i < result.length; i++ ) {
			result[ i ] = joinDevice( addresses[ i ] );			
		}
		
		return result;
	}
	
	public SlaveStation joinDevice(String address) {
		SlaveStation result = null;
		
		if ( AddressUtils.validate( address ) ) {
			
			if ( isExist( address ) )
				result = devices.get( address );
			else 
				result = internalJoin( address );
				
		} else {
			throw new IllegalArgumentException( "Invalid device address: " 
					+ address );
		}
		
		return result;
	}
	
	/*package*/ void pushExecutionPlansToQueue() {
		int counter = 0;
		try {
			for ( SlaveStation slave : devices.values() ) {
				for ( Operation op : slave.executionPlans ) {
					masterStation.enqueue( op );
					counter++;
				}			
			}
		} catch (MmcpException e) {
			e.printStackTrace();
		}
		
		// TODO 队列的结束意味着某个 Bus 上的操作已经处理完毕，可准备连接下一 Bus
		
		if ( DEBUG ) Log.i( LOG_TAG, String.format( "Pushe %s operation to queue.", counter ) ); 
	}
	
	/*package*/ void setupOperations(SlaveStation target, Operation... operations) {
		if ( isExist( target.address ) ) {
			target.withOperations( operations );
		} else {
			throw new IllegalArgumentException( "The device " + target 
					+ " which is not belong to this Bus " + this + "." );
		}	
	}
	
	/* -------------------------------------------------------- */
	
	private SlaveStation internalJoin(String address) {
		SlaveStation result = null;
		
		SlaveStation device = new SlaveStation( this, address );
		
		result = devices.put( address, device );
		
		if ( null == result ) {
			// New device
			result = device;
		} else {
			// That's insignificance operation
		}
		
		return result;
	}
	
	private void internalSetState(State now) {
		State old = state;
		this.state = now;
		
		fireOnStateChanged( old, now );
	}
	
	private void fireOnStateChanged(State previous, State now) {
		onStateChanaged( previous, now );
	}
	
	private void init() {
		devices = new HashMap<String, SlaveStation>( MAX_SLAVE_STATION >> 4 );
	}
}