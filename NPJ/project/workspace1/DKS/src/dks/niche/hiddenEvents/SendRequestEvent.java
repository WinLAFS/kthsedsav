/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import java.io.Serializable;
import java.math.BigInteger;

//import org.apache.mina.common.WriteFuture;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.bcast.IntervalBroadcastInfo;
import dks.messages.Message;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.SendClassInterface;

/**
 * The <code>SendRequestEvent</code> class
 *
 * @author Joel
 * @version $Id: SendRequestEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class SendRequestEvent extends Event {

	
	public static final int SEND_TO_ID = 1;
	public static final int SEND_TO_NODE = 16;
	
	public static final int SEND_TO_MANAGEMENT = 128;
	
	public static final int REQUEST_MESSAGE = 64;
	
	public static final int SEND_TO_ID_RANGE = 256;
	
	public static final int REQUEST_ID = 512;
	
	//public static final int WITH_RETURN_VALUE = 8;
	
		
	
//	public static final int SEND_TO_MANAGEMENT_ID = 0;
//	public static final int SEND_TO_MANAGEMENT_NODE = 1;
//	public static final int REQUEST_FROM_MANAGEMENT_ID = 2;
//	public static final int REQUEST_FROM_MANAGEMENT_ID_RANGE = 12;
//	public static final int REQUEST_FROM_MANAGEMENT_NODE = 3;
//	//public static final int SEND_TO_ID = 4;
//	//public static final int SEND_TO_NODE = 5;
//	public static final int REQUEST_FROM_NODE = 6;
//	public static final int REQUEST_ID = 10;
	
	private BigInteger destinationId;
	//private BigInteger secondDestinationId;
	
	private DKSRef destinationNode;
	protected Serializable message;
	private MessageManagerInterface messageManager;
	private IntervalBroadcastInfo info;
	int type;
	
	NicheNotifyInterface initiator;
	
	public SendRequestEvent() {
		
	}
	public SendRequestEvent(BigInteger destinationId, Serializable message, int type) {
		this.destinationId = destinationId;
		this.message = message;
		this.type = type;
	}
	
//	public SendRequestEvent(DKSRef destinationNode, Object message, int type) {
//		this.destinationNode = destinationNode;
//		this.message = message;
//		this.type = type;
//	}

	public SendRequestEvent(DKSRef destinationNode, BigInteger destinationId, Message message, NicheNotifyInterface initiator, MessageManagerInterface messageManager, int type) {
		this.destinationNode = destinationNode;
		this.destinationId = destinationId;
		this.message = message;
		this.initiator = initiator;
		this.messageManager = messageManager;
		this.type = type;
	}

	public BigInteger getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(BigInteger destinationId) {
		this.destinationId = destinationId;
	}

	public DKSRef getDestinationNode() {
		return destinationNode;
	}

	public void setDestinationNode(DKSRef destinationNode) {
		this.destinationNode = destinationNode;
	}

	public Serializable getAttachedObject() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public NicheNotifyInterface getInitiator() {
		return initiator;
	}

	public void setInitiator(NicheNotifyInterface initiator) {
		this.initiator = initiator;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public MessageManagerInterface getMessageManager() {
		return messageManager; 
	}
	
	public void setInfo(IntervalBroadcastInfo info) {
		this.info = info;
		this.type = SEND_TO_ID_RANGE & REQUEST_MESSAGE; // REQUEST_FROM_MANAGEMENT_ID_RANGE;
	}
	public IntervalBroadcastInfo getInfo() {
		return info;
	}
}
