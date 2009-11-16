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
import dks.bcast.interfaces.PseudoReliableIntervalBroadcastAckInterface;

/**
 * The <code>PseudoReliableIntervalBroadcastAckEvent</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: PseudoReliableIntervalBroadcastAckEvent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class PseudoReliableIntervalBroadcastAckEvent extends Event implements PseudoReliableIntervalBroadcastAckInterface {
	
	ArrayList<Object> values;
	DKSRef initiator;
	BigInteger instanceID;
	Boolean aggregate;
	
	public ArrayList<Object> getValues() {
		return values;
	}

	public void setValues(ArrayList<Object> values) {
		this.values = values;
	}

	public BigInteger getInstanceId() {
		return instanceID;
	}

	public void setInstanceId(BigInteger instanceID) {
		this.instanceID = instanceID;
	}

	public Boolean getAggregate() {
		return aggregate;
	}

	public void setAggregate(Boolean aggregate) {
		this.aggregate = aggregate;
	}

	public DKSRef getInitiator() {
		return initiator;
	}

	public void setInitiator(DKSRef initiator) {
		this.initiator = initiator;
	}
	
	public String getUniqueId(){
		return initiator.getId()+":"+instanceID;
	}
	

}
