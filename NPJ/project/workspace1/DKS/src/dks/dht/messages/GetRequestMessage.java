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

import dks.dht.DHTComponent.getFlavor;
import dks.dht.events.GetRequestEvent;
import dks.messages.Message;
import dks.messages.MessageTypeTable;

/**
 * The <code>GetRequestMessage</code> class
 *
 * @author Joel
 * @author Ahamad
 * @version $Id: GetRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class GetRequestMessage extends Message {

/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 2976403466967675163L;

public static final int messageType = MessageTypeTable.MSG_TYPE_GET_REQUEST;
	
	BigInteger id;
	int operationId;
		
	Object key;
	getFlavor flavor;
	int position;
	
   public GetRequestMessage() {
		
	}
	/**
	 * 
	 */
	public GetRequestMessage(BigInteger id, GetRequestEvent event) {
		
		this.id = id;
		this.operationId = event.getOperationId();
		
		this.key = event.getKey();
		this.flavor = event.getFlavor();
		this.position = event.getPosition();
		
	}
//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler,	List<DirectByteBuffer> buffers)  {
//		marshaler.addInt(buffers, messageType);
//		
//		marshaler.addBigInteger(buffers, id);
//		marshaler.addInt(buffers, operationId);
//		try {
//			marshaler.addObject(buffers, key);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		marshaler.addInt(buffers, flavor.ordinal());
//		marshaler.addInt(buffers, position);
//			
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		id = marshaler.remBigInteger(buffers);
//		operationId = marshaler.remInt(buffers);
//		try {
//			key = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		flavor = getFlavor.values()[marshaler.remInt(buffers)];
//		position = marshaler.remInt(buffers);
//		
//	}
//	/**
//	 * @return Returns the messageType.
//	 */
//	@Override
//	public int getMessageType() {
//		return messageType;
//	}
//	
//	public static int getStaticMessageType() {
//		return messageType;
//	}
	
	/**
	 * @return Returns the flavor.
	 */
	public getFlavor getFlavor() {
		return flavor;
	}
	/**
	 * @param flavor The flavor to set.
	 */
	public void setFlavor(getFlavor flavor) {
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
	
	public int getPosition() {
	    return position;
	}
	
	public void setPosition(int position) {
	    this.position = position;
	}

	

	
	
	


}
