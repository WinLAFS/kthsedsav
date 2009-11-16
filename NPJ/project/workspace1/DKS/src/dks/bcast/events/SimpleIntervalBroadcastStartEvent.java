/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast.events;

import dks.arch.Event;
import dks.bcast.IntervalBroadcastInfo;

/**
 * The <code>SimpleIntervalBroadcastStartEvent</code> class
 * This event initiates an interval broadcast.
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleIntervalBroadcastStartEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class SimpleIntervalBroadcastStartEvent extends Event {

	protected IntervalBroadcastInfo info;
	
	public IntervalBroadcastInfo getInfo() {
		return info;
	}

	public void setInfo(IntervalBroadcastInfo info) {
		this.info = info;
	}

}
