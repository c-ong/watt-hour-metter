package org.ong.mmcp.queue;

import org.ong.mmcp.MmcpContext;
import org.ong.mmcp.QueueableOperation;
import org.ong.mmcp.impl.DefaultMmcpMasterStation;

import cn.hnsi.android.apps.metterreader.BuildConfig;

import android.os.SystemClock;
import android.util.Log;

import java.lang.Thread.State;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 半双工 half-duplex
 *  
 * 在双向通道中，双向交替进行、一次只在一个方向(而不是同时在两个方向)传输信息的
 * 一种通信方式。
 * 
 * @author 	<a href="mailto:izhaoad@gmail.com">ONG</a>
 */
public class QueueExecutor {
	private static final boolean DEBUG = BuildConfig.DEBUG;
	private static final String LOG_TAG = "QueueExecutor";
	
	private static final AtomicLong _QueueNumber = new AtomicLong( 0 );
	
	private boolean strictDefer = false;
	
	/*
	 * 收到命令帧后的响应延时 Td：20ms ≤ Td ≤ 500ms,
	 * 字节之间停顿时间 Tb: Tb ≤ 500ms.
	 */
	private int frequencyInterval = 500;
	
	private QueueEntry head;	
	private QueueableOperation currentOp;
	
	private QueueEntry tail;
	
	private Runner runner = null;
	private boolean working = false;;
	
	MmcpContext ctx;
	
	// Delivery result to UI Activity
	// MasterStation
	DefaultMmcpMasterStation t = null;
	
	public QueueExecutor(DefaultMmcpMasterStation defaultMmcpMasterStation) {
		t = defaultMmcpMasterStation;
	}
	
	public QueueEntry current() {
		return head;
	}
	
	long allocateQueueId() {
		long result = _QueueNumber.getAndIncrement();
		if ( DEBUG ) Log.i( LOG_TAG, "## allocateQueueId: " + result ); 
		
		return result;
	}
	
	public long enqueue(QueueableOperation op) {
		if ( DEBUG ) Log.i( LOG_TAG, "++ enqueue: " + op ); 
		
		if ( null == runner && ! working ) {
			runner = new Runner();
			
			// WARING: do not set the Thread is Daemon Thread
			// runner.setDaemon( true );
			
			runner.start();
			working = true;
		} 
		
		// The new Task...
		QueueEntry ne = QueueEntry.obtain( this );
		
		ne.id 	= allocateQueueId();
		ne.obj 	= op;
		
		synchronized( runner ) {
			Log.i( LOG_TAG, "## Runner " + runner.getState() );
			
			if ( null == head() ) {	// 第一个 Waiter
				tail = head = ne;
				if ( Thread.State.WAITING == runner.getState() ) {
					runner.notify();
				}
			} else {				// 第 N + 1个 Waiter，往后排
				QueueEntry prev = tail();
				tail = ne;
				prev.next = tail;
			}
		}
		
//		if ( null == runner && ! working ) {
//			runner = new Runner();
//			/* WARING: do not set the Thread is Daemon Thread
//			 * runner.setDaemon( true ); 
//			 */
//			runner.start();
//			working = true;
//		} 
//		
//		// The new Task...
//		QueueEntry ne = QueueEntry.obtain( this );
//		ne.id = allocateQueueId();
//		ne.obj = op;
//		
//		synchronized( runner ) {
//			System.out.println( "## Runner " + runner.getState() );
//			
//			if ( null == tail ) {
//				tail = ne;
//				head = tail;
//				runner.notify();
//			} else {
//				QueueEntry prev = tail;
//				tail = ne;
//				prev.next = tail;
//			}
//		}
		
		return ne.id;
	}
	
	private QueueEntry next() {
		if ( null == head ) return null;
		
		QueueEntry next = head.next;
		head = next;
		
//		if ( head == tail ) tail = null;
//		QueueEntry next = head.next;
//		head = next;
		
		return next;
	}
	
	private QueueEntry head() {
		return head;
	}
	
	private QueueEntry tail() {
		return tail;
	}
	
	void awaitReply() {
		// 是否需要回应
	}
	
	private void processing(QueueEntry task) 
			throws InterruptedException {
		t.performTask( task );
	}
	
	/*package*/ State getState() {
		return runner.getState();
	}
	
	// TODO 如果我数据已经接收完毕则马上可以操作下轮任务，有待测试可行性...
	void onTaskDone(QueueEntry task) {
		Log.i( LOG_TAG, "## onTaskDone: task=" + task.id + ", head=" 
				+ ( null != head() ? head().id : "NONE" ) + " ##" );
		
		synchronized ( runner ) {
			
			Log.i( LOG_TAG, "## State: " + runner.getState() + " ##" );
			// TIMED_WAITING 不考虑
			Thread.State state = runner.getState();
			
			boolean waiting 		= Thread.State.WAITING == state;
			boolean timedWaiting 	= Thread.State.TIMED_WAITING == state;
			
			if ( strictDefer ? timedWaiting : waiting || timedWaiting ) {
				runner.notify();
			}
		} 
	}
	
	/*package*/ void fireTaskTimedOut() {
		onTaskTimedOut();
	}
	
	/*package*/ void onTaskTimedOut() {
		head().state = QueueEntry.State.TIMED_OUT;
	}
	
	/*package*/ void notifyTaskIsDone(QueueEntry task) {
//		synchronized( runner ) {
//			runner.notify();
//		}
	}
	
	// ## Responsed raw:     FEFE6834002003120068810643C364343333C416	
	// Data frame is invalid[FEFE6803002003120068810643C364343333C416] !!
	
	/* 
	 * 0 1  2  3 4 5 6 7 8  9  10 11 1213 14151617 18 19
	 * EFEF 68 340020031200 68 81 06 43C3 5B343333 BB 16
	 */
	
	/*package*/ void processAndAwaitUntilResponed(QueueEntry task) {
		// task.doWoring
	}
	
	private void processAndAwaitResponed(QueueEntry task) throws InterruptedException {
		t.performTask( task );
	}
	
	// ResponseInterceptor->handle
	
	private class Runner extends Thread {

		@Override
		public void run() {
			long last = SystemClock.uptimeMillis();
			synchronized( this ) {
				while ( true ) {
					QueueEntry e = head();
					try {
						// Empty quueue
						if ( null == e ) {
							wait();
							
						} else if ( QueueEntry.State.AVAILABLE == e.state ) {
							
							Log.i( LOG_TAG, "## Interval: " 
									+ (SystemClock.uptimeMillis() - last) 
									+ " QueueID " + e.id );
							
							last = SystemClock.uptimeMillis();
							
							processing( e );
//							processAndAwaitResponed( e );
							
							// 收到命令帧后的响应延时 Td：20ms ≤ Td ≤ 500ms
							wait( frequencyInterval );
							// 如果这里醒来在状态为 PROCESSING 时还继续等待...
							
						} else if ( QueueEntry.State.PROCESSING == e.state ) {
							
							// 字节之间停顿时间 Tb: Tb ≤ 500ms
							// XXX 按理说这项操作只是分析数据，合理的话一半时间便够。
							wait( frequencyInterval );
							
							// Task response timed out...
							fireTaskTimedOut();
							
						} else if ( QueueEntry.State.DONE == e.state ) {
							next();
						} else if ( QueueEntry.State.TIMED_OUT == e.state ) {
							next();
						}
						
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} 
				}
			}
		}
	}
}