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

import dks.messages.Message;

/**
 * The <code>RemoveAckMessage</code> class
 *
 * @author Joel
 * @version $Id: RemoveAckMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class RemoveAckMessage extends Message {

//	public static final int messageType = MessageTypeTable.MSG_TYPE_REMOVE_ACK;

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 4694461737479581253L;
	Object result;
	int operationId;
	
	public RemoveAckMessage() {
		
	}

	public RemoveAckMessage(int operationId, Object result) {
		
		this.operationId = operationId;
		this.result = result;
		
	}
	
	public RemoveAckMessage(RemoveRequestMessage m, Object result) {
		
		this.operationId = m.getOperationId();
		this.result = result;
	}

//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler,	List<DirectByteBuffer> buffers) {
//		marshaler.addInt(buffers, messageType);
//		try {
//		    marshaler.addObject(buffers, result);
//		} catch (IOException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
//		marshaler.addInt(buffers, operationId);
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		try {
//		    result = marshaler.remObject(buffers);
//		} catch (IOException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//		    // TODO Auto-generated catch block
//		    e.printStackTrace();
//		}
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

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
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
