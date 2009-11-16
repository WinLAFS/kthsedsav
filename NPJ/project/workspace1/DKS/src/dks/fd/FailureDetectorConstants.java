/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.fd;

/**
 * The <code>FailureDetectorConstants</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: FailureDetectorConstants.java 633 2008-07-15 16:15:20Z joel $
 */
public interface FailureDetectorConstants {

	/* Failure Detector constants */

	// Constant to add to the RTO for setting the Timer C
	
	final static  String PING_TIMEOUT_FLAG = System.getProperty("dks.ping.timeout") instanceof String ? System.getProperty("dks.ping.timeout") : System.getProperty("dks.timeout");
	final static  String PING_INTERVAL_FLAG = System.getProperty("dks.ping.interval");
	
	public final static long FD_PONG_TIMEOUT_ADD_TO_RTO  = (PING_TIMEOUT_FLAG instanceof String) ? Integer.parseInt(PING_TIMEOUT_FLAG) : 15000;

	// Timer C bounds

	public final static long FD_MINIMUM_RTO = FD_PONG_TIMEOUT_ADD_TO_RTO;

	// Timer A , it must be greater than MAX(C)

	public final static long FD_PING_INTERVAL_TIMER = (PING_INTERVAL_FLAG instanceof String) ? Integer.parseInt(PING_INTERVAL_FLAG) : 2*FD_MINIMUM_RTO;

	// Number of pings to issue before giving up
	
	public final static long FD_PING_RETRY = 10;
	// Failure Detector running
//	public final static boolean FD_RUNNING = false;
}
