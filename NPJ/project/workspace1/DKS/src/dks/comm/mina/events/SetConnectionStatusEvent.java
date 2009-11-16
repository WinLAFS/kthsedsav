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

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.comm.mina.TransportProtocol;

/**
 * The <code>SetConnectionStatusEvent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SetConnectionStatusEvent.java 294 2006-05-05 17:14:14Z roberto $
 */
public class SetConnectionStatusEvent extends Event {

	private DKSRef endPointReference;

	private TransportProtocol protocol;

	private boolean permanent;

	public SetConnectionStatusEvent(DKSRef endPointReference,
			TransportProtocol protocol, boolean permanent) {

		this.endPointReference = endPointReference;
		this.protocol = protocol;
		this.permanent = permanent;

	}

	public DKSRef getEndPointReference() {
		return endPointReference;
	}

	public TransportProtocol getProtocol() {
		return protocol;
	}

	public boolean isPermanent() {
		return permanent;
	}

}
