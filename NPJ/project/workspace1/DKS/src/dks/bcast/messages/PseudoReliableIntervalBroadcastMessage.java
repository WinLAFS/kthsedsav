/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast.messages;

import dks.bcast.IntervalBroadcastInfo;
import dks.messages.Message;

/**
 * The <code>PseudoReliableIntervalBroadcastMessage</code> class
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: PseudoReliableIntervalBroadcastMessage.java 294 2006-05-05
 *          17:14:14Z alshishtawy $
 */
public class PseudoReliableIntervalBroadcastMessage extends Message {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 4150493306836332496L;

	IntervalBroadcastInfo info;

	/**
	 * 
	 */
	public PseudoReliableIntervalBroadcastMessage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IntervalBroadcastInfo getInfo() {
		return info;
	}

	public void setInfo(IntervalBroadcastInfo info) {
		this.info = info;
	}

}
