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
 * The <code>GetResponseMessage</code> class
 *
 * @author Joel
 * @version $Id: GetResponseMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class GetResponseMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 7261048092583203239L;
	//public static final int messageType = MessageTypeTable.MSG_TYPE_GET_RESPONSE;
	Object result;
	int operationId;
	
	
	public GetResponseMessage() {
		 
	}
	
	public GetResponseMessage(int operationId, Object result) {
		this.operationId = operationId;
		this.result = result;
		 
	}
	
	public GetResponseMessage(GetRequestMessage grm, Object result) {
		this.operationId = grm.getOperationId();
		this.result = result;
		 
	}
//	/* (non-Javadoc)
//	 * @see dks.messages.Message#getMessageType()
//	 */
//	@Override
//	public int getMessageType() {
//		// TODO Auto-generated method stub
//		return messageType;
//	}
//	
//	public static int getStaticMessageType() {
//		return messageType;
//	}
//
//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler,	List<DirectByteBuffer> buffers)  {
//		marshaler.addInt(buffers, messageType);
//		marshaler.addInt(buffers, operationId);
//		try {
//			marshaler.addObject(buffers, result);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		operationId = marshaler.remInt(buffers);
//		try {
//			result = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
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
	 * @return Returns the result.
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * @param result The result to set.
	 */
	public void setResult(Object result) {
		this.result = result;
	}
}
