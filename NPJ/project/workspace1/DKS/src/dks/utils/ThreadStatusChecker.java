package dks.utils;

/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;

import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.events.StartScriptEvent;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;

/**
 * The <code>StorageAggregator</code> class
 * 
 * @author Joel
 * @version $Id: StorageAggregator.java 294 2006-05-05 17:14:14Z joel $
 */
public class ThreadStatusChecker implements Runnable, EventHandlerInterface {

	
	

	static final int TIMER =
		System.getProperty("dks.test.threadStatus") instanceof String ?
				Integer.parseInt(System.getProperty("dks.test.threadStatus"))
			:
				
				5000
	;
				
	static final int EVERY_X =
		System.getProperty("dks.test.threadStatusDump") instanceof String ?
				Integer.parseInt(System.getProperty("dks.test.threadStatusDump"))
			:
				100
	;

	// ///////////////////
	
	private NicheAsynchronousInterface logger;
	private NicheManagementInterface niche;
	
	//private NicheActuatorInterface timerInterface;
	long timerId;
	ThreadMXBean bean;
	long lastTimeSeen;
	HashMap<Long, Long> times;
	int counter = 0;
	
	public ThreadStatusChecker(NicheManagementInterface niche) {
		this.logger = niche.getNicheAsynchronousSupport();
		//this.timerInterface = timerInterface;
		this.bean = ManagementFactory.getThreadMXBean();
		times = new HashMap<Long, Long>(500);
	}

	
	public void run() {
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//timerInterface = niche.getJadeSupport(null);
		System.out.println("StatusChecker is registering itself");
		timerId = logger.registerTimer(
					this,
					StartScriptEvent.class,
					TIMER
				);	
	}


	public void eventHandler(Serializable event) {
		eventHandler(event, 0);
	}

	public void eventHandler(Serializable event, int flag) {
		//Time to track threads!
		long ids[] = bean.getAllThreadIds();
		long currentTime = 0;
		for (long id : ids) {
			times.put(id, bean.getThreadCpuTime(id));
			
		}
		Object allTimes[] = times.values().toArray();
		for (Object t : allTimes) {
			currentTime += (Long)t;
		}
//		System.out.println("current time, last time\n"
//				+currentTime
//				+"\n"
//				+lastTimeSeen
//		);
		/*#%*/ logger.log("ThreadChecker says: time spent in " + ids.length + " threads is " + ( (currentTime - lastTimeSeen) / 1000000.0 ) + " ms");
		lastTimeSeen = currentTime;
		counter++;
		
		if( (counter % EVERY_X) == 0) {
			counter = 1;
			/*#%*/ String logMessage = "";
			ThreadInfo[] ti = bean.getThreadInfo(ids);
			/*#%*/ for (ThreadInfo threadInfo : ti) {
				/*#%*/ logMessage += "\nState for " + threadInfo.getThreadName() + " is " + threadInfo.getThreadState();
			/*#%*/ }
		/*#%*/ logger.log(logMessage);
		}
		timerId = logger.registerTimer(
				this,
				StartScriptEvent.class,
				TIMER
			);

	}
	

}
