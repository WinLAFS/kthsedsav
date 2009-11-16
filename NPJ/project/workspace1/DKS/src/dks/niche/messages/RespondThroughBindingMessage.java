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

import java.io.Serializable;

import dks.messages.Message;
import dks.niche.ids.BindElement;
import dks.niche.ids.BindId;
import dks.niche.ids.NicheId;

/**
 * The <code>RespondThroughBindingMessage</code> class
 *
 * @author Joel
 * @version $Id: RespondThroughBindingMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class RespondThroughBindingMessage extends Message implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -7238817270074128527L;
	
	
	BindId bindId;
	Serializable responseMessage;
	int operationId;
		
	/*
	 * Remember the empty constructor...
	 */
	public RespondThroughBindingMessage() {
	}
	
	
	/**
	 * @param globalBindId
	 * @param message
	 */
	public RespondThroughBindingMessage(BindId bindId, Serializable message, int operationId) {
		this.bindId = bindId;
		this.responseMessage = message;
		this.operationId = operationId;
	}
	
	public BindId getBindInfo() {
		return bindId;
	}

	public void setBindInfo(BindId bindId) {
		this.bindId = bindId;
	}

	public Serializable getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(Serializable message) {
		this.responseMessage = message;
	}
	
	public int getOperationId() {
		return operationId;
	}

}
