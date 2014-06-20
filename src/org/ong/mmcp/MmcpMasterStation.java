package org.ong.mmcp;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;

import org.ong.mmcp.Bus.State;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.PooledConnection;

public abstract class MmcpMasterStation extends MmcpContext 
		implements MasterStation {
	
	private static final String LOG_TAG = "MmcpMasterStation";
	private static final boolean DEBUG = BuildConfig.DEBUG;
	
	protected Map<String, Bus> buses;
	// XXX 这可能随时会发生元素被移出的情况
	protected Set<String> futureBuses;
	
	private int loopIndex = -1;	
	
	private Bus current;
	
	// waitUntilConnected
	private Object waitUntilFoundBusLock = new Object();
	
	private boolean first = true;
	
	private boolean forceKill = false;
	
	private int wasDoneCounter = 0;
	
	private boolean looping = false;
	
	@Override
	public int obtainBusesCount() {
		return buses.size();
	}
	
	@Override
	public Bus current() {
		if ( BuildConfig.DEBUG ) Log.i( "MmcpMasterStation", "++ current: " + current ); 
			
		return current;	
	}
	
	@Override
	public void listen() {
		synchronized ( waitUntilFoundBusLock ) {
			if ( looping ) {
				return;
			}			
		}
		
		looping = true;
			
		prepare();
	}
	
	/*package*/ boolean hasNext() {		
		final int busesSize = buses.size();
		
		return wasDoneCounter != busesSize 
				&& busesSize > futureBuses.size() 
				&& loopIndex < busesSize;
	}
	
	/*package*/ Bus findNext() {
		Bus next = null;
		
		// 
		final int currentIdx = loopIndex;
		Iterator<Bus> iterator = buses.values().iterator();
		int i = 0;
		Bus temp = null;
		
		while ( iterator.hasNext() ) {
			if ( i > currentIdx ) {
				temp = iterator.next();
				
				// TODO When a connection is lost
				if ( State.CLOSED == temp.state ) {
					wasDoneCounter++;
					
				} else { 
					
					next = temp;
					break;
				}				
			}
			
			temp = iterator.next();
			
			if ( State.CLOSED == temp.state ) {
				wasDoneCounter++;
			}
			
			i++;
		}
		
		loopIndex = i;
		
		if ( buses.size() == (loopIndex + 1 ) ) {
//			resetLoopIndex();
		}
		
		if ( DEBUG ) Log.i( LOG_TAG, "++ findNext: " + next ); 
		
		return next;
	}
	
	public boolean getToReady() {
		return false;
	}
	
	/* -------------------------------------------------------- */
	
	private void init() {
		buses = new HashMap<String, Bus>();
		futureBuses = new HashSet<String>();
	}
	
	/*package*/ void notifyDeviceFound() {
		synchronized ( waitUntilFoundBusLock ) {
			if ( 0 < futureBuses.size() ) {
				waitUntilFoundBusLock.notify();
			}
		}
	}
	
	private Thread thread;
	
	private void prepare() {
		loop();
	}
	
	private void loop() {
	
		forceKill = false;
		
		thread = new Thread() {
		
			@Override
			public void run() {
		
		synchronized ( waitUntilFoundBusLock ) {		
			
			final long start = SystemClock.uptimeMillis();
			
			while ( ! forceKill ) {
				
				while ( 0 == futureBuses.size() ) {
					try {
						waitUntilFoundBusLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				while ( true ) {
					if ( first ) {					
						Bus first = pollFirst();
						
						if ( null != first && isRangeIn( first ) ) {
							current = first;
							
						} else {
							Bus tryNext = findNext();
							
							if ( null != tryNext && isRangeIn( tryNext ) ) {
								current = tryNext;
								
							} else {
								// -> continue
							}						
						}
					} else {
						Bus next = findNext();
						
						if ( null != next ) { 
							if ( isRangeIn( next ) ) {
								
								current = next;
							} else {
								
								// -> continue
							}
						} else {
							
							// end of -> Just wait
						}					
					}
					
					if ( null != current() ) {
						if ( BuildConfig.DEBUG ) Log.i( "MmcpMasterStation", "++ looping " + Thread.currentThread() );
						
						current().run();
						
					} else {
						
						// Just wait until one or more device be found...
						try {
							waitUntilFoundBusLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
					try {
						waitUntilFoundBusLock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					continue;
				}
			} // End of the main loop
		}
		
		
			} // End of run of Thread 
		};
		
		thread.start();
	}
	
	private void quit() {
		synchronized ( waitUntilFoundBusLock ) {
			forceKill = true;
			waitUntilFoundBusLock.notify();
		}
	}
	
	private void resetLoopIndex() {
		loopIndex = -1;
	}
	
	// TODO Maybe should Move to MmcpBluetooth
	private boolean isRangeIn(Bus which) {
		return futureBuses.contains( ( (BluetoothBus) which ).address );
	}
	
	private Bus pollFirst() {
		Bus first = null;
		
		if ( -1 == loopIndex ) {
			for ( Bus bus : buses.values() ) {
				loopIndex = 0;
				first = bus;
				break;
			}
		}
		
		// Change the first flag as false
		this.first = false;
		
		return first;
	}
	
//	Thread t = new Thread() {
//
//		@Override
//		public void run() {
//			if ( DEBUG ) {
//				Log.i( LOG_TAG, "-->> Thread " + Thread.currentThread() );
//				synchronized ( this ) {
//					try {
//						System.out.println( "wait before " + SystemClock.uptimeMillis() );
//						Thread.currentThread().wait( 1000 );
//						System.out.println( "wait after " + SystemClock.uptimeMillis() );
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}			
//	};
//	t.start();
	
	private void reset() {
		
	}
	
	/*package*/ MmcpMasterStation() {
		init();
	}
}