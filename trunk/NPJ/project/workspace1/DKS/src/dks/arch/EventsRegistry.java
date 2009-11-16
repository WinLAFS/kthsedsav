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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import dks.comm.mina.events.DeliverMessageEvent;
import dks.messages.Message;

/**
 * The <code>EventConsumerRegistry</code> class
 * 
 * The EventConsumerRegistry is used to determine which component has to use
 * directly one type of message without issueing a different event for every
 * kind of message received
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: EventsRegistry.java 496 2007-12-20 15:39:02Z roberto $
 */
public class EventsRegistry {

	/*#%*/ private static Logger log = Logger.getLogger(EventsRegistry.class);

	/**
	 * Map of the events associated with their subscriptions
	 */
	private HashMap<Class, List<Subscription>> subscriptions;

	/**
	 * Map containing message types associated with their consumers
	 */
	private HashMap<Class<Message>, Set<EventConsumer>> consumersRegistry;

	/**
	 * Table containing all the dependencies between WorkingPairs(Event class,
	 * Component class)
	 */
	private HashMap<WorkingPair, Set<WorkingPair>> dependenciesTable;

	/**
	 * Table containing all the general dependencies between a
	 * WorkingPairs(Event class, Component class) and the components that must
	 * be excluded from execution while the Working Pair is running
	 */
	private HashMap<WorkingPair, Set<Class>> eventComponentDependenciesTable;

	private Set<Class> FIFOevents = new HashSet<Class>();

	public EventsRegistry() {
		subscriptions = new HashMap<Class, List<Subscription>>();
		consumersRegistry = new HashMap<Class<Message>, Set<EventConsumer>>();
		dependenciesTable = new HashMap<WorkingPair, Set<WorkingPair>>();
		eventComponentDependenciesTable = new HashMap<WorkingPair, Set<Class>>();
		FIFOevents = new HashSet<Class>();

	}

	/**
	 * Adds a consumer to the corresponding message type
	 * 
	 */
	public void addConsumer(Class messageType, EventConsumer eventConsumer) {
		if (!consumersRegistry.containsKey(messageType)) {
			Set<EventConsumer> set = new HashSet<EventConsumer>();
			set.add(eventConsumer);
			consumersRegistry.put(messageType, set);
		} else {
			/*#%*/ log.debug("Registering a second consumer " + messageType);
			Set set = consumersRegistry.get(messageType);
			set.add(eventConsumer);
		}
		/* we make the Message Delivery Event a FIFO event for all messages */
		FIFOevents.add(DeliverMessageEvent.class);

		Class consumerComponentClass = eventConsumer.getComponent().getClass();

		Class eventConsumerClass = DeliverMessageEvent.class;

		updateDependencies(eventConsumerClass, consumerComponentClass);

	}

	/**
	 * Adds a subscription to the corresponding event excluding from execution
	 * the components that are concurrent to that event
	 */
	public void addDependentSubscription(Subscription subscription,
			Class[] componentsToExclude) {

		addToSubscriptionsTable(subscription);

		Class subscriptionComponentClass = subscription.getComponent()
				.getClass();

		Class subscriptionEventClass = subscription.getEventClass();

		WorkingPair subscriptionPair = new WorkingPair(subscriptionEventClass,
				subscriptionComponentClass);

		addDependencies(subscriptionPair, componentsToExclude);

		printDependencyTable();

	}

	/**
	 * Adds a subscription to the corresponding event
	 */
	private void addSubscription(Subscription sub) {

		addToSubscriptionsTable(sub);

		Class subscriptionComponentClass = sub.getComponent().getClass();

		Class subscriptionEventClass = sub.getEventClass();

		updateDependencies(subscriptionEventClass, subscriptionComponentClass);

		printDependencyTable();

	}

	/**
	 * Adds a subscription to the corresponding event
	 */
	public void addSubscription(Subscription sub, boolean fifo) {
		if (fifo)
			FIFOevents.add(sub.getEventClass());
		addSubscription(sub);
	}

	/**
	 * Adds a dependency relation between the {@link WorkingPair} passed and all
	 * the events of the components passed to exclude them from concurrent
	 * execution
	 * 
	 * @param subscriptionPair
	 *            The {@link WorkingPair}
	 * @param componentsToExclude
	 *            The components whose events must be excluded when
	 *            subscriptionPair is running
	 */
	private void addDependencies(WorkingPair subscriptionPair,
			Class[] componentsToExclude) {

		for (int i = 0; i < componentsToExclude.length; i++) {
			Class componentClass = componentsToExclude[i];
			addToComponentsToExclude(subscriptionPair, componentClass);
		}

		Set<WorkingPair> dependenciesSet = new HashSet<WorkingPair>();

		/* Adding to consumer's WorkingPairs */

		/* For All the Consumer's types */
		for (Set<EventConsumer> consumers : consumersRegistry.values()) {

			/* For all the Consumer's subscriptions of the specifies type */
			for (EventConsumer consumer : consumers) {

				/* Class of the component which registered the consumer */
				Class processedComponentClass = consumer.getComponent()
						.getClass();

				for (int i = 0; i < componentsToExclude.length; i++) {

					Class componentToexclude = componentsToExclude[i];

					if (processedComponentClass.equals(componentToexclude)) {

						/*
						 * Adding to the Set of dependencies of the new
						 * WorkingPair
						 */
						WorkingPair processedPair = new WorkingPair(
								DeliverMessageEvent.class,
								processedComponentClass);

						dependenciesSet.add(processedPair);

						/*
						 * Adding to the Set of dependencies of the processed
						 * Consumer (WorkingPair)
						 */

						addToDependencyTable(processedPair, subscriptionPair);

					}
				}

			}

		}

		/* Adding to normal event's WorkingPairs */

		/* For all the events that have a subscription */
		for (List<Subscription> subscriptionSet : subscriptions.values()) {

			/* For all the subscriptions to that event */
			for (Subscription tempSubscription : subscriptionSet) {

				/* Class of the component which registered the consumer */
				Class processedComponentClass = tempSubscription.getComponent()
						.getClass();

				for (int i = 0; i < componentsToExclude.length; i++) {

					Class componentToexclude = componentsToExclude[i];

					if (processedComponentClass.equals(componentToexclude)) {

						WorkingPair pair = new WorkingPair(tempSubscription
								.getEventClass(), processedComponentClass);

						dependenciesSet.add(pair);

						/*
						 * Adding to the Set of dependencies of the processed
						 * Subscription (WorkingPair)
						 */

						addToDependencyTable(pair, subscriptionPair);

					}
				}

			}

		}

		dependenciesTable.put(subscriptionPair, dependenciesSet);
	}

