package org.ong.mmcp.queue;

import org.ong.mmcp.OperationCallback;
import org.ong.mmcp.QueueableOperation;

import android.util.Log;

import cn.hnsi.android.apps.metterreader.BuildConfig;

/** 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class QueueEntry {
	
	enum State {
		AVAILABLE,
		
//		ACQUIRED,
		
		PROCESSING,
		TIMED_WAITTING_REPLAY,
		TIMED_WAITTING_RESPON,
//		EXPIRED,
		
//		DEQUEUED,
		
		DONE,
		
		TIMED_OUT;
		
        /**
         * The thread has been created, but has never been started.
         */
        //NEW,
        /**
         * The thread may be run.
         */
        //RUNNABLE,
        /**
         * The thread is blocked and waiting for a lock.
         */
        //BLOCKED,
        /**
         * The thread is waiting.
         */
        //WAITING,
        /**
         * The thread is waiting for a specified amount of time.
         */
        //TIMED_WAITING,
        /**
         * The thread has been terminated.
         */
        //TERMINATED
	}
	
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "QueueEntry";
	
	final QueueEntry HEAD = null;
	final QueueEntry TAIL = null;
	
	/*package*/ long id = -1;
	/*package*/ State state = State.AVAILABLE;
	/*package*/ QueueableOperation obj;
	
	//boolean done = false;
	
	/*package*/ QueueEntry next;
	
	private static Object _PoolSync = new Object();
	private static QueueEntry _Pool;	
    private static int _PoolSize = 0;

    private static final int MAX_POOL_SIZE = 10;
    
    private QueueExecutor executor;
	
	private QueueEntry() {
	}
	
	private QueueEntry(QueueExecutor executor) {
		this.executor = executor;
	}
	
	public void begin() {
		Log.i( LOG_TAG, "++ BEGIN ++" );
		// 保护该方法，使有效执行一次...
		if ( State.AVAILABLE == state ) {
			state = State.PROCESSING;
		}
	}
	
	public void end() {
		Log.i( LOG_TAG, "-- END --" );
		// 保护该方法，使有效执行一次...
		if ( State.DONE != state ) {
			state = State.DONE;
			
			executor.onTaskDone( this );
		}
		
//		Thread.State state = executor.getState();
//		if ( Thread.State.WAITING == state || Thread.State.TIMED_WAITING == state ) {
//			executor.notifyTaskIsDone( this );
//		} 
	}
	
	public long getId() {
		return id;
	}
	
	private void fireOnTimeout(OperationCallback callback) {
		
	}
	
	public QueueableOperation getOperation() {
		return obj;
	}
	
    /**
     * Return a new Message instance from the global pool. Allows us to
     * avoid allocating new objects in many cases.
     */
    static QueueEntry obtain(QueueExecutor executor) {
        synchronized ( _PoolSync ) {
            if ( null != _Pool ) {
            	QueueEntry e = _Pool;
            	
                _Pool = e.next;
                e.next = null;
                e.executor = executor;
                
                return e;
            }
        }
        return new QueueEntry( executor );
    }
    
    /**
     * Return a Message instance to the global pool.  You MUST NOT touch
     * the Message after calling this function -- it has effectively been
     * freed.
     */
    public void recycle() {
        synchronized ( _PoolSync ) {
            if (_PoolSize < MAX_POOL_SIZE) {
                clearForRecycle();
                
                next = _Pool;
                _Pool = this;
            }
        }
    }
    
    /*package*/ void clearForRecycle() {
    	id = -1;
    	state = State.AVAILABLE;
    	obj = null;    	
    	
    	next = TAIL;    	
//        what = 0;
//        arg1 = 0;
//        arg2 = 0;
//        obj = null;
//        replyTo = null;
//        when = 0;
//        target = null;
//        callback = null;
//        data = null;
    }
}