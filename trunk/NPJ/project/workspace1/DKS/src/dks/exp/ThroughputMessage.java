/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.exp;

import dks.messages.Message;

/**
 * The <code>ThroughputMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ThroughputMessage.java 294 2006-05-05 17:14:14Z roberto $
 */
public class ThroughputMessage extends Message {

//	private byte[] data;
	
	private static final long serialVersionUID = -3773535845903520997L;

	/**
	 * @param data
	 */
	public ThroughputMessage(int size) {
		super();
//		data = new byte[size-116];
	}
}
