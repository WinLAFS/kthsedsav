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
import java.util.HashMap;

import dks.niche.interfaces.IdentifierInterface;
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>ComponentId</code> class
 *
 * @author Joel
 * @author Ahmad
 * @version $Id: ResourceId.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class ComponentId implements Serializable, IdentifierInterface, SNR {
	
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 5390094676855647714L;
	
	NicheId globalComponentId; //the id of the SNRElement
	private NicheId realComponentId; //the id of the (current) component, "only" known by the SNRElement

	//For caching only:
	ResourceRef currentAllocatedResource;

	String componentName;
	String serializedDeployParameters;
	
	HashMap<String, BindId> serverSideBindingsADLToBindId;
	
	//deployment book-keeping
	int operationId;
	int position;	
	
	
	//Remember the empty constructor...
	public ComponentId() {
		
	}
	public ComponentId(NicheId nicheId, String componentName) {
		this.globalComponentId = this.realComponentId = nicheId;
		this.componentName = componentName;
	}
	

	public void setDKSInfo(int operationId, ResourceRef rid, int position) {
		this.operationId = operationId;
		this.currentAllocatedResource = rid;
		this.position = position;
		
	}
	
//	public DKSRef getDKSRef() {
//		//Decide: do we want to hide the dht-get? :
//		/*
//		if(timestamp < ) {
//			newDKSRefValue = myDHT.get(globalComponentId).getDKSRef();
//			store timestamp = currentTime;
//			return 
//		}
//		*/
//		return currentAllocatedResource.getDKSRef();
//	}
//	
	
//	public void setDKSRef(DKSRef dksRef) {
//		this.dksRef = dksRef;
//	}
	
	/**
	 * @return Returns the .
	 */
	public NicheId getId() {
		return globalComponentId;
	}
//	public NicheId getGlobalComponentId() {
//		return globalComponentId;
//	}
	public String getComponentName() {
		return componentName;
	}
	
	public NicheId getRealComponentId() {
		return realComponentId;
	}
	
	/**
	 * @return Returns the localID.
	 *	public Object getLocalComponentId() {		return localComponentId;	}
	/**
	
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
	/**
	 * @return Returns the operationId.
	 */
	public int getOperationId() {
		return operationId;
	}
	/**
	 * @param operationId The operationId to set.
	 */
	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(NicheId globalId) {
		this.globalComponentId = globalId;
	}
	
//	public void setGlobalComponentId(NicheId globalId) {
//		this.globalComponentId = globalId;
//	}

	public int getAllocatedStorage() {
		return currentAllocatedResource.getAllocatedStorage();
	}

	public String getSerializedDeployParameters() {
		return serializedDeployParameters;
	}
	public void setSerializedDeployParameters(Object param) {
		if(!(param instanceof String)) {
			throw new ClassCastException();
		}
		serializedDeployParameters = (String) param;
	}


	/* (non-Javadoc)
	 * @see dks.niche.interfaces.SNR#getResourceRef()
	 */
	public ResourceRef getResourceRef() {
		return currentAllocatedResource;
	}
	/* (non-Javadoc)
	 * @see dks.niche.ids.SNR#addServerBinding(java.lang.String, int)
	 */
	public void addServerBinding(String interfaceName, int type) {
		BindId cachedBindId = new BindId();
		cachedBindId.setReceiverSideInterfaceDescription(interfaceName);
		serverSideBindingsADLToBindId.put(interfaceName, cachedBindId);
		
	}
	/* (non-Javadoc)
	 * @see dks.niche.ids.SNR#getPredefinedReceiverBindings()
	 */
	public HashMap<String, BindId> getPredefinedReceiverBindings() {
		return serverSideBindingsADLToBindId;
	}

	public String toString() {
		return globalComponentId.toString();
	}
	
//	
//	@Override
//	public void activate(boolean replicate) {
//		
//		niche.log("ComponentId says: sending to destination: "+myId);
//		DelegationRequestMessage message = new DelegationRequestMessage(
//												myId,
//												ComponentId.class.getName(),
//												new Object[]{currentComponentLocation, realComponentId}
//											);
//		
//		niche.sendToManagement(myId, message, replicate);
//		//return new Future??;
//
//	}


}
