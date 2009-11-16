/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.web.jetty.util;

import java.util.Comparator;

import org.apache.mina.core.session.IoSession;

//import org.apache.mina.common.IoSession;

import dks.addr.DKSRef;
import static dks.comm.mina.CommunicationConstants.CONNECTION_ENDPOINT;

/**
 * The <code>SessionComparator</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SessionComparator.java 294 2006-05-05 17:14:14Z roberto $
 */
public class SessionIDComparator implements Comparator<IoSession> {

	public int compare(IoSession o1, IoSession o2) {

		DKSRef myRef = (DKSRef) o1.getAttribute(CONNECTION_ENDPOINT);
		DKSRef otherRef = (DKSRef) o2.getAttribute(CONNECTION_ENDPOINT);

		return myRef.compareTo(otherRef);

	}

}
