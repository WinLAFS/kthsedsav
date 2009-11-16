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

import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.utils.LongSequenceGenerator;

/**
 * The <code>TimerComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: TimerComponent.java 450 2007-11-27 16:27:53Z roberto $
 */
public class TimerComponent extends Component {

	/*#%*/ private static Logger log = Logger.getLogger(TimerComponent.class);

	/* Map keeping track of the Timers now in execution */
	private ConcurrentHashMap<Long, TimeOutTask> timers;

	private Timer timer;

	private LongSequenceGenerator sequenceGenerator;

	/**
	 * Generates a timer component
	 */
	public TimerComponent(ComponentRegistry registry, Scheduler scheduler) {
		super(scheduler, registry);
		this.timer = new Timer();
		this.timers = new ConcurrentHashMap<Long, TimeOutTask>();
		this.sequenceGenerator = new LongSequenceGenerator(0);
		registry.registerTimerComponent(this);
	}

	/**
	 * Generates a Timer for the object passed and schedules it after the
	 * specified timeout
	 * 
	 * @param obj
	 *            The object to be returned when the timeout expires
	 * @param timeout
	 *            The timeout
	 * @return Returns the timer id
	 */

	public synchronized long registerTimer(Class eventClass, Object obj,
			long timeout) {

		// Generating the TimeOutTask to handle the expiration of the Timer
		long timerId = sequenceGenerator.getNextSequenceNumber();
		TimeOutTask timeOutTask =
			new TimeOutTask(
					eventClass,
					this,
					obj,
					timerId
			);
	
			/*#%*/ log.debug("Setting timer num: " + timerId + " for event "
			/*#%*/ 		+ eventClass.getSimpleName() + " and "
			/*#%*/ 			+ (
			/*#%*/ 			(obj == null) ?
			/*#%*/ 				 " no attachment "
			/*#%*/ 			  :
			/*#%*/ 			 	" attachment " + obj.getClass().getSimpleName()
			/*#%*/ 			)
			/*#%*/ 	);

		timers.put(timerId, timeOutTask);
		/*#%*/try {
			timer.schedule(timeOutTask, timeout);
		/*#%*/} catch(IllegalStateException e) {
		/*#%*/	log.debug("Timer ERROR");
		/*#%*/	e.printStackTrace();
		/*#%*/}
	
		return timerId;
	}

	public synchronized boolean isTimerSet(long timerId) {
		return timers.containsKey(timerId);
	}

	public synchronized void cancelTimer(long timerId) {
		if (timers.containsKey(timerId)) {
			TimeOutTask timeOutTask = timers.remove(timerId);
			timeOutTask.cancel();
			/*#%*/ log.debug("Removing timer num: " + timerId);
			timer.purge();
		}
	}

	public synchronized void timeout(long timerID, Event event) {
		timers.remove(timerID);
		
		trigger(event);
	}

	/*
	 * Stops all the timer sheduled
	 */
	public synchronized void stopAll() {
		timer.cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		// TODO Auto-generated method stub

	}
}
