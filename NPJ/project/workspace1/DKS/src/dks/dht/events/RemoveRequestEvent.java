/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.events;

import dks.arch.Event;
import dks.dht.DHTComponent.removeFlavor;

/**
 * The <code>RemoveRequestEvent</code> class
 *
 * @author Joel
 * @version $Id: RemoveRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class RemoveRequestEvent extends Event {

	Object key;
	removeFlavor flavor;
	int position;    //used only with REMOVE_AT

	
	int operationId;
	/**
	 * 
	 */
	public RemoveRequestEvent() {
		flavor = removeFlavor.REMOVE_ALL;
		position = -1;
	}
	

	public removeFlavor getFlavor() {
	    return flavor;
	}
	public void setFlavor(removeFlavor flavor) {
	    this.flavor = flavor;
	}
	
	public Object getKey() {
		return key;
	}
	
	public void setKey(Object key) {
		this.key = key;
	}
	
	

	public int getPosition() {
	    return position;
	}

	public void setPosition(int position) {
	    this.position = position;
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
