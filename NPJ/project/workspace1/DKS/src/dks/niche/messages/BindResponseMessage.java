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

import dks.messages.Message;
import dks.niche.NicheMessageTable;

/**
 * The <code>BindResponseMessage</code> class
 *
 * @author Joel
 * @version $Id: BindResponseMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class BindResponseMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -2211769764130455668L;

	public final static int messageType = NicheMessageTable.MSG_TYPE_BIND_RESPONSE_MESSAGE + NicheMessageTable.INTERVAL_STARTING;
	
	int operationId;
	Object response;
	
	//Do remember empty constructor
	public BindResponseMessage() {
		
	}
	/**
	 * @param operationId
	 * @param message
	 */
	public BindResponseMessage(int operationId, Object response) {
		this.operationId = operationId;
		this.response = response;
	}

//	
//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler   ) { //,	List<DirectByteBuffer> buffers)  {
//		super.initMarshall(buffers, messageType, marshaler);
//		
//		marshaler.addInt(buffers, operationId);		
//		try {
//			marshaler.addObject(buffers, response);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		
//		operationId = marshaler.remInt(buffers);
//		try {
//			response = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	
//	/* (non-Javadoc)
//	 * @see dks.messages.Message#getMessageType()
//	 */
//	@Override
//	public int getMessageType() {
//		return messageType ;
//	}
//
//	public static int getStaticMessageType() {
//		return messageType ;
//	}

	/**
	 * @return Returns the message.
	 */
	public Object getResponse() {
		return response;
	}

	/**
	 * @param message The message to set.
	 */
	public void setResponse(Object response) {
		this.response = response;
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