	/**
	 * Updates the table of dependencies according to the event-component couple
	 * passed
	 * 
	 */
	private void updateDependencies(Class subscriptionEventClass,
			Class subscriptionComponentClass) {
		WorkingPair subscriptionPair = new WorkingPair(subscriptionEventClass,
				subscriptionComponentClass);

		for (WorkingPair workingPair : eventComponentDependenciesTable.keySet()) {

			for (Class componentToExclude : eventComponentDependenciesTable
					.get(workingPair)) {

				if (componentToExclude.equals(subscriptionComponentClass)) {
					addToDependencyTable(subscriptionPair, workingPair);

					addToDependencyTable(workingPair, subscriptionPair);
				}
			}
		}
	}

	private void addToComponentsToExclude(WorkingPair pair, Class componentClass) {

		if (pair.getComponentClass().equals(componentClass)) {
			return;
		}

		if (eventComponentDependenciesTable.containsKey(pair)) {
			eventComponentDependenciesTable.get(pair).add(componentClass);

		} else {
			Set<Class> dependenciesSet = new HashSet<Class>();
			dependenciesSet.add(componentClass);
			eventComponentDependenciesTable.put(pair, dependenciesSet);
		}

		// Set<WorkingPair> keyPairs = eventComponentDependenciesTable.keySet();
		//
		// for (WorkingPair pair1 : keyPairs) {
		// log.debug("KEYPAIR: " + pair1.getComponentClass() + "-"
		// + pair1.getEventClass());
		//
		// Set<Class> values = eventComponentDependenciesTable.get(pair1);
		//
		// log.debug("VALUES");
		// for (Class pair2 : values) {
		// log.debug("Class: " + pair2);
		// }
		// log.debug("+++++++++++++++++++");
		// }
		// log.debug("------------------");

	}

	private void addToSubscriptionsTable(Subscription subscription) {
		if (subscriptions.containsKey(subscription.getEventClass())) {
			List<Subscription> eventSubscriptions = subscriptions
					.get(subscription.getEventClass());
			if (!eventSubscriptions.contains(subscription))
				eventSubscriptions.add(subscription);
		} else {
			List<Subscription> eventSubscription = new LinkedList<Subscription>();
			eventSubscription.add(subscription);
			subscriptions.put(subscription.getEventClass(), eventSubscription);
		}
	}

	private void addToDependencyTable(WorkingPair pair,
			WorkingPair subscriptionPair) {

		if (dependenciesTable.containsKey(pair)) {
			dependenciesTable.get(pair).add(subscriptionPair);

		} else {
			Set<WorkingPair> dependenciesSet = new HashSet<WorkingPair>();
			dependenciesSet.add(subscriptionPair);
			dependenciesTable.put(pair, dependenciesSet);
		}
	}

	private void printDependencyTable() {

		if (!dependenciesTable.isEmpty()) {
			Set<WorkingPair> keyPairs = dependenciesTable.keySet();

			for (WorkingPair pair : keyPairs) {
				/*#%*/ log.debug("KEYPAIR: " + pair.getComponentClass() + "-"
				/*#%*/ 		+ pair.getEventClass());

				Set<WorkingPair> values = dependenciesTable.get(pair);

				/*#%*/ log.debug("VALUEPAIRS");
				/*#%*/ for (WorkingPair pair2 : values) {
					/*#%*/ log.debug("PAIR: " + pair2.getComponentClass() + "-"
					/*#%*/ 		+ pair2.getEventClass());
				/*#%*/ }
				/*#%*/ log.debug("+++++++++++++++++++");
			}
			/*#%*/ log.debug("------------------");
		}

	}

	public Set<EventConsumer> getEventConsumerSet(Class messageType) {
		if (consumersRegistry.containsKey(messageType))
			return consumersRegistry.get(messageType);
		return null;
	}

	/**
	 * Gets all the subscription associated with an event in the registry
	 */
	public List<Subscription> getSubscriptions(Class eventClass) {
		if (subscriptions.containsKey(eventClass)) {
			return subscriptions.get(eventClass);
		} else {
			return null;
		}
	}

	/**
	 * @param pair
	 * @return
	 */
	public Set<WorkingPair> getDependenciesSet(WorkingPair pair) {
		return dependenciesTable.get(pair);
	}

	/**
	 * @return Returns the fIFOevents.
	 */
	public Set<Class> getFIFOevents() {
		return FIFOevents;
	}

}
