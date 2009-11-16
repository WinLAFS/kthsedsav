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
import dks.router.events.GetResponsibleResponse;
import dks.router.events.LookupResultEvent;

/**
 * The <code>MessageThroughputComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageThroughputComponent.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class LookupThroughputComponent extends CommunicatingComponent {

	private static final int LOOKUP_NUMBER = 2000;

//	private static final long IDENTIFIER_SPACE = 1023;

	private int count;

//	private double average;
//
//	private TimerComponent timerComponent;

	private long startTimestamp;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public LookupThroughputComponent(Scheduler scheduler,
			ComponentRegistry registry) {
		super(scheduler, registry);

//		this.timerComponent = registry.getTimerComponent();

		registerForEvents();
		// registerConsumer();
	}

	public void start() {

		/*-----------------STARTING MEASUREMENS------------------*/
		System.out.println("Starting");

		// timerComponent.registerTimer(MeasurementTimerExpiredEvent.class,
		// null,
		// 1000);

		// while (true) {
		//
		// try {
		// Thread.sleep(1);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		//			
		// ThroughputMessage message = new ThroughputMessage();
		//
		// send(message);
		//
		// transmitter = true;
		//
		// }

//		Random random = new Random();

		count = 0;

		startTimestamp = System.currentTimeMillis();

		for (int i = 0; i < LOOKUP_NUMBER; i++) {

//			long lookedUpId = (int) Math.floor(((IDENTIFIER_SPACE * random
//					.nextDouble())));
//
//			BigInteger Id = BigInteger.valueOf(lookedUpId);

			// ReliableLookupRequestEvent lookupRequest = new
			// ReliableLookupRequestEvent(
			// Id, TOPOLOGY_MAINTENANCE_LOOKUP_STRATEGY,
			// new GetResponsibleRequest(Id));
			// trigger(lookupRequest);

		}
		
		System.out.println("overhead="+(System.currentTimeMillis()-startTimestamp));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		// register(MeasurementTimerExpiredEvent.class,
		// "handleMeasurementTimerExpired");

		register(LookupResultEvent.class, "handleLookupResultEvent");
	}

	public void handleLookupResultEvent(LookupResultEvent event) {

		if (event.getOperationMessage().getClass().equals(
				GetResponsibleResponse.class)) {

			// GetResponsibleResponse response = (GetResponsibleResponse) event
			// .getOperationMessage();
			//
			// System.out.println("LookedUp Id=" + response.getLookedUpId()
			// + " Responsible=" + response.getResponsible());

			count++;

			if (count == LOOKUP_NUMBER) {

				System.out.println("Time taken to make " + LOOKUP_NUMBER
						+ " lookups is:"
						+ (System.currentTimeMillis() - startTimestamp)
						+ " millisecs");

			}
		}

	}

	// protected void registerConsumer() {
	// registerConsumer("handleThroughputMessage", ThroughputMessage.class);
	// }

	// public void handleThroughputMessage(DeliverMessageEvent event) {
	//
	// Message message = event.getMessage();
	//
	// if (transmitter) {
	//
	// count++;
	//
	// this.send(message);
	//
	// } else {
	//
	// this.send(message);
	//
	// }
	//
	// }

	// public void send(Message message) {
	// super.send(message, ringComponent.getMyDKSRef(), ringComponent
	// .getRingState().successor);
	//
	// // if (transmitter)
	// // System.out.println("Sending to="
	// // + ringComponent.getRingState().successor);
	//
	// }
	//
	// public void handleMeasurementTimerExpired(MeasurementTimerExpiredEvent
	// event) {
	//
	// // totalCount += count;
	// // seconds++;
	// //
	// // average = totalCount / seconds;
	// //
	// // count = 0;
	//
	// System.out.println("Average per second=" + average);
	//
	// // timerComponent.registerTimer(MeasurementTimerExpiredEvent.class,
	// // null,
	// // 1000);
	//
	// }

}
