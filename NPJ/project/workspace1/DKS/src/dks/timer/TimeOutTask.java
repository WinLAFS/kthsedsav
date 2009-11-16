/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.timer;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import dks.arch.Event;

/**
 * The <code>TimeOutTask</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TimeOutTask.java 586 2008-03-26 11:03:21Z ahmad $
 */
public class TimeOutTask extends TimerTask {

	/*#%*/ private static Logger log = Logger.getLogger(TimeOutTask.class);

	private TimerComponent timer;

	private Object attachment;

	private Class eventClass;

	private long timerID;

	/**
	 * Generate a TimeOutTask to be triggered by the Timer when the amount of
	 * time set is expired
	 * 
	 * @param eventClass
	 */
	public TimeOutTask(Class eventClass, TimerComponent timer,
			Object attachment, long timerID) {
		this.timer = timer;
		this.attachment = attachment;
		this.eventClass = eventClass;
		this.timerID = timerID;
	}

	@Override
	public void run() {
		/*#%*/ log.debug("Timeout " + timerID + "expired - issueing event "
		/*#%*/ 		+ eventClass);
		Event event = null;
		try {
			event = (Event) (eventClass.newInstance());
		} catch (InstantiationException e) {
			/*#%*/ log.debug("Problems instantiating");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			/*#%*/ log.debug("Problems instantiating");
			e.printStackTrace();
		}
		event.setAttachment(this.attachment);

		// Setting the priority for the queue of events
		// event.setPriority(TIMER_PRIORITY);
		
		event.fairPriority=Event.Priority.HIGH;

		timer.timeout(timerID, event);
	}
}
