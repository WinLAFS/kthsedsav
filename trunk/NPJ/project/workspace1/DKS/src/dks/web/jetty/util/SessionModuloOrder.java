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

import static dks.comm.mina.CommunicationConstants.CONNECTION_ENDPOINT;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.mina.core.session.IoSession;

//import org.apache.mina.common.IoSession;

import dks.addr.DKSRef;

/**
 * The <code>SessionOrder</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: SessionOrder.java 220 2007-03-03 17:54:27Z Roberto $
 */
public class SessionModuloOrder {


	public static void orderSessions(List<IoSession> list,
			DKSRef startRef) {
		
		Collections.sort(list,new SessionIDComparator());
		
		List<IoSession> temp_list = new LinkedList<IoSession>();
		int centralindex = -1;
		for (IoSession connection : list) {
			DKSRef myRef = (DKSRef) connection.getAttribute(CONNECTION_ENDPOINT);
			if (myRef.compareTo(startRef) > 0) {
				centralindex = list.indexOf(connection);
				break;
			}
		}
		if (centralindex != -1 && centralindex != 0) {
			int i = centralindex;
			int size = list.size();
			if (size != 1) {
				for (Iterator<IoSession> iter = list.iterator(); iter.hasNext();) {
					if (i == (size))
						i = 0;

					temp_list.add(list.get(i));
					i++;
					if (i == (centralindex)) {
						break;
					}
				}
				list.clear();
				list.addAll(temp_list);
			}
		}

	}

}
