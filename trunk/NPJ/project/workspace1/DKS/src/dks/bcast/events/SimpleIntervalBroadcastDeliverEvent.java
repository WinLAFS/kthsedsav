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
 * The <code>SimpleIntervalBroadcastDeliverEvent</code> class.
 * <p>This event delivers the broadcasted message to the application.</p>
 * 
 * @author Ahmad Al-Shishtawy
 * @version $Id: SimpleIntervalBroadcastDeliverEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class SimpleIntervalBroadcastDeliverEvent extends Event {
protected IntervalBroadcastInfo info;
	
	public IntervalBroadcastInfo getInfo() {
		return info;
	}

	public void setInfo(IntervalBroadcastInfo info) {
		this.info = info;
	}
}
