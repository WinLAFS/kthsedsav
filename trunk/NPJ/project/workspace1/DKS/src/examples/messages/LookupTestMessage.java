/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package examples.messages;

import dks.addr.DKSRef;
import dks.messages.Message;

/**
 * The <code>LookupTestMessage</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: LookupTestMessage.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class LookupTestMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -9206104444498298833L;
	
	String lookupId; //the loo
	String uniqueMessadeID;
	Object messageData;
	long sendTime=0;
	long receiveTime=0;
	long reSendTime=0;
	transient DKSRef dest;
	
	boolean ping=true; // the sender set it to true then receiver set it to false when re sending back

	
	
	
	
	
	/**
	 * @param lookupId
	 * @param uniqueMessadeID
	 * @param messageData
	 * @param sendTime
	 */
	public LookupTestMessage(String lookupId, String uniqueMessadeID, Object messageData, long sendTime) {
		super();
		this.lookupId = lookupId;
		this.uniqueMessadeID = uniqueMessadeID;
		this.messageData = messageData;
		this.sendTime = sendTime;
	}

	public boolean isPing() {
		return ping;
	}

	public void setPing(boolean ping) {
		this.ping = ping;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public long getReSendTime() {
		return reSendTime;
	}

	public void setReSendTime(long reSendTime) {
		this.reSendTime = reSendTime;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public Object getMessageData() {
		return messageData;
	}

	public void setMessageData(Object messageData) {
		this.messageData = messageData;
	}

	public String getUniqueMessadeID() {
		return uniqueMessadeID;
	}

	public void setUniqueMessadeID(String uniqueMessadeID) {
		this.uniqueMessadeID = uniqueMessadeID;
	}

	public String getLookupId() {
		return lookupId;
	}

	public void setLookupId(String lookupId) {
		this.lookupId = lookupId;
	}
	
	/**
	 * @return Returns the dest.
	 */
	public DKSRef getDest() {
		return dest;
	}
	
	/**
	 * @param dest The dest to set.
	 */
	public void setDest(DKSRef dest) {
		this.dest = dest;
	}
}


