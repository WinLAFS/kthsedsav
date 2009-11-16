/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.exp;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSNode;

/**
 * The <code>DKSExpNode</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSExpNode.java 294 2006-05-05 17:14:14Z roberto $
 */
public class DKSExpNode extends DKSNode {

	private MessageThroughputComponent messageTcomp;

	private LookupThroughputComponent lookupTcomp;

	private EventSenderThroughputComponent senderTcomp;

	private EventReceiverThroughputComponent receiverTcomp;

	private BroadcastThroughputComponent broadcastTcomp;

	/**
	 * @param myRef
	 * @param dksParameters
	 * @param webcacheAddress
	 */
	public DKSExpNode(DKSRef myRef, DKSParameters dksParameters,
			String webcacheAddress) {
		super(myRef, dksParameters, webcacheAddress);

		this.messageTcomp = new MessageThroughputComponent(scheduler, registry);

		this.lookupTcomp = new LookupThroughputComponent(scheduler, registry);

		this.receiverTcomp = new EventReceiverThroughputComponent(scheduler,
				registry);

		this.senderTcomp = new EventSenderThroughputComponent(scheduler,
				registry, receiverTcomp);

		this.broadcastTcomp = new BroadcastThroughputComponent(scheduler,
				registry);

	}

	public MessageThroughputComponent getMessageThroughputComponent() {
		return messageTcomp;
	}

	public LookupThroughputComponent getLookupTcomp() {
		return lookupTcomp;
	}

	/**
	 * @return Returns the senderTcomp.
	 */
	public EventSenderThroughputComponent getSenderTcomp() {
		return senderTcomp;
	}

	/**
	 * @return Returns the receiverTcomp.
	 */
	public EventReceiverThroughputComponent getReceiverTcomp() {
		return receiverTcomp;
	}

	public BroadcastThroughputComponent getBroadcastTcomp() {
		return broadcastTcomp;
	}

}
