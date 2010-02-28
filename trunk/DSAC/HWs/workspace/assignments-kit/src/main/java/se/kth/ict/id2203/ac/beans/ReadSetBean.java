package se.kth.ict.id2203.ac.beans;

public class ReadSetBean {
	
	private int timestamp;
	private String value;
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public ReadSetBean(int timestamp, String value) {
		super();
		this.timestamp = timestamp;
		this.value = value;
	}
	
	
}
