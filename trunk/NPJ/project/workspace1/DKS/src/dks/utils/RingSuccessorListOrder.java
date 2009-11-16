/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dks.addr.DKSRef;

/**
 * The <code>SuccessorListOrder</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingSuccessorListOrder.java 496 2007-12-20 15:39:02Z roberto $
 */
public class RingSuccessorListOrder {

	/**
	 * modularly orders the List of {@link DKSRef}, starting form the reference
	 * passed
	 * 
	 * @param list
	 *            The List of {@link DKSRef}
	 * @param startRef
	 *            The starting reference
	 */
	public static void order(List<DKSRef> list, DKSRef startRef) {
		Collections.sort(list);
		List<DKSRef> temp_list = new LinkedList<DKSRef>();
		int centralindex = -1;
		for (DKSRef ref : list) {
			if (ref.compareTo(startRef) > 0) {
				centralindex = list.indexOf(ref);
				break;
			}
		}
		if (centralindex != -1 && centralindex != 0) {
			int i = centralindex;
			int size = list.size();
			if (size != 1) {
				for (Iterator iter = list.iterator(); iter.hasNext();) {
					if (i == (size))
						i = 0;

					if (!temp_list.contains(list.get(i))) {
						temp_list.add(list.get(i));
					}
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

	// public static void orderConnections(List<ConnectionState> list,
	// DKSRef startRef) {
	// Collections.sort(list);
	// List<ConnectionState> temp_list = new LinkedList<ConnectionState>();
	// int centralindex = -1;
	// for (ConnectionState connection : list) {
	// if (connection.peerDKSRef.compareTo(startRef) > 0) {
	// centralindex = list.indexOf(connection);
	// break;
	// }
	// }
	// if (centralindex != -1 && centralindex != 0) {
	// int i = centralindex;
	// int size = list.size();
	// if (size != 1) {
	// for (Iterator iter = list.iterator(); iter.hasNext();) {
	// if (i == (size))
	// i = 0;
	//
	// temp_list.add(list.get(i));
	// i++;
	// if (i == (centralindex)) {
	// break;
	// }
	// }
	// list.clear();
	// list.addAll(temp_list);
	//			}
	//		}
	//
	//	}

}
