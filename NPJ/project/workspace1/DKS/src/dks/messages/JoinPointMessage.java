/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.messages;

import java.util.List;

import dks.addr.DKSRef;
import dks.ring.OperationNumber;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: JoinPointMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class JoinPointMessage extends Message {

	private static final long serialVersionUID = -7257520214903983152L;

	private DKSRef nodeDKSRef;

	private OperationNumber opNum;

	private List<DKSRef> succlist = null;

	/**
	 * Default constructor for marshaller instantiation
	 */
	public JoinPointMessage() {

	}

	public JoinPointMessage(OperationNumber opNum, DKSRef nodeDKSRef) {
		this.opNum = opNum;
		this.nodeDKSRef = nodeDKSRef;
	}

	public JoinPointMessage(OperationNumber opNum, DKSRef nodeDKSRef,
			List<DKSRef> succlist) {
		this.opNum = opNum;
		this.nodeDKSRef = nodeDKSRef;
		this.succlist = succlist;
	}

	/**
	 * @return Returns the nodeDKSRef.
	 */
	public DKSRef getNodeDKSRef() {
		return nodeDKSRef;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

	/**
	 * @return Returns the succlist.
	 */
	public List<DKSRef> getSucclist() {
		return succlist;
	}

}
