/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.stats;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The <code>Statistics</code> class
 * 
 * @author Cosmin Arad
 * @author Roberto Roverso
 * @version $Id: NodeStatistics.java 458 2007-11-30 00:29:11Z roberto $
 */
public class NodeStatistics {

	/**
	 * the total number of sent bytes
	 */
	public static AtomicLong bytesSent = new AtomicLong(0);

	/**
	 * the total number of received bytes
	 */
	public static AtomicLong bytesReceived = new AtomicLong(0);

	/**
	 * the total number of sent messages
	 */
	public static AtomicLong messagesSent = new AtomicLong(0);

	/**
	 * the total number of received messages
	 */
	public static AtomicLong messagesReceived = new AtomicLong(0);

	/**
	 * Unacked messages
	 */
	public static AtomicLong unackedMessages = new AtomicLong(0);

	public static AtomicLong lookupIssued = new AtomicLong(0);

	public static AtomicLong duplicateLookups = new AtomicLong(0);

	public static AtomicLong lookupSucceeded = new AtomicLong(0);
}
