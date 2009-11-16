/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.events.DeliverMessageEvent;
import examples.events.ExampleRequestEvent;
import examples.events.ExampleResponseEvent;
import examples.messages.SimpleMessage;

/**
 * The <code>SimpleComponent</code> class This is an example of a very simple
 * component, it is able to trigger/handle events and send and receive messages
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SimpleComponent.java 582 2008-03-25 14:05:35Z ahmad $
 */
public class SimpleComponent extends CommunicatingComponent {

	private DKSRef myDKSRef;

	public SimpleComponent(Scheduler scheduler, ComponentRegistry registry) {
		super(scheduler, registry);

		/*
		 * If you need the identifier of this peer
		 * 
		 */

		this.myDKSRef = registry.getRingMaintainerComponent().getMyDKSRef();

		registerForEvents();

		registerConsumers();

	}

	/**
	 * Registers the consumer for the messages that the component wants to
	 * receive. Consumers make possible to receive a message directly from the
	 * Marshaler without waking up all the components that want to receive a
	 * message
	 */
	private void registerConsumers() {
		/*
		 * A registration for a consumer is done by passing the class of the
		 * message the component wants to receive and the name of the handler of
		 * the message
		 */
		registerConsumer("handleSimpleMessage", SimpleMessage.class);
	}

	/**
	 * Registers all the events that the component wants to receive
	 */
	public void registerForEvents() {
		/*
		 * The registration for an event is made by passing the class of the
		 * event that the component wants to receive and the name of the handler
		 * that will process the specified event
		 */
		register(ExampleRequestEvent.class, "handleExampleEvent");

	}

	public void handleExampleEvent(ExampleRequestEvent event) {

		int value = event.getAttribute();

		// Process the event ...

		value++;

		/*
		 * An event can be triggered at any time, calling the "trigger" method
		 */
		ExampleResponseEvent responseEvent = new ExampleResponseEvent(value);
		trigger(responseEvent);

		/*
		 * A component can directly send messages to another one. This is done
		 * by calling the inherited "send" method passing both the destination
		 * and the source reference
		 */
		DKSRef destination = null;

		try {
			destination = new DKSRef("dks://127.0.0.1:12345/10");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		SimpleMessage simpleMessage = new SimpleMessage();
		send(simpleMessage, myDKSRef, destination);

	}

	/**
	 * 
	 * Handler for the {@link SimpleMessage}. The handler will receive a
	 * {@link DeliverMessageEvent} with attached the message.
	 */
	public void handleSimpleMessage(DeliverMessageEvent event) {

		SimpleMessage simpleMessage = (SimpleMessage) event.getMessage();

		int carriedValue = simpleMessage.getCarriedValue();

		carriedValue++;
	}
}