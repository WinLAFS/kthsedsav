/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.bcast.events;

import java.math.BigInteger;
import java.util.ArrayList;

import dks.addr.DKSRef;
import dks.arch.Event;

/**
 * The <code>RecursiveIntervalAggregationMyValueEvent</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: RecursiveIntervalAggregationMyValueEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class RecursiveIntervalAggregationMyValueEvent extends Event {
	
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
	 *	The interval broadcasr instance ID relative to the initiator.
	 *
	 *   <p>The unique ID should be the compound "initiator:instanceId".</p>
	 *   
	 */
	BigInteger instanceId;

	/**
	 * 
	 */
	public RecursiveIntervalAggregationMyValueEvent() {
		super();
		values = new ArrayList<Object>();
	}

	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}
	
	public void addValue(Object o) {
		values.add(o);
	}
	
	public Object getValue(int i){
		return values.get(i);
	}
	
	public int size() {
		return values.size();
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
	
	public String getUniqueId(){
		return initiator.getId()+":"+instanceId;
	}
	
}
