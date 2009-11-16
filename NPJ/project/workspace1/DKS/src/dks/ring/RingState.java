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

import dks.addr.DKSRef;

/**
 * The <code>RingPointers</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingState.java 627 2008-07-11 23:17:56Z joel $
 */
public class RingState {
	public DKSRef successor=null;

	public DKSRef predecessor=null;

	public DKSRef oldSuccessor=null;
	
	public DKSRef oldPredecessor=null;
	
	public SuccessorList successorList;
	
	public RingStatus status=RingStatus.JOIN_REQ;
	
	public boolean lockTaken=false;
	
	public boolean joinForward=false;
	
	public boolean leaveForward=false;
	
	/**
	 * Initilaize the datas needed to manage the state of the ring
	 * @param ringMaintainer 
	 * @param n
	 */
	public RingState(RingMaintenanceComponentInt ringMaintainer, DKSRef n) {
		this.successorList=new SuccessorList(this,n,ringMaintainer);
	}
	
}
