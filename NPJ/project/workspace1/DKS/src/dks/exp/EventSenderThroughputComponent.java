/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.exp;

import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;

/**
 * The <code>MessageThroughputComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageThroughputComponent.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class EventSenderThroughputComponent extends CommunicatingComponent {

//	private RingMaintenanceComponentInt ringComponent;
//
//	private boolean transmitter = false;
//
//	private double count;
//
//	private double totalCount;
//
//	private double seconds;
//
//	private double average;
//
//	private TimerComponent timerComponent;

	private EventReceiverThroughputComponent receiver;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public EventSenderThroughputComponent(Scheduler scheduler,
			ComponentRegistry registry,EventReceiverThroughputComponent comp) {
		super(scheduler, registry);

//		this.ringComponent = registry.getRingMaintainerComponent();
//
//		this.timerComponent = registry.getTimerComponent();

		this.receiver=comp;
		// registerForEvents();
	}

	public void start() {

		/*-----------------STARTING MEASUREMENS------------------*/
		System.out.println("Starting");

		// timerComponent.registerTimer(MeasurementTimerExpiredEvent.class,
		// null,
		// 1000);

		int c=500000;
		
		receiver.setNumber(c);
		receiver.setStartTimestamp(System.currentTimeMillis());
		
		for (int i = 0; i < c; i++) {
			 ThroughtputReqEvent event = new ThroughtputReqEvent();

			trigger(event);
		}
		
		// while(true) {
		//			
		// ThroughtputReqEvent event = new ThroughtputReqEvent();
		//
		// trigger(event);
		//
		//		}

	}

	// @Override
	// protected void registerForEvents() {
	// register(ThroughtputResEvent.class, "handleThroughputResEvent");
	// }
	//
	// public void handleThroughputResEvent(ThroughtputReqEvent event) {
	//
	// count++;
	//
	// }
	//	
	// public void handleMeasurementTimerExpired(MeasurementTimerExpiredEvent
	// event) {
	//
	// totalCount += count;
	// seconds++;
	//
	// average = totalCount / seconds;
	//
	// count = 0;
	//
	// System.out.println("Average per second=" + average);
	//
	// timerComponent.registerTimer(MeasurementTimerExpiredEvent.class, null,
	// 1000);
	//
	// }

}
