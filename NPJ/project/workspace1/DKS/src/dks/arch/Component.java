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

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import dks.comm.mina.events.DeliverMessageEvent;

/**
 * The <code>Component</code> class
 * 
 * @author Roberto Roverso
 * @version $Id: Component.java 496 2007-12-20 15:39:02Z roberto $
 */
public abstract class Component {

	protected ComponentRegistry registry;

	private Scheduler scheduler;

	private EventsRegistry eventsRegistry;

	private HooksRegistry hooksRegistry;
	
	private Executor executor;

	private final boolean separateThreadpool = System
		.getProperty("dks.scheduler.separateThreadpool") instanceof String ?
				0 < Integer.parseInt(System.getProperty("dks.scheduler.separateThreadpool")) ?
							true
						:
							false
				:
					true;

	
	protected Component(Scheduler scheduler, ComponentRegistry registry) {
		this.scheduler = scheduler;
		this.registry = registry;

		// Get the EventConsumersRegistry
		this.eventsRegistry = registry.getEventsRegistry();

		// Get the HooksRegistry
		this.hooksRegistry = registry.getHooksRegistry();
		
		this.executor = registry.getScheduler().getNicheExecutor();

	}
	

	/**
	 * Method to be overridden by the extending Component for registering for
	 * his own Events
	 * 
	 */
	protected abstract void registerForEvents();

	protected static Component newInstance() {
		return null;
	}

	protected void trigger(Event event) {
		scheduler.dispatch(event);
	}
	
	protected void execute(Runnable task) {
		if(separateThreadpool) {
			executor.execute(task);
		} else {
			scheduler.execute(task);
		}
	}

	// protected void register(Subscription subscription) {
	// registry.addSubscription(subscription);
	// }

	protected ComponentRegistry getComponentRegistry() {
		return registry;
	}

	/**
	 * Register the subscription for a component to a particular event
	 * 
	 * @param eventClass
	 *            The event to subscribe to
	 * @param method
	 *            The method of the component to handle the event
	 */

	protected void register(Class eventClass, String method) {
		// Added by Ahmad & Joel for testing
		register(eventClass, method, true);
	}

	protected void registerGeneric(Class eventClass, String method) {
		// Added by Ahmad & Joel for testing
		registerGeneric(eventClass, method, true);
	}

	protected void register(Class eventClass, String method, boolean fifo) {
		Class[] args = new Class[1];

		args[0] = eventClass;
		Method handlerMethod;
		try {
			handlerMethod = this.getClass().getMethod(method, args);

			Subscription subscription = new Subscription(this, handlerMethod,
					args[0]);

			// Joel and ahmad-edit: testing
			registry.addSubscription(subscription, fifo);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void registerGeneric(Class eventClass, String methodName,
			boolean fifo) {
		Method handlerMethod = null, methods[];
		try {
			methods = this.getClass().getMethods();

			for (int i = 0; i < methods.length; i++) {
				if (methods[i].getName().equals(methodName)) {
					handlerMethod = methods[i];
					break;
				}
			}

			if (handlerMethod == null)
				throw new NoSuchMethodException("Generic method not found");

			Subscription subscription = new Subscription(this, handlerMethod,
					eventClass);

			// Joel and ahmad-edit: testing
			registry.addSubscription(subscription, fifo);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * TODO
	 * 
	 * @param eventClass
	 * @param method
	 * @param componentToExclude
	 */
	protected void registerDependentEvent(Class eventClass, String method,
			Class[] componentsToExclude) {

		Class[] args = new Class[1];

		args[0] = eventClass;
		Method handlerMethod;
		try {
			handlerMethod = this.getClass().getMethod(method, args);

			Subscription subscription = new Subscription(this, handlerMethod,
					args[0]);

			eventsRegistry.addDependentSubscription(subscription,
					componentsToExclude);

		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Register an EventConsumer in the Consumerregistry
	 * 
	 * @param method
	 * @param messageType
	 */

	protected void registerConsumer(String method, Class messageType) {
		Class[] args = new Class[1];

		args[0] = DeliverMessageEvent.class;

		Method handlerMethod = null;
		try {
			handlerMethod = this.getClass().getMethod(method, args);
		} catch (SecurityException e) {
			// TODO handle exception
		} catch (NoSuchMethodException e) {
			// TODO handle exception
		}

		EventConsumer eventConsumer = new EventConsumer(this, handlerMethod);

		eventsRegistry.addConsumer(messageType, eventConsumer);

	}

	/**
	 * Registers an hook in the {@link HooksRegistry}
	 * 
	 * @param hookNumber
	 * @param component
	 * @param handler
	 */

	protected void registerHook(int hookNumber, Component component,
			String handler) {

		Class[] args = new Class[1];

		args[0] = Object.class;

		Method handlerMethod = null;
		try {
			handlerMethod = component.getClass().getMethod(handler, args);
		} catch (SecurityException e) {
			// TODO handle exception
		} catch (NoSuchMethodException e) {
			// TODO handle exception
		}

		// Creating Hook
		Hook hook = new Hook(component, handlerMethod);

		// Registering hook
		hooksRegistry.registerHook(hookNumber, hook);
	}
}
