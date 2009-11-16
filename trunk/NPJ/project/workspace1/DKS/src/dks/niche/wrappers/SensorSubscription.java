/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.Serializable;

import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>SensorSubscription</code> class
 *
 * @author Joel
 * @version $Id: SensorSubscription.java 294 2006-05-05 17:14:14Z joel $
 */
public class SensorSubscription extends Subscription implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 7739496033828914520L;
	
	NodeRef nodeOfSource; //needed for infrastructure sensors
	IdentifierInterface broker;
	//remember empty constructor
	public SensorSubscription() {
		
	}
	public SensorSubscription(IdentifierInterface source, IdentifierInterface broker, IdentifierInterface sink, String eventName, NodeRef nodeOfSource) {
		super(source, sink, eventName);
		this.broker = broker;
		this.nodeOfSource = nodeOfSource;
	}

	public SensorSubscription(Subscription s, NodeRef nodeOfSource) {
		super(s.getSourceId(), s.getSinkId(), s.getEventName());
		this.nodeOfSource = nodeOfSource;		
	}
	public NodeRef getNodeOfSource() {
		return nodeOfSource;
	}
	public void setNodeOfSource(NodeRef nodeOfSource) {
		this.nodeOfSource = nodeOfSource;
	}
	public IdentifierInterface getBroker() {
		return broker;
	}
	
}
