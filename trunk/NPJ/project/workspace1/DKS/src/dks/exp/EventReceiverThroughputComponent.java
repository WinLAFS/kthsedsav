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
import dks.timer.TimerComponent;

/**
 * The <code>MessageThroughputComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageThroughputComponent.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class EventReceiverThroughputComponent extends CommunicatingComponent {

	private final static long MEASUREMENT_INTERVAL = 1000;

//	private RingMaintenanceComponentInt ringComponent;

//	private boolean transmitter = false;

	private double count;

	private double totalCount;

	private double seconds;

//	private double average;

	private int i = 0;

	private TimerComponent timerComponent;

	private long startTimestamp;

	private long number;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public EventReceiverThroughputComponent(Scheduler scheduler,
			ComponentRegistry registry) {
		super(scheduler, registry);

//		this.ringComponent = registry.getRingMaintainerComponent();

		this.timerComponent = registry.getTimerComponent();

		registerForEvents();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.arch.Component#registerForEvents()
	 */
	@Override
	protected void registerForEvents() {
		register(MeasurementTimerExpiredEvent.class,
				"handleMeasurementTimerExpired");
		register(ThroughtputReqEvent.class, "handleThroughputEvent");
	}

	public void handleThroughputEvent(ThroughtputReqEvent event) {

		if (i == 0) {
			timerComponent.registerTimer(MeasurementTimerExpiredEvent.class,
					null, MEASUREMENT_INTERVAL);
			i++;
		}
		count++;

	}

	public void handleMeasurementTimerExpired(MeasurementTimerExpiredEvent event) {

		if (TestRingBootAndJoinTest.WHICH_TEST == TestRingBootAndJoinTest.EVENTS) {
			totalCount += count;
			seconds++;

//			average = totalCount / seconds;
			System.out.println("Events per second=" + count);

			count = 0;

			timerComponent.registerTimer(MeasurementTimerExpiredEvent.class,
					null, MEASUREMENT_INTERVAL);

			if (totalCount == number) {

				System.out.println("FINISHED in "
						+ (System.currentTimeMillis() - startTimestamp));

			}

		}
	}

	public void setStartTimestamp(long timestamp) {
		this.startTimestamp = timestamp;

	}

	public void setNumber(long number) {
		this.number = number;

	}
}
