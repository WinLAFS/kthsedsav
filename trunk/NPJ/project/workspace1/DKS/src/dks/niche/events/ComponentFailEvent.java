/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.events;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.niche.ids.NicheId;

/**
 * The <code>ComponentFailEvent</code> class
 * 
 * @author Joel
 * @version $Id: ComponentFailEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class ComponentFailEvent implements ConfigurationEvent, Serializable {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = -2446438467775724865L;

	NicheId failedComponentId;

	NicheId affectedGroupId; // can of course be null, if the subscription

	// was made directly to the component
	DKSRef failedNode; // will also be null if the failure was caused by the
						// RM/the Jade container

	public ComponentFailEvent() {

	}

	/**
	 * @param failedComponentId
	 * @param affectedGroupId
	 * @param failedNode
	 */
	public ComponentFailEvent(NicheId failedComponentId,
			NicheId affectedGroupId, DKSRef failedNode) {
		this.failedComponentId = failedComponentId;
		this.affectedGroupId = affectedGroupId;
		this.failedNode = failedNode;
	}

	/**
	 * @return Returns the failedComponentId.
	 */
	public NicheId getFailedComponentId() {
		return failedComponentId;
	}

	// Backwards compatible...
	public NicheId getNicheId() {
		return failedComponentId;
	}

	/**
	 * @param failedComponentId
	 *            The failedComponentId to set.
	 */
	public void setFailedComponentId(NicheId failedComponentId) {
		this.failedComponentId = failedComponentId;
	}

	/**
	 * @return Returns the affectedGroupId.
	 */
	public NicheId getAffectedGroupId() {
		return affectedGroupId;
	}

	/**
	 * @param affectedGroupId
	 *            The affectedGroupId to set.
	 */
	public void setAffectedGroupId(NicheId affectedGroupId) {
		this.affectedGroupId = affectedGroupId;
	}

	/**
	 * @return Returns the failedNode.
	 */
	public DKSRef getFailedNode() {
		return failedNode;
	}

	/**
	 * @return Returns the failedNode.
	 */
	public DKSRef getDKSRef() {
		return failedNode;
	}

	/**
	 * @param failedNode
	 *            The failedNode to set.
	 */
	public void setFailedNode(DKSRef failedNode) {
		this.failedNode = failedNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.events.ConfigurationEvent#getBroker()
	 */
	@Override
	public NicheId getBroker() {
		// 
		return affectedGroupId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.events.ConfigurationEvent#getSource()
	 */
	@Override
	public String getSource() {
		// 
		return failedComponentId.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.events.ConfigurationEvent#setBroker(dks.niche.ids.NicheId)
	 */
	@Override
	public ConfigurationEvent setBroker(NicheId id) {
		affectedGroupId = id;
		return this;
	}

}
