package se.kth.ict.id2203.lpb.beans;

import java.io.Serializable;

import se.sics.kompics.address.Address;

public class GossipMessage implements Serializable {

	private static final long serialVersionUID = 2118892902527488043L;

	private Address sender;
	private Address originalMessageSender;
	private int messageNumber;
	private int ttl=0;
	private String messageData;
	private String messageType;
	
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	public String getMessageData() {
		return messageData;
	}
	public void setMessageData(String messageData) {
		this.messageData = messageData;
	}
	public Address getSender() {
		return sender;
	}
	public void setSender(Address sender) {
		this.sender = sender;
	}
	public Address getOriginalMessageSender() {
		return originalMessageSender;
	}
	public void setOriginalMessageSender(Address originalMessageSender) {
		this.originalMessageSender = originalMessageSender;
	}
	public int getMessageNumber() {
		return messageNumber;
	}
	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}
	public int getTtl() {
		return ttl;
	}
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
