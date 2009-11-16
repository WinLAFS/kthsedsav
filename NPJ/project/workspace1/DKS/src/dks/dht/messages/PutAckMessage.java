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

import static dks.messages.MessageTypeTable.MSG_TYPE_PUT_ACK;
import dks.messages.Message;

/**
 * The <code>PutAckMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: PutAckMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class PutAckMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -3355898911959015333L;

	public static final int messageType = MSG_TYPE_PUT_ACK;

	boolean result;
	int operationId;
	
	public PutAckMessage() {
		
	}

	public PutAckMessage(int operationId, boolean result) {
		
		this.operationId = operationId;
		this.result = result;
		
	}
	
	public PutAckMessage(PutRequestMessage m, boolean result) {
		
		this.operationId = m.getOperationId();
		this.result = result;
	}

//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler,	List<DirectByteBuffer> buffers) {
//		marshaler.addInt(buffers, messageType);
//		marshaler.addBool(buffers, result);
//		marshaler.addInt(buffers, operationId);
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		result = marshaler.remBool(buffers);
//		operationId = marshaler.remInt(buffers);
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

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
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



}
