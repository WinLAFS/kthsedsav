package yass.storage;

import java.io.File;
import java.io.Serializable;

import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;

public class YassResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5635371377532850933L;

	private ComponentId replicaHost;
	private GroupId fileGroup;
	private File file;
	private String message;
	private boolean succeeded;
	private int numberOfHops = -1;
	
	public YassResult(String message) {
		this.message = message;
		this.succeeded = false;
	}
	public YassResult(GroupId fileGroup) {
		this.fileGroup = fileGroup;
		this.succeeded = true;
	}

	public YassResult(GroupId fileGroup, int numberOfHops) {
		this.fileGroup = fileGroup;
		this.numberOfHops = numberOfHops;
		this.succeeded = true;
	}
	public YassResult(File file) {
		this.file = file;
		this.succeeded = true;
	}
	public YassResult(ComponentId replicaHost) {
		this.replicaHost = replicaHost;
		this.succeeded = true;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	public GroupId getFileGroup() {
		return fileGroup;
	}
	public void setFileGroup(GroupId fileGroup) {
		this.fileGroup = fileGroup;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ComponentId getReplicaHost() {
		return replicaHost;
	}
	public void setReplicaHost(ComponentId replicaHost) {
		this.replicaHost = replicaHost;
	}
	public boolean isSucceeded() {
		return succeeded;
	}
	public void setSucceeded(boolean succeeded) {
		this.succeeded = succeeded;
	}
	public int getNumberOfHops() {
		return numberOfHops;
	}
	public void setNumberOfHops(int numberOfHops) {
		this.numberOfHops = numberOfHops;
	}
	
	
	
}
