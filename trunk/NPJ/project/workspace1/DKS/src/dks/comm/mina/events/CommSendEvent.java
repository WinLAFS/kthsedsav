/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina.events;

//import org.apache.mina.common.WriteFuture;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.comm.SendJob;
import dks.comm.mina.TransportProtocol;
import dks.messages.Message;

/**
 * The <code>CommSendEvent</code> class
 * 
 * @author Cosmin Arad
 * @author Roberto Roverso
 * @version $Id: CommSendEvent.java 642 2008-09-05 12:57:27Z joel $
 */
public class CommSendEvent extends Event {

	private Message message;

	private DKSRef destination;

	private TransportProtocol protocol;
	
	private int operationId;
	
	private SendJob sendJob;

	/**
	 * The message will be sent using TCP if this constructor is used
	 */
	public CommSendEvent(Message message, DKSRef destination) {
		super();
		this.message = message;
		this.destination = destination;
		this.protocol = TransportProtocol.TCP;
		this.operationId = -1;
	}
	
	public CommSendEvent(Message message, DKSRef destination, TransportProtocol protocol, SendJob sendJob) {
		super();
		this.message = message;
		this.destination = destination;
		this.protocol = protocol;
		this.sendJob = sendJob;
		this.operationId = sendJob.getOperationId();
	}

	public CommSendEvent(Message message, DKSRef destination,
			TransportProtocol protocol) {
		super();
		this.message = message;
		this.destination = destination;
		this.protocol = protocol;
		this.operationId = -1;
	}

	public Message getMessage() {
		return message;
	}

	public DKSRef getDestination() {
		return destination;
	}

	public TransportProtocol getProtocol() {
		return protocol;
	}
	
	public int getOperationId() {
		return operationId;
	}
	public SendJob getSendJob() {
		return sendJob;
	}

}
