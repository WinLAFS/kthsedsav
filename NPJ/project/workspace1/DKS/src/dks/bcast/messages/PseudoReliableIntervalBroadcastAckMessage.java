/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast.messages;

import java.math.BigInteger;
import java.util.ArrayList;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.utils.IntervalsList;

/**
 * The <code>PseudoReliableIntervalBroadcastMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: PseudoReliableIntervalBroadcastMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class PseudoReliableIntervalBroadcastAckMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -7291272639813118501L;


		
	
	/**
	 * If aggregating then the Ack message also carries the values
	 */
	Boolean aggregate;
	
	/**
	 * The values corresponding to the request recieved from the interval broadcast.
	 * This equals null if not aggregating.
	 */
	ArrayList<Object> values;
	
	/**
	 *	The <code>DKSRef</code> of the node initiated the interval broadcast.
	 *
	 *	<p>The aggregation result should be sent to the initiator.</p>
	 *
	 */
	DKSRef initiatorRef;
	
	/**
	 *	The interval broadcasr instance ID relative to the initiator.
	 *
	 *   <p>The unique ID should be the compound "initiator:instanceId".</p>
	 *   
	 */
	BigInteger instanceId;
	
	/**
	 * The DKSRef of the node sending this ack
	 */
	
	DKSRef senderRef;
	
	IntervalsList interval;
	
	
	
	public IntervalsList getInterval() {
		return interval;
	}

	public void setInterval(IntervalsList interval) {
		this.interval = interval;
	}

	/**
	 * 
	 */
	public PseudoReliableIntervalBroadcastAckMessage() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	

	public Boolean getAggregate() {
		return aggregate;
	}

	public void setAggregate(Boolean aggregate) {
		this.aggregate = aggregate;
	}

	public DKSRef getInitiatorRef() {
		return initiatorRef;
	}

	public void setInitiatorRef(DKSRef initiatorRef) {
		this.initiatorRef = initiatorRef;
	}

	public BigInteger getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(BigInteger instanceId) {
		this.instanceId = instanceId;
	}

	public DKSRef getSenderRef() {
		return senderRef;
	}

	public void setSenderRef(DKSRef senderRef) {
		this.senderRef = senderRef;
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}
	
	public String getUniqueId(){
		return initiatorRef.getId()+":"+instanceId;
	}
		
}
