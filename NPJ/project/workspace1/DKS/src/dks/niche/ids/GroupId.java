/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.ids;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.ReliableInterface;
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>GroupId</code> class
 * 
 * @author Joel
 * @version $Id: GroupId.java 294 2006-05-05 17:14:14Z joel $
 */
public class GroupId implements Serializable, SNR, ReliableInterface {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 4371051513916660862L;

//	
//	// ArrayList<IdentifierInterface> things;
//	HashMap<String, IdentifierInterface> myReferences;

	NicheId myId;
	ArrayList<IdentifierInterface> initialReferences;
	HashMap<String, BindId> serverSideBindingsADLToBindId;
	

	boolean reliable;
	// Hmm, one solution to 'can a watcher listen to many events'(yes)
	// would be to assume that such a watcher subscribes itself as many times
	// as there are events

	// To Ahmad: I think I expect this class to be expanded by us... this was
	// supposed to be for testing only...

	
	// Remember the empty constructor
	public GroupId() {
		serverSideBindingsADLToBindId = new HashMap<String, BindId>();

	}


	// Template can be null
	public GroupId(NicheId id, SNR template) {
		
		myId = id;
		if(template != null) {
			this.serverSideBindingsADLToBindId = template.getPredefinedReceiverBindings();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.IdentifierInterface#getId()
	 */
	public NicheId getId() {
		return myId;
	}


	/**
	 * @return Returns the initialReferences.
	 */
	public ArrayList<IdentifierInterface> getInitialReferences() {
		return initialReferences;
	}


	/**
	 * @param initialReferences The initialReferences to set.
	 */
	public void setInitialReferences(ArrayList<IdentifierInterface> initialReferences) {
		this.initialReferences = initialReferences;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.SNR#getResourceRef()
	 */
	
	public ResourceRef getResourceRef() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addServerBinding(String receiverSideInterfaceDescription, int type) {

		BindId cachedBindId = new BindId();
		cachedBindId.setReceiverSideInterfaceDescription(receiverSideInterfaceDescription);
		serverSideBindingsADLToBindId.put(receiverSideInterfaceDescription, cachedBindId);

	}


	/* (non-Javadoc)
	 * @see dks.niche.ids.SNR#getPredefinedReceiverBindings()
	 */
	
	public HashMap<String, BindId> getPredefinedReceiverBindings() {
		return serverSideBindingsADLToBindId;
	}

	/**
	 * @param reliable The reliable to set.
	 */
	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ReliableInterface#isReliable()
	 */
	
	public boolean isReliable() {
		return reliable;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		
		if(obj instanceof GroupId) {
			return ((GroupId)obj).myId.equals(myId);
		}
		return false;
	}
}
