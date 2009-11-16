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
import java.math.BigInteger;

import dks.messages.Message;

/**
 * The <code>DeployResponseMessage</code> class
 *
 * @author Joel
 * @version $Id: DeployResponseMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeployResponseMessage extends Message implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 6764852169175041325L;
	BigInteger responder;
	Object[]results;
	
	public DeployResponseMessage(BigInteger responder, Object[]results) {
		
		this.responder = responder;
		this.results = results;
	}

	public BigInteger getResponder() {
		return responder;
	}

	public void setResponder(BigInteger responder) {
		this.responder = responder;
	}

	public Object[] getResults() {
		return results;
	}

	public void setResults(Serializable[] results) {
		this.results = results;
	}

	
}
