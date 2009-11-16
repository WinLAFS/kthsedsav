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
 * The <code>EventHandlerInterface</code> class
 *
 * This interface must be implemented by all management elements that want
 * to be able to subscribe to events, and have them delivered

 * @author Joel
 * @version $Id: EventHandlerInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface EventHandlerInterface {

	/**
	 * This method is invoked by the system when an event matching a previously done 
	 * subscription is delivered.
	 * 
	 * @param event
	 *            The event coming from one of the sources to which the ME is subscribed
	 *
	 * @param flag
	 *            A flag which indicates whether the event has arrived normally (value zero) or
	 *            during a period of churn, so that the ME has been moved or restored in between
	 *            event creation and event delivery            
	 *             
	 */
	public void eventHandler(Serializable event, int flag);
	
}
