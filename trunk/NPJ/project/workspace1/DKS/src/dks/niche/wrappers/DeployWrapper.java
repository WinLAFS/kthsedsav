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

import dks.niche.ids.NicheId;
import dks.niche.ids.ResourceId;

/**
 * The <code>DeployWrapper</code> class
 *
 * @author Joel
 * @version $Id: DeployWrapper.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeployWrapper implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 8689780326821499415L;
	/*
	 * Fields for each destination
	 */
	private ResourceRef destinationRef;
	private int position; 	//needed to know which nodes succeeded/failed
	private String handlerId; //only one eventName for one deployment operation
	
	/*
	 * Fields for each component
	 */
	private NicheId componentToBeDeployedId; 
	private Object componentDescription;
	
	/*
	 * Constructor for each destination
	 */
	public DeployWrapper(ResourceRef destination, int position) {
		
		this.destinationRef = destination;
		this.position = position;
	}
	
	/*
	 * Constructor for each component
	 */
	public DeployWrapper(NicheId componentToBeDeployedId, Object componentDescription)	{
		
		this.componentToBeDeployedId = componentToBeDeployedId;
		this.componentDescription = componentDescription;
	}

	public void setComponentInfo(DeployWrapper input, String handlerId) {

		this.componentToBeDeployedId = input.componentToBeDeployedId;
		this.componentDescription = input.componentDescription;
		this.handlerId = handlerId;
		
	}
	
	public DeployWrapper setComponentInfoAndGetResults(DeployWrapper input, String handlerId) {

		this.componentToBeDeployedId = input.componentToBeDeployedId;
		this.componentDescription = input.componentDescription;
		this.handlerId = handlerId;
		return this;
	}
	
	/**
	 * @return Returns the componentDescription.
	 */
	public Object getComponentDescription() {
		return componentDescription;
	}

	/**
	 * @param componentDescription The componentDescription to set.
	 */
	public void setComponentDescription(Object componentDescription) {
		this.componentDescription = componentDescription;
	}

	/**
	 * @return Returns the componentToBeDeployedId.
	 */
	public NicheId getComponentToBeDeployedId() {
		return componentToBeDeployedId;
	}

	/**
	 * @param componentToBeDeployedId The componentToBeDeployedId to set.
	 */
	public void setComponentToBeDeployedId(NicheId componentToBeDeployedId) {
		this.componentToBeDeployedId = componentToBeDeployedId;
	}

	/**
	 * @return Returns the destinationRId.
	 */
	public ResourceRef getDestinationResourceRef() {
		return destinationRef;
	}

	/**
	 * @param destinationRId The destinationRId to set.
	 */
	public void setDestinationResourceRef(ResourceRef destinationRef) {
		this.destinationRef = destinationRef;
	}

	/**
	 * @return Returns the eventName.
	 */
	public String getHandlerId() {
		return handlerId;
	}

	/**
	 * @param eventName The eventName to set.
	 */
	public void setHandlerId(String handlerId) {
		this.handlerId = handlerId;
	}

	/**
	 * @return Returns the position.
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(int position) {
		this.position = position;
	}
	

}