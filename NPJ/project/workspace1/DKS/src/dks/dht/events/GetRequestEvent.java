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
import dks.dht.DHTComponent.getFlavor;

/**
 * The <code>GetRequestEvent</code> class
 *
 * @author Joel
 * @version $Id: GetRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class GetRequestEvent extends Event {

	Object key;
	getFlavor flavor;
	int position;	// only used with GET_AT
	
	//boolean multiVal;
	
	int operationId;
	/**
	 * 
	 */
	public GetRequestEvent() {
		super();
		flavor = getFlavor.GET_LAST;
		position=-1;
		//multiVal=false;
	}
	
	public getFlavor getFlavor() {
		return flavor;
	}
	public void setFlavor(getFlavor flavor) {
		this.flavor = flavor;
	}
	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}
	
	/**
	 * @return Returns the position.
	 */
	public int getPosition() {
	    return position;
	}
	
	/**
	 * @param position The position to set.
	 */
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
