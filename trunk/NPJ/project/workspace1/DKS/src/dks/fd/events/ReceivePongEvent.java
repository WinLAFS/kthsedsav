/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd.events;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>ReceivePongEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ReceivePongEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ReceivePongEvent extends Event {

	private DKSRef source;
	public ReceivePongEvent(DKSRef source) {
		this.source = source;
	}
	public DKSRef getSource() {
		return source;
	}
}
