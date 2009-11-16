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

import dks.addr.DKSRef;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ReliableInterface;
import dks.niche.wrappers.NodeRef;

/**
 * The <code>NicheId</code> class
 *
 * @author Joel
 * @version $Id: NicheId.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheId implements Serializable, IdentifierInterface, ReliableInterface {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5527673847891544722L;
	
	final static String separator = ":";
	private String location;
	//private String privateId;
	private String creator;
	private String owner;
	private String counter;
	boolean reliable;
	DKSRef dksRef;
	int type;
	int replicaNumber;
	
	public final static int TYPE_EXECUTOR = -3;
	
	public final static int TYPE_ACTUATOR = -2;
	
	public final static int TYPE_SENSOR = -1;
	
	public final static int TYPE_UNDEFINED = 0;

	public final static int TYPE_WATCHER = 1;

	public final static int TYPE_AGGREGATOR = 2;

	public final static int TYPE_MANAGER = 3;

	public final static int TYPE_BINDING = 4;

	public final static int TYPE_COMPONENT_ID = 5;
	
	public final static int TYPE_GROUP_ID = 6;
	
	public final static int TYPE_SUBSCRIPTION = 7;

	public NicheId() {
		location = creator = counter = null;
		
	}
	
	public NicheId(NicheId parent) {
		this.counter = parent.counter;
		this.creator = parent.creator;
		this.location = parent.location;
		this.owner = parent.owner;
		//this.privateId = parent.privateId;
		this.reliable = parent.reliable;
		this.type = parent.type;
		
	}
	
	public NicheId(String location, String owner, String creator, String counter, int type, boolean reliable) {
		this.location = location;
		this.owner = owner;
		this.creator = creator;
		this.counter = counter;
		this.type = type;
		this.reliable = reliable;
	}
	
	
	
//	public NicheId(String location, String privateId) {
//		this.location = location;		
//		if(privateId == null || privateId.length() == 0) {
//			this.privateId = this.creator = this.counter = null; 
//		} else {
//			String[]temp = privateId.split(separator);
//			this.creator = temp[0];
//			this.counter = temp[1];
//			this.privateId = creator + separator + counter;
//		}
//	}
//	
//	public NicheId(NicheId locationId, String privateId) {
//		this.location = locationId.getLocation();
//		String[]temp = privateId.split(separator);
//		this.creator = temp[0];
//		this.counter = temp[1];
//		this.privateId = creator + separator + counter;
//	}
//	
//	public NicheId(String id) {
//		String[]temp = id.split(separator);
//		location = temp[0];
//		creator = temp[1];
//		counter = temp[2];
//		privateId = creator + separator + counter;
//	}
	
	public boolean equals(Object compareObject) {
		if(compareObject instanceof NicheId) {
			NicheId compareId = (NicheId)compareObject;
			if(compareId.toString().equals(this.toString())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isCollocated(NicheId compare) {
		return location.equals(compare.getLocation());
	}
	
	public String getLocation() {
		return location;
	}

	public NicheId setLocation(String location) {
		this.location = location;
		return this;
	}
	
//	public NicheId set(String location, String privateId) {
//		this.location = location;
//		String[]temp = privateId.split(separator);
//		this.creator = temp[0];
//		this.counter = temp[1];
//		//this.privateId = creator + separator + counter;
//
//		return this;
//	}
	
//	public String getPrivateId() {
//		return privateId;
//	}

//	public void setPrivateId(String privateId) {
//		this.privateId = privateId;
//	}

	public String toString() {
		return location+separator+owner+separator+creator+separator+counter;
	}
	
	
	public NicheId getId() {
		return this;
	}
	public DKSRef getDKSRef() {
		return dksRef;
	}
	public NodeRef getNodeRef() {
		return new NodeRef(dksRef);
	}
	public NicheId setDKSRef(DKSRef dksRef) {
		this.dksRef = dksRef;
		return this;
	}
	
	public NicheId getDKSRefCopy(DKSRef dksRef) {
		return new NicheId(this).setDKSRef(dksRef);
	}
	public boolean isReliable() {
		return reliable;
	}
	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type The type to set.
	 */
	public NicheId setType(int type) {
		this.type = type;
		return this;
	}
	/**
	 * @param reliable The reliable to set.
	 */
	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}

	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * @return Returns the replicaNumber.
	 */
	public int getReplicaNumber() {
		return replicaNumber;
	}

	/**
	 * @param replicaNumber The replicaNumber to set.
	 */
	public void setReplicaNumber(int replicaNumber) {
		this.replicaNumber = replicaNumber;
	}
}
