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

import java.util.List;

import dks.DKSParameters;
import dks.comm.mina.CommunicationComponent;
import dks.fd.FailureDetectorComponent;
import dks.operations.OperationManagerComponent;
import dks.ring.RingMaintenanceComponentInt;
import dks.router.Router;
import dks.timer.TimerComponent;
import dks.utils.AtomicIntSequenceGenerator;
import dks.web.jetty.JettyServer;

/**
 * The <code>ComponentRegistry</code> class
 * 
 * @author Roberto Roverso
 * @version $Id: ComponentRegistry.java 494 2007-12-14 15:09:00Z roberto $
 */
public class ComponentRegistry {

	/* Parameters of teh system */
	private static DKSParameters dksParameters;

	/* Components in the system */
	private TimerComponent timerComponent;

	private CommunicationComponent communicatorComponent;

	private Scheduler scheduler;

	private JettyServer webServerComponent;

	private FailureDetectorComponent failureDetectorComponent;

	private Router router;

	private RingMaintenanceComponentInt ringMaintainer;

	private OperationManagerComponent operationManager;

	/* Helper Components */

	private static AtomicIntSequenceGenerator sequenceGenerator;

	private static EventsRegistry eventsRegistry;

	private static HooksRegistry hooksRegistry;

	private static ComponentRegistry singleton = null;

	/**
	 * @return Returns the dksParameters.
	 */
	public static DKSParameters getDksParameters() {
		return dksParameters;
	}

	public static ComponentRegistry getInstance() {
		return singleton;
	}

	public static ComponentRegistry init(DKSParameters dksParameters) {
		ComponentRegistry.dksParameters = dksParameters;
		return singleton = new ComponentRegistry();
	}

	private ComponentRegistry() {
		eventsRegistry = new EventsRegistry();
		timerComponent = null;
	}

	public void registerTimerComponent(TimerComponent timerComponent) {
		this.timerComponent = timerComponent;
	}

	public void registerFailureDetectorComponent(
			FailureDetectorComponent failureDetectorComponent) {
		this.failureDetectorComponent = failureDetectorComponent;
	}

	public void registerRouter(Router router) {
		this.router = router;
	}

	public static AtomicIntSequenceGenerator getSequenceGenerator() {
		if (sequenceGenerator == null) {
			return sequenceGenerator = new AtomicIntSequenceGenerator(0);
		}
		return sequenceGenerator;
	}

	public EventsRegistry getEventsRegistry() {

		return eventsRegistry;
	}

	public HooksRegistry getHooksRegistry() {
		if (hooksRegistry == null) {
			return hooksRegistry = new HooksRegistry();
		}
		return hooksRegistry;
	}

	/**
	 * Adds a subscription to the corresponding event
	 */
	// public synchronized void addSubscription(Subscription sub) {
	// eventsRegistry.addSubscription(sub);
	// }
	public synchronized void addSubscription(Subscription sub, boolean fifo) {
		eventsRegistry.addSubscription(sub, fifo);
	}

	/**
	 * Gets all the subscription associated with an event in the registry
	 */
	public List<Subscription> getSubscriptions(Class eventClass) {
		return eventsRegistry.getSubscriptions(eventClass);
	}

	public TimerComponent getTimerComponent() {
		return timerComponent;
	}

	public CommunicationComponent getCommunicatorComponent() {
		return communicatorComponent;
	}

	public void setCommunicatorComponent(
			CommunicationComponent communicatorComponent) {
		this.communicatorComponent = communicatorComponent;
	}

	// public MarshallComponent getMarshalerComponent() {
	// return marshalerComponent;
	// }
	//
	// public void registerMarshalerComponent(MarshallComponent
	// marshalerComponent) {
	// this.marshalerComponent = marshalerComponent;
	//	}

	/**
	 * @param scheduler
	 */
	public void registerScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;

	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	/**
	 * @return Returns the webServerComponent.
	 */
	public JettyServer getWebServerComponent() {
		return webServerComponent;
	}

	/**
	 * @param webServerComponent
	 *            The webServerComponent to set.
	 */
	public void setWebServerComponent(JettyServer webServerComponent) {
		this.webServerComponent = webServerComponent;
	}

	/**
	 * @return Returns the failureDetectorComponent.
	 */
	public FailureDetectorComponent getFailureDetectorComponent() {
		return failureDetectorComponent;
	}

	/**
	 * @return Returns the router.
	 */
	public Router getRouterComponent() {
		return router;
	}

	/**
	 * @param component
	 */
	public void registerRingMaintainer(RingMaintenanceComponentInt component) {
		this.ringMaintainer = component;
	}

	/**
	 * @return Returns the ringMaintainer.
	 */
	public RingMaintenanceComponentInt getRingMaintainerComponent() {
		return ringMaintainer;
	}

	/**
	 * @param component
	 */
	public void registerOperationManager(OperationManagerComponent component) {
		this.operationManager = component;

	}

	/**
	 * @return Returns the operationManager.
	 */
	public OperationManagerComponent getOperationManager() {
		return operationManager;
	}

}
