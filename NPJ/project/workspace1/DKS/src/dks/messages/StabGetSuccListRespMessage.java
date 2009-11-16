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

/**
 * The <code>JoinRequestMessage</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: StabGetSuccListRespMessage.java 188 2007-02-09 11:31:02Z
 *          Roberto $
 */
public class StabGetSuccListRespMessage extends Message {

	private static final long serialVersionUID = -5609104703220300070L;

	private List<DKSRef> successors;

	private DKSRef respondingPeer;

	/**
	 * @param successors
	 */
	public StabGetSuccListRespMessage(DKSRef respondingPeer,
			List<DKSRef> successors) {
		super();
		this.respondingPeer = respondingPeer;
		this.successors = successors;
	}

	/**
	 * Default constructor for marshaller instantiation
	 */
	public StabGetSuccListRespMessage() {

	}

	/**
	 * @return Returns the successors.
	 */
	public List<DKSRef> getSuccessorsList() {
		return successors;
	}

	/**
	 * @return Returns the respondingPeer.
	 */
	public DKSRef getRespondingPeer() {
		return respondingPeer;
	}

}
