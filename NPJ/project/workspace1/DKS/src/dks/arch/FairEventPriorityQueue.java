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

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

/**
 * The <code>FairEventPriorityQueue</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FairEventPriorityQueue.java 294 2006-05-05 17:14:14Z roberto $
 */
public class FairEventPriorityQueue {

	/*#%*/ private static Logger log = Logger.getLogger(FairEventPriorityQueue.class);

	private ConcurrentLinkedQueue<Event> highPriorityQueue;

	private ConcurrentLinkedQueue<Event> normalPriorityQueue;

	private ConcurrentLinkedQueue<Event> lowPriorityQueue;

	private int fairnessRate;

	private long highPriorityCounter = 0;

	private long normalPriorityCounter = 0;

	/**
	 * {@link FairEventPriorityQueue} constructor
	 * 
	 * @param fairnessRate
	 *            The fairness rate
	 */

	public FairEventPriorityQueue(int fairnessRate) {
		this.fairnessRate = fairnessRate;
		this.highPriorityQueue = new ConcurrentLinkedQueue<Event>();
		this.normalPriorityQueue = new ConcurrentLinkedQueue<Event>();
		this.lowPriorityQueue = new ConcurrentLinkedQueue<Event>();
	}

	public synchronized Event take() throws InterruptedException {

		Event takenEvent = null;
		/*
		 * log.debug("Events in the high priority queue=" +
		 * highPriorityQueue.size()); log.debug("Events in the normal priority
		 * queue=" + normalPriorityQueue.size()); log .debug("Events in the low
		 * priority queue=" + lowPriorityQueue.size());
		 */

		if (highPriorityQueue.isEmpty() && normalPriorityQueue.isEmpty()
				&& lowPriorityQueue.isEmpty()) {

			// log.debug(Thread.currentThread().getName());

			/* Block the thread until an event is triggered */
			wait();
			// log.debug("It doesn't block");

		}

		/* Take from the queue according to prioritization and fairness */
		if ((highPriorityCounter < fairnessRate || (normalPriorityQueue
				.isEmpty() && lowPriorityQueue.isEmpty()))
				&& !highPriorityQueue.isEmpty()) {

			/* Take from the high priority queue */
			takenEvent = highPriorityQueue.remove();

			if (normalPriorityQueue.isEmpty() && lowPriorityQueue.isEmpty()) {

				highPriorityCounter = 0;

			} else {

				highPriorityCounter++;

			}

			/*#%*/ log.debug("Taking one event from the HIGH priority queue");

		} else

		if ((normalPriorityCounter < fairnessRate || lowPriorityQueue.isEmpty())
				&& !normalPriorityQueue.isEmpty()) {

			/* Take from the normal priority queue */
			takenEvent = normalPriorityQueue.remove();

			// log.debug("Taking one event from the NORMAL priority queue");

			if (lowPriorityQueue.isEmpty()
					|| highPriorityCounter == fairnessRate
					|| highPriorityQueue.isEmpty()) {

				highPriorityCounter = 0;

			}

			normalPriorityCounter++;

		} else {

			/* Take from the low priority queue */
			takenEvent = lowPriorityQueue.remove();

			/*#%*/ log.debug("Taking one event from the LOW priority queue");

			normalPriorityCounter = 0;
			highPriorityCounter = 0;

		}

		return takenEvent;

	}

	public synchronized void add(Event event) {

		switch (event.fairPriority) {

		case HIGH:
			highPriorityQueue.add(event);
			// log.debug("Events in the high priority queue="
			// + highPriorityQueue.size());

			break;

		case NORMAL:
			normalPriorityQueue.add(event);
			// log.debug("Events in the normal priority queue="
			// + normalPriorityQueue.size());

			break;

		case LOW:
			lowPriorityQueue.add(event);
			// log.debug("Events in the low priority queue="
			// + lowPriorityQueue.size());
			break;

			/*#%*/ default:
			/*#%*/ log.debug("Unknown type of priority");
			/*#%*/ break;
		}

		// slog.debug(Thread.currentThread().getName());

		// log.debug("Notify");
		notify();

	}

}
