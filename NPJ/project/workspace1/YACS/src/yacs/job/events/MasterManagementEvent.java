package yacs.job.events;

import java.io.Serializable;

import dks.arch.Event;

import yacs.job.events.WorkerManagementEvent.TYPE;
import yacs.job.state.CheckpointInformation;

public class MasterManagementEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TYPE type;
	
	public MasterManagementEvent(){}
	public MasterManagementEvent(TYPE type) {
		super();
		this.type = type;
	}

	// BEGIN: getters and setters
	public TYPE getType() 			{ return type; }
	public void setType(TYPE type) 	{ this.type = type; }
	// END: getters and setters


	public enum TYPE {
		JOB_STARTED,
		JOB_COMPLETED,
		JOB_DELETED,
		JOB_FAILED
	}
	
	public String toString(){
		return "MME:{"+(type==null?"null":type.toString())+"}";
	}
}
