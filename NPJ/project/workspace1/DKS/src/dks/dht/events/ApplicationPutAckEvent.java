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
import dks.dht.messages.PutAckMessage;

/**
 * The <code>GetRequestEvent</code> class
 *
 * @author Joel
 * @version $Id: GetRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ApplicationPutAckEvent extends Event {
	boolean result;
	int operationId;
	
	public ApplicationPutAckEvent(PutAckMessage m) {
		this.result = m.getResult();
		this.operationId = m.getOperationId();
	}
	
	public boolean getResult(){
		return result;
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
