/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring;

/**
 * The <code>RingStatus</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingStatus.java 108 2006-11-16 13:58:37Z cosmin $
 */
public enum RingStatus {
	INSIDE, JOIN_REQ, JOINING, LEAVE_REQ, PRED_LEAVE_REQ, LEAVING, PRED_LEAVING
}
