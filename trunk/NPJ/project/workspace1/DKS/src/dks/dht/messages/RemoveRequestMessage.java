/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.messages;

import java.math.BigInteger;

import dks.dht.DHTComponent.removeFlavor;
import dks.dht.events.RemoveRequestEvent;
import dks.messages.Message;

/**
 * The <code>RemoveRequestMessage</code> class
 *
 * @author Joel
 * @version $Id: RemoveRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class RemoveRequestMessage extends Message {

/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 955389147367888142L;
//    public static final int messageType = MessageTypeTable.MSG_TYPE_REMOVE_REQUEST;

    int operationId;
    BigInteger id;

    Object key;
    removeFlavor flavor;
    int position; 		// used only witn REMOVE_AT


    /**
     * Empty constructor, needed by marshaler
     */

    public RemoveRequestMessage() {
//	flavor = removeFlavor.REMOVE_ALL;
//	position = -1;

    }

    public RemoveRequestMessage(BigInteger id, RemoveRequestEvent e) {

	this.id = id;
	this.operationId = e.getOperationId();

	this.key = e.getKey();
	this.flavor = e.getFlavor();
	this.position = e.getPosition();

	//System.out.println("Ahmad: id="+this.id+" oID="+this.operationId+" key=" + this.key+ " flavor="+this.flavor+" pos="+this.position);

    }


//    @Override
//    public List<DirectByteBuffer> marshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers)  {
//	marshaler.addInt(buffers, messageType);
//
//	marshaler.addBigInteger(buffers, id);
//	marshaler.addInt(buffers, operationId);
//	try {
//	    marshaler.addObject(buffers, key);
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//	marshaler.addInt(buffers, flavor.ordinal());
//	marshaler.addInt(buffers, position);
//
//	return buffers;
//    }
//
//    @Override
//    public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//
//	id = marshaler.remBigInteger(buffers);
//	operationId = marshaler.remInt(buffers);
//	
//	try {
//	    key = marshaler.remObject(buffers);
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	} catch (ClassNotFoundException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//	
//	
//	flavor = removeFlavor.values()[marshaler.remInt(buffers)];
//	position = marshaler.remInt(buffers);
//	
//    }
//
//    /* (non-Javadoc)
//     * @see dks.messages.Message#getMessageType()
//     */
//    @Override
//    public int getMessageType() {
//	return messageType;
//    }
//
//
//    public static int getStaticMessageType() {
//	return messageType;
//    }


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
     * @return Returns the flavor.
     */
    public removeFlavor getFlavor() {
	return flavor;
    }


    /**
     * @param flavor The flavor to set.
     */
    public void setFlavor(removeFlavor flavor) {
	this.flavor = flavor;
    }


    /**
     * @return Returns the id.
     */
    public BigInteger getId() {
	return id;
    }


    /**
     * @param id The id to set.
     */
    public void setId(BigInteger id) {
	this.id = id;
    }


    /**
     * @return Returns the key.
     */
    public Object getKey() {
	return key;
    }


    /**
     * @param key The key to set.
     */
    public void setKey(Object key) {
	this.key = key;
    }




    public int getPosition() {
	return position;
    }

    public void setPosition(int position) {
	this.position = position;
    }

//    //Joels lilla hemmasnickeri for att slippa en extra parameter
//    public boolean acknowledgementWanted() {
//	return !(NicheInterfaceComponent.NO_ACKNOWLEDGEMENT_WANTED == operationId);
//    }



}
