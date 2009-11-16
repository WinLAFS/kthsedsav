/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.arch;

import static dks.arch.EventPriorityTable.NORMAL_PRIORITY;
import static dks.arch.EventPriorityTable.TIMER_PRIORITY;

import java.util.Comparator;

/**
 * The <code>PriorityEventComparator</code> class
 * 
 * Priority comparator for the Prioritized Queue of the scheduler's events 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: PriorityEventComparator.java 153 2007-01-24 11:01:31Z Roberto $
 */
public class PriorityEventComparator implements Comparator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object event0, Object event1) {
		int event0Priority = ((Event) event0).priority;
		int event1Priority = ((Event) event1).priority;

		int first, second;

		switch (event0Priority) {
		case TIMER_PRIORITY:
			first = 0;
			break;
		case NORMAL_PRIORITY:
			first = 1;
			break;

		default:
			first = 2;
			break;
		}

		switch (event1Priority) {
		case TIMER_PRIORITY:
			second = 0;
			break;
		case NORMAL_PRIORITY:
			second = 1;
			break;

		default:
			second = 2;
			break;
		}

		return first - second;

	}
}
