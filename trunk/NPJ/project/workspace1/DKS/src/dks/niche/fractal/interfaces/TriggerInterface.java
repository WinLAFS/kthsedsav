/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.fractal.interfaces;

import java.io.Serializable;


/**
 * The <code>TriggerInterface</code> class
 *
 * This interface is used by management elements that want
 * to be able to trigger events

 * @author Joel
 * @version $Id: TriggerInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface TriggerInterface {

	/**
	 * Triggers an event
	 * 
	 * @param event
	 *            The event, which will be matched against current subscriptions.
	 *            All management elements which has subscribed to the triggering
	 *            element for that type of event will get notified
	 *             
	 */
	public void trigger(Serializable event);
	
	/**
	 * Triggers an event with a special tag for filtering
	 * 
	 * @param event
	 *            The event, which will be matched against current subscriptions.
	 *            If there are subscriptions matching the event type, they will also
	 *            be checked against the tag.
	 *          
	 * @param tag
	 * 			             
	 */
	public void trigger(Serializable event, Serializable tag);
	
	/**
	 * Triggers an event
	 * 
	 * @param event
	 *            The event, which will be matched against current subscriptions.
	 *            Out of the matching subscriptions, one random subscriber will get notified
	 *             
	 */
	public void triggerAny(Serializable event);
	
	public void removeSink(String sinkId);
	//public void triggerSystemEvent(Event event);
}
