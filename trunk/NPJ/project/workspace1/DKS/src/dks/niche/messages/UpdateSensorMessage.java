/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.messages;

import java.io.Serializable;
import java.util.ArrayList;

//import dks.comm.DirectByteBuffer;import dks.marshall.MarshallComponent;
import dks.messages.Message;
import dks.niche.ids.NicheId;

/**
 * The <code>UpdateSensorMessage</code> class
 *
 * @author Joel
 * @version $Id: UpdateManagementElementMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class UpdateSensorMessage extends Message implements Serializable {


	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 3191614285812502092L;
	
	
	public final static int TYPE_CHANGE_SOURCE = 0;
	public final static int TYPE_ADD_SINK = 2;
	public final static int TYPE_REMOVE_SINK = 3;
	
	NicheId destination;
	boolean bulkOperation;
	
	int type;
	
	Object thing;
	ArrayList things; //no typechecking for now. rebuild, if desired later...
	
	//Object reference; ArrayList references;	Object watcher; ArrayList watchers;
	
	public UpdateSensorMessage() {
		
	}

	public UpdateSensorMessage(NicheId id, int type, ArrayList things) {
		this.destination = id;
		this.bulkOperation = true;
		this.type = type;
		this.things = things;
		
	}
	
	public UpdateSensorMessage(NicheId id, int type, Object thing) {
		this.destination = id;
		this.bulkOperation = false;
		this.type = type; 
		this.thing = thing;
		
	}

	/**
	 * @return Returns the bulkOperation.
	 */
	public boolean isBulkOperation() {
		return bulkOperation;
	}

	/**
	 * @param bulkOperation The bulkOperation to set.
	 */
	public void setBulkOperation(boolean bulkOperation) {
		this.bulkOperation = bulkOperation;
	}

	/**
	 * @return Returns the thing.
	 */
	public Object getReference() {
		return thing;
	}

	/**
	 * @param thing The thing to set.
	 */
	public void setReference(Object thing) {
		this.thing = thing;
	}

	/**
	 * @return Returns the things.
	 */
	public ArrayList getReferences() {
		return things;
	}

	/**
	 * @param things The things to set.
	 */
	public void setReferences(ArrayList things) {
		this.things = things;
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
	public void setType(int type) {
		this.type = type;
	}

	
	/**
	 * @return Returns the destination.
	 */
	public NicheId getDestination() {
		return destination;
	}

	/**
	 * @param destination The destination to set.
	 */
	public void setDestination(NicheId destination) {
		this.destination = destination;
	}

}
