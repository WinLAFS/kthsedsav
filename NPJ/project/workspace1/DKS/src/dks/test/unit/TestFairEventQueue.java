/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.unit;


import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

import dks.arch.Event;
import dks.arch.FairEventPriorityQueue;

/**
 * The <code>FairEventQueueTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FairEventQueueTest.java 294 2006-05-05 17:14:14Z roberto $
 */
public class TestFairEventQueue extends TestCase {

	
	static int fairnessRate = 3;

	private FairEventPriorityQueue eventsQueue;

	private int highPcount = 0;

	private int normalPcount = 0;

	private int lowPcount = 0;

	/**
	 * 
	 */
	public TestFairEventQueue() {
		super();
	}

	/**
	 * @param arg0
	 */
	public TestFairEventQueue(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestFairEventQueue.class);
	}

	public void setUp() {
		
		BasicConfigurator.configure();
		eventsQueue = new FairEventPriorityQueue(fairnessRate);
	}

	public void tearDown() {

	}

	public void testQueue() {

		Event event = null;

//		for (int i = 0; i < 60; i++) {
//			event = new PriorityEvent();
//			event.fairPriority = Event.Priority.HIGH;
//			eventsQueue.add(event);
//		}
//
//		for (int i = 0; i < 30; i++) {
//			event = new PriorityEvent();
//			event.fairPriority = Event.Priority.NORMAL;
//			eventsQueue.add(event);
//		}
//		
//		for (int i = 0; i < 10; i++) {
//			event = new PriorityEvent();
//			event.fairPriority = Event.Priority.LOW;
//			eventsQueue.add(event);
//		}
		
		for (int i = 0; i < 100; i++) {

			try {
				event = eventsQueue.take();

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			switch (event.fairPriority) {
			case HIGH:
				highPcount++;
				//System.out.println("HIGH priority events="+highPcount);
				break;

			case NORMAL:
				normalPcount++;
				break;

			case LOW:
				lowPcount++;
				break;

			default:
				break;
			}
			
		}
		
	    System.out.println("HIGH priority events="+highPcount);
	    System.out.println("NORMAL priority events="+normalPcount);
	    System.out.println("LOW priority events="+lowPcount);
		assertEquals(9, highPcount);
		assertEquals(3, normalPcount);
		assertEquals(1, lowPcount);
	}

}
