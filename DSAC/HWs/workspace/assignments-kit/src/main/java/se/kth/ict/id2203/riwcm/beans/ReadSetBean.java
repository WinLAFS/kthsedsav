package se.kth.ict.id2203.riwcm.beans;

public class ReadSetBean {
	private int timestamp;
	private int processRank;
	private String value;
	public int getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	public int getProcessRank() {
		return processRank;
	}
	public void setProcessRank(int processRank) {
		this.processRank = processRank;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public ReadSetBean(int timestamp, int processRank, String value) {
		super();
		this.timestamp = timestamp;
		this.processRank = processRank;
		this.value = value;
	}
	
	
}
