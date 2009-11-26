/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina.events;

//import org.apache.mina.common.IoSession;


import org.apache.mina.core.session.IoSession;

import dks.arch.Event;

/**
 * The <code>SessionGarbageCollectionEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SessionGarbageCollectionEvent.java 294 2006-05-05 17:14:14Z roberto $
 */
public class SessionGarbageCollectionEvent extends Event {

	public SessionGarbageCollectionEvent() {
		
	}
	public SessionGarbageCollectionEvent(IoSession session) {
		this.attachment = session;
	}
}