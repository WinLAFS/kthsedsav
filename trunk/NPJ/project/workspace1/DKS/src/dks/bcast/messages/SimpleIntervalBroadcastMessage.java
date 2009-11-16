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
 * The <code>SimpleIntervalBroadcastMessage</code> class
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleIntervalBroadcastMessage.java 294 2006-05-05 17:14:14Z
 *          alshishtawy $
 */
public class SimpleIntervalBroadcastMessage extends Message {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = -77265294287470178L;

	IntervalBroadcastInfo info;

	/**
	 * 
	 */
	public SimpleIntervalBroadcastMessage() {
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
