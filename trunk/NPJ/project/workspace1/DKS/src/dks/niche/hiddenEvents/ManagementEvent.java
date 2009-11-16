/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.bcast.events.PseudoReliableIntervalBroadcastDeliverEvent;
import dks.messages.Message;
import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>ManagementEvent</code> class
 *
 * @author Joel
 * @version $Id: ManagementEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public abstract class ManagementEvent extends Event implements Serializable {

	Serializable message;
	IdentifierInterface initiator;
	boolean request;
	boolean broadcast;
	int operationId;
	
	public ManagementEvent() {
		
	}
	public ManagementEvent(Serializable message) {
		this.message = message;
	}
	public ManagementEvent(Serializable message, IdentifierInterface initiator) {
		this.message = message;
		this.initiator = initiator;
	}
	public Serializable getMessage() {
		return message;
	}
	public IdentifierInterface getInitiator() {
		return initiator;
	}
	public boolean setSource(DKSRef source) {
		if(message instanceof Message) {
			((Message)message).setSource(source);
			return true;
		}
		return false;
	}
	public boolean isRequest() {
		return request;
	}
	public void setRequest(boolean request) {
		this.request = request ;
	}
	public boolean isBroadcast() {
		return broadcast;
	}
	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}
	/**
	 * @return Returns the operationId.
	 */
	public int getOperationId() {
		return operationId;
	}
	/**
	 * @param operationId The operationId to set.
	 */
	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}
	
}
