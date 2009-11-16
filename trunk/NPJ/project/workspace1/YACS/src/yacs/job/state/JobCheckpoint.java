package yacs.job.state;

import java.io.*;
import java.util.Hashtable;
import yacs.job.*;

public class JobCheckpoint extends CheckpointInformation {
	
	private static final long serialVersionUID = 1L;
	
	private Job job;
	private Serializable[] params;
	private Hashtable<String,String> seenWorkers;
	
	public JobCheckpoint(long id, Job job, Hashtable<String,String> seenWorkers){
		super(id);
		this.job = job;
		this.seenWorkers = seenWorkers;
	}

	
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	
	public Serializable[] getParams() {
		return params;
	}
	public void setParams(Serializable[] params) {
		this.params = params;
	}
	
	public Hashtable<String, String> getSeenWorkers() {
		return seenWorkers;
	}
	public void setSeenWorkers(Hashtable<String, String> seenWorkers) {
		this.seenWorkers = seenWorkers;
	}
}
