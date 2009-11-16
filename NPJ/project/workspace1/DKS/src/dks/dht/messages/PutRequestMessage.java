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

import dks.dht.DHTComponent.putFlavor;
import dks.dht.events.PutRequestEvent;
import dks.messages.Message;
import dks.messages.MessageTypeTable;

/**
 * The <code>PutRequestMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: PutRequestMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class PutRequestMessage extends Message { //LookupOperationRequestMessage {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5760056409851497714L;

	public static final int messageType = MessageTypeTable.MSG_TYPE_PUT_REQUEST;
	
	int operationId;
	BigInteger id;
	Object key;
	Object value;
	putFlavor flavor;
	boolean multiVal;

	/**
	 * 
	 */
//	public LookupOperationRequestMessage(long lookupId,
//			BigInteger destinationId, LookupStrategy strategy,
//			boolean reliable, DKSRef initiator, Message operationMsg) {
//		
//	}
	public PutRequestMessage() {
		
	}

	public PutRequestMessage(BigInteger id, PutRequestEvent e) {
		
		this.id = id;
		operationId = e.getOperationId();
		
		key = e.getKey();
		value = e.getValue();
		flavor = e.getFlavor();
		multiVal = e.isMultiVal();
		
	}


//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers)  {
//		marshaler.addInt(buffers, messageType);
//		
//		marshaler.addBigInteger(buffers, id);
//		marshaler.addInt(buffers, operationId);
//		try {
//			marshaler.addObject(buffers, key);
//			marshaler.addObject(buffers, value);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//marshaler.addInt(buffers, flavor.);
//		marshaler.addInt(buffers, flavor.ordinal());
//		marshaler.addBool(buffers, multiVal);
//			
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		id = marshaler.remBigInteger(buffers);
//		operationId = marshaler.remInt(buffers);
//		
//		try {
//			key = marshaler.remObject(buffers);
//			value = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		flavor = putFlavor.values()[marshaler.remInt(buffers)];
//		
//		multiVal = marshaler.remBool(buffers);
//		
//	}
//
//	/* (non-Javadoc)
//	 * @see dks.messages.Message#getMessageType()
//	 */
//	@Override
//	public int getMessageType() {
//		return messageType;
//	}
//	
//	
//	public static int getStaticMessageType() {
//		return messageType;
//	}

	
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
	public putFlavor getFlavor() {
		return flavor;
	}


	/**
	 * @param flavor The flavor to set.
	 */
	public void setFlavor(putFlavor flavor) {
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


	/**
	 * @return Returns the multiVal.
	 */
	public boolean isMultiVal() { //is, has, get...
		return multiVal;
	}


	/**
	 * @param multiVal The multiVal to set.
	 */
	public void setMultiVal(boolean multiVal) {
		this.multiVal = multiVal;
	}


	/**
	 * @return Returns the value that is about to be stored.
	 */
	public Object getValue() {
		return value;
	}


	/**
	 * @param value The value to set.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

//	//Joels lilla hemmasnickeri for att slippa en extra parameter
//	public boolean acknowledgementWanted() {
//		return !(NicheInterfaceComponent.NO_ACKNOWLEDGEMENT_WANTED == operationId);
//	}

	

}
