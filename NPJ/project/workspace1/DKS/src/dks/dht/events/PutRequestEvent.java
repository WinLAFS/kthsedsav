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
import dks.dht.DHTComponent.putFlavor;


/**
 * The <code>PutRequestEvent</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: PutRequestEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class PutRequestEvent extends Event {
	
	Object key;
	Object value;
	putFlavor flavor;
	boolean multiVal;
	
	int operationId;
	/**
	 * 
	 */
	public PutRequestEvent() {
		super();
		flavor = putFlavor.PUT_OVERWRITE;
		multiVal=false;
	}
	
	public putFlavor getFlavor() {
		return flavor;
	}
	public void setFlavor(putFlavor flavor) {
		this.flavor = flavor;
	}
	public Object getKey() {
		return key;
	}
	public void setKey(Object key) {
		this.key = key;
	}
	public boolean isMultiVal() {
		return multiVal;
	}
	public void setMultiVal(boolean multiVal) {
		this.multiVal = multiVal;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
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
