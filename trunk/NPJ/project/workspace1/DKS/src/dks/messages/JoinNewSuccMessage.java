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
 * @version $Id: JoinNewSuccMessage.java 452 2007-11-27 22:13:23Z roberto $
 */
public class JoinNewSuccMessage extends Message {

	private static final long serialVersionUID = 558798857138270421L;

	private OperationNumber opNum;

	private DKSRef d;

	/**
	 * @return Returns the d.
	 */
	public DKSRef getD() {
		return d;
	}

	/**
	 * Default constructor for marshaller instantiation
	 */
	public JoinNewSuccMessage() {
	}

	public JoinNewSuccMessage(OperationNumber opNum, DKSRef m) {
		this.opNum = opNum;
		this.d = m;
	}

	/**
	 * @return Returns the opNum.
	 */
	public OperationNumber getOpNum() {
		return opNum;
	}

}
