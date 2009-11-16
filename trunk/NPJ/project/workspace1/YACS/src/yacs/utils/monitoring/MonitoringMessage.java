package yacs.utils.monitoring;

import java.io.Serializable;

public class MonitoringMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int id;
	private long creationTime = System.currentTimeMillis();
	
	private String sender;
	private String sendername;
	private String sendertype;
	private String receiver;
	
	private String freetext;
	
	public MonitoringMessage(){
		this(-1);
	}
	public MonitoringMessage( int id ){
		this.id = id;
	}
	
	// getters and setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public long getCreationTime() {
		return creationTime;
	}
	
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getSendername() {
		return sendername;
	}
	public void setSendername(String sendername) {
		this.sendername = sendername;
	}
	public String getSendertype() {
		return sendertype;
	}
	public void setSendertype(String sendertype) {
		this.sendertype = sendertype;
	}
	
	public String getReceiver() {
		return receiver;
	}
	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}
	
	public String getFreetext() {
		return freetext;
	}
	public void setFreetext(String freetext) {
		this.freetext = freetext;
	}
}
