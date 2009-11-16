/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.interfaces;

import java.math.BigInteger;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.messages.Message;
import dks.niche.hiddenEvents.ManagementEvent;

/**
 * The <code>NicheMessageInterface</code> class
 *
 * @author Joel
 * @version $Id: NicheMessageInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheMessageInterface {

	//public int getOperationId();
	public void setMessageId(int messageId);
	public int getMessageId();
	public DKSRef getSource();
	public Message setSource(DKSRef ref);
	//getSource() - same as inherited from dks.Message. no need for explicit implementation!
	public BigInteger getDestinationId();
	public void setDestinationId(BigInteger destination);
	public void setDestinationNode(DKSRef destinationNode);
	public DKSRef getDestinationNode();
	public ManagementEvent getEvent();
	public void setEvent(ManagementEvent event);
	public Object getOriginalMessage();
	//public boolean acknowledgementWanted();
}
