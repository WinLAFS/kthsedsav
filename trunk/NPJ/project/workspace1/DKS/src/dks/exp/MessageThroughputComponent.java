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

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.messages.Message;
import dks.ring.RingMaintenanceComponentInt;
import dks.timer.TimerComponent;

/**
 * The <code>MessageThroughputComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageThroughputComponent.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class MessageThroughputComponent extends CommunicatingComponent {

	private final static long MEASUREMENT_INTERVAL = 1000;

	private RingMaintenanceComponentInt ringComponent;

	private boolean stop = true;

	private boolean isSink = false;

	private double count;

	private double totalCount;

	private double seconds;

//	private double average;

	private int i = 0;

	private TimerComponent timerComponent;

	private DKSRef dksRef;

	/**
	 * @param scheduler
	 * @param registry
	 */
	public MessageThroughputComponent(Scheduler scheduler,
			ComponentRegistry registry) {
		super(scheduler, registry);

		this.ringComponent = registry.getRingMaintainerComponent();

		this.dksRef = registry.getRingMaintainerComponent().getMyDKSRef();

		this.timerComponent = registry.getTimerComponent();

		registerForEvents();
		registerConsumer();
	}

	public void stop() {
		stop = true;
	}

	private int cnt;

	private int sleep;

	private int size;

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public void setSleep(int sleep) {
		this.sleep = sleep;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public void start() {

		/*-----------------STARTING MEASUREMENS------------------*/
		System.out.println("Starting " + cnt + " sleep=" + sleep + " size="
				+ size);

		stop = false;

		while (!stop) {

			try {
				Thread.sleep(sleep);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < cnt; i++) {
				ThroughputMessage message = new ThroughputMessage(size);
				send(message);
			}
		}

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
	}

	protected void registerConsumer() {
		registerConsumer("handleThroughputMessage", ThroughputMessage.class);
	}

	public void handleThroughputMessage(DeliverMessageEvent event) {

		Message message = event.getMessage();

		if (isSink) {

			if (i == 0) {

				timerComponent.registerTimer(
						MeasurementTimerExpiredEvent.class, null,
						MEASUREMENT_INTERVAL);
				i++;
				// System.out.println("HEJ4");
			}

			// System.out.println("HEJ1");
			count++;

			// this.send(message);

		} else {

			this.send(message);

		}

	}

	public void send(Message message) {
		// System.out.println("HEJ2");
		super.send(message, dksRef, ringComponent.getRingState().successor);

		// if (transmitter)
		// System.out.println("Sending to="+
		// ringComponent.getRingState().successor);

	}

	public void handleMeasurementTimerExpired(MeasurementTimerExpiredEvent event) {

		// System.out.println("HE3J");
		if (TestRingBootAndJoinTest.WHICH_TEST == TestRingBootAndJoinTest.MESSAGES) {
			totalCount += count;
			seconds++;

//			average = totalCount / seconds;

			System.out.println(count
					+ "");

			count = 0;

			timerComponent.registerTimer(MeasurementTimerExpiredEvent.class,
					null, MEASUREMENT_INTERVAL);
		}

	}

	/**
	 * @param b
	 */
	public void setIsSink(boolean b) {
		this.isSink = b;
	}
}
