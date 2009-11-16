/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.messages;

import dks.messages.Message;
import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>DelegateSubscriptionMessage</code> class
 *
 * @author Joel
 * @version $Id: DelegateSubscriptionMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class DelegateSubscriptionMessage extends Message {


	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 2784240871531661424L;
	IdentifierInterface eventSource;
	String sinkName;
	String eventName;
	IdentifierInterface destination;
	/**
	 * @param source
	 * @param sinkName
	 * @param eventName
	 * @param destination
	 */
	public DelegateSubscriptionMessage(IdentifierInterface source, String sinkName, String eventName, IdentifierInterface destination) {
		this.eventSource = source;
		this.sinkName = sinkName;
		this.eventName = eventName;
		this.destination = destination;
	}
	public IdentifierInterface getDestination() {
		return destination;
	}
	public void setDestination(IdentifierInterface destination) {
		this.destination = destination;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
	public String getSinkName() {
		return sinkName;
	}
	public void setSinkName(String sinkName) {
		this.sinkName = sinkName;
	}
	public IdentifierInterface getEventSource() {
		return eventSource;
	}
	public void setEventSource(IdentifierInterface source) {
		this.eventSource = source;
	}
	
	
	
}
