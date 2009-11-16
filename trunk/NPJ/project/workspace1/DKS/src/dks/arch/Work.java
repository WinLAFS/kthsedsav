/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.arch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * The <code>Work</code> class
 * 
 * @author Roberto Roverso
 * @version $Id: Work.java 453 2007-11-28 16:31:33Z cosmin $
 */
public class Work implements Runnable {

	// private static Logger log = Logger.getLogger(Work.class);

	private Event event;

	private Method method;

	private Component component;

	private WorkingPair processedPair;

	/**
	 * Creating the general work to be done
	 * 
	 * @param processedPair
	 */
	public Work(Event event, Component component, Method method,
			WorkingPair processedPair) {
		this.event = event;
		this.component = component;
		this.method = method;
		this.processedPair = processedPair;
	}

	/*
	 * Running the method on the object and with the event present in the
	 * subscription
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		synchronized (component) {
			// log.debug("Executing event handler");
			try {
				method.invoke(component, event);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// log.debug("Event handler execution finished, calling finishedWork");
//		ComponentRegistry.getInstance().getScheduler().finishedWork(
//				processedPair, this);
	}

	/**
	 * Does the actual work. Called by a worker thread in the thread pool.
	 */
	public Object call() throws Exception {
		synchronized (component) {
			return method.invoke(component, event);
		}
	}

	/**
	 * @return Returns the processedPair.
	 */
	public WorkingPair getProcessedPair() {
		return processedPair;
	}
}
