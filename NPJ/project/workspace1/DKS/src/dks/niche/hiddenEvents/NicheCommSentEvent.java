/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import dks.arch.Event;
import dks.comm.SendJob;
import dks.comm.mina.SendNotifyHelper;

/**
 * The <code>CommSentEvent</code> class
 *
 * @author Joel
 * @version $Id: CommSentEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheCommSentEvent extends Event {

	SendJob sendJob;
	public NicheCommSentEvent() {
		
	}
	public NicheCommSentEvent(SendJob sendJob) {
		this.sendJob = sendJob;
	}
	public Object getAttachment() {
		return sendJob;
	}
	
}
