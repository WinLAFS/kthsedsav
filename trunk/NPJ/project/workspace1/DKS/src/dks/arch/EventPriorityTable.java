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

/**
 * The <code>EventPriorityTable</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: EventPriorityTable.java 154 2007-01-24 14:43:31Z Roberto $
 */
public interface EventPriorityTable {

	public final static int MIN_PRIORITY = 0;

	public final static int TIMER_PRIORITY = 1;

	public final static int NORMAL_PRIORITY = 127;

	public final static int MAX_PRIORITY = 255;

}
