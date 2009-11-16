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

/**
 * The <code>DirectIntervalAggregationSubTotalMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: DirectIntervalAggregationSubTotalMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class DirectIntervalAggregationSubTotalMessage extends Message {

	/**
	 * @serialVersionUId - 
	 */
	private static final long serialVersionUId = 1051220295170123044L;

	/**
	 * The values corresponding to the request recieved from the interval broadcast
	 * 
	 */
	ArrayList<Object> values;
	
	/**
	 *	The <code>DKSRef</code> of the node initiated the interval broadcast.
	 *
	 *	<p>The aggregation result should be sent to the initiator.</p>
	 *
	 */
	DKSRef initiator;
	
	/**
	 *	The interval broadcasr instance Id relative to the initiator.
	 *
	 *   <p>The unique Id should be the compound "initiator:instanceId".</p>
	 *   
	 */
	BigInteger instanceId;
	
	DKSRef src;
	
	
	
	/**
	 * 
	 */
	public DirectIntervalAggregationSubTotalMessage() {
		super();
		// TODO Auto-generated constructor stub
	}


	
	public DKSRef getInitiator() {
		return initiator;
	}

	public void setInitiator(DKSRef initiator) {
		this.initiator = initiator;
	}

	public BigInteger getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(BigInteger instanceId) {
		this.instanceId = instanceId;
	}

	public DKSRef getSrc() {
		return src;
	}

	public void setSrc(DKSRef src) {
		this.src = src;
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}
	

}
