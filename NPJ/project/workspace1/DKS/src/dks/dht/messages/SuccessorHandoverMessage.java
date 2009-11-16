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

import static dks.messages.MessageTypeTable.MSG_TYPE_DHT_SUCC_HANDOVER;
import dks.addr.DKSRef;
import dks.messages.Message;

/**
 * The <code>SuccessorHandoverMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: SuccessorHandoverMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class SuccessorHandoverMessage extends Message implements HandoverMessage {
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -6116007721805921850L;

	public static final int messageType = MSG_TYPE_DHT_SUCC_HANDOVER;
	
	//boolean stopIndirection;
	Object buffer;
	int chunkID;
	int totalChunks;
	
	
	//not serialized
	DKSRef from;
	DKSRef to;
	
	public SuccessorHandoverMessage() {
		super();
	}
	
	/**
	 * @param stopIndirection
	 * @param buffer
	 */
	public SuccessorHandoverMessage(Object buffer, int id , int total) {
		super();
		//this.stopIndirection = stopIndirection;
		this.buffer = buffer;
		chunkID = id;
		totalChunks = total;
	}

	
	
	/**
	 * @param buffer
	 * @param chunkID
	 * @param totalChunks
	 * @param from
	 * @param to
	 */
	public SuccessorHandoverMessage(Object buffer, int chunkID, int totalChunks, DKSRef from, DKSRef to) {
		super();
		this.buffer = buffer;
		this.chunkID = chunkID;
		this.totalChunks = totalChunks;
		this.from = from;
		this.to = to;
	}

//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler   ) { //,	List<DirectByteBuffer> buffers)  {
//		super.initMarshall(buffers, messageType, marshaler);
//		
//		try {
//			marshaler.addObject(buffers, buffer);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		marshaler.addInt(buffers, chunkID);
//		marshaler.addInt(buffers, totalChunks);
//		//marshaler.addBool(buffers, stopIndirection);
//			
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		try {
//			buffer = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		chunkID = marshaler.remInt(buffers);
//		totalChunks = marshaler.remInt(buffers);
//		//stopIndirection = marshaler.remBool(buffers);
//	}
	
	
	
	public Object getBuffer() {
		return buffer;
	}

	public void setBuffer(Object buffer) {
		this.buffer = buffer;
	}

	public int getChunkID() {
		return chunkID;
	}

	public void setChunkID(int chunkID) {
		this.chunkID = chunkID;
	}

	public int getTotalChunks() {
		return totalChunks;
	}

	public void setTotalChunks(int totalChunks) {
		this.totalChunks = totalChunks;
	}
	
	
	
//	public boolean isStopIndirection() {
//		return stopIndirection;
//	}
//
//	public void setStopIndirection(boolean stopIndirection) {
//		this.stopIndirection = stopIndirection;
//	}

//	for local use! not serialized
	public DKSRef getFrom() {
		return from;
	}

//	for local use! not serialized
	public void setFrom(DKSRef from) {
		this.from = from;
	}

//	for local use! not serialized
	public DKSRef getTo() {
		return to;
	}

//	for local use! not serialized
	public void setTo(DKSRef to) {
		this.to = to;
	}

//	/* (non-Javadoc)
//	 * @see dks.messages.Message#getMessageType()
//	 */
//	@Override
//	public int getMessageType() {
//		return messageType;
//	}
//	
//	public static int getStaticMessageType() {
//		return messageType;
//	}

}
