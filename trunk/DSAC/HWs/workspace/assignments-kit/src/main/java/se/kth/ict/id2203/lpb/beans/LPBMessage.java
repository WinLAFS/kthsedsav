package se.kth.ict.id2203.lpb.beans;

import java.io.Serializable;

import se.sics.kompics.address.Address;
import sun.security.provider.MD5;

/**
 * @author Shum
 *
 */
public class LPBMessage implements Serializable {

	private static final long serialVersionUID = -132651751020165015L;
	
	private String msgType;
	private Address sender;
	private String message;
	private int messageNumber;
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public Address getSender() {
		return sender;
	}
	public void setSender(Address sender) {
		this.sender = sender;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getMessageNumber() {
		return messageNumber;
	}
	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public int hashCode() {
		return (this.message+this.messageNumber+this.msgType).hashCode();
	}

}
