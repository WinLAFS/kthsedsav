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

import dks.addr.DKSRef;
import dks.ring.OperationNumber;

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: JoinRequestMessage.java 259 2007-03-23 14:59:06Z Roberto $
 */
public class JoinResponseMessage extends Message {

	private static final long serialVersionUID = 4797952372086392258L;

	private DKSRef nodeId;

	private OperationNumber opNum;

	/**
	 * 
	 * @param nodeId
	 *            The NodeId of the d that wants to join
	 */
	public JoinResponseMessage(OperationNumber opNum, DKSRef nodeId) {
		super();
		this.nodeId = nodeId;
		this.opNum = opNum;
	}

	/**
	 * Default constructor for marshaller instantiation
	 */
	public JoinResponseMessage() {

	}

	/**
	 * @return Returns the nodeId.
	 */
	public DKSRef getNodeDKSRef() {
		return nodeId;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}
}
