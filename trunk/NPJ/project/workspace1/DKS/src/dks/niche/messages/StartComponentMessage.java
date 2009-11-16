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
import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>StartComponentMessage</code> class
 *
 * @author Joel
 * @version $Id: StartComponentMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class StartComponentMessage extends Message implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 2456353521002797985L;
	IdentifierInterface componentId;
	public StartComponentMessage() {
		
	}
	public StartComponentMessage(IdentifierInterface c) {
		this.componentId = c;
	}
	
	public IdentifierInterface getComponentId() {
		return componentId;
	}
}
