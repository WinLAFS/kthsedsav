package yacs.job.events;

import java.io.Serializable;

import dks.arch.Event;

import yacs.job.events.MasterManagementEvent.TYPE;
import yacs.job.state.CheckpointInformation;

public class WorkerManagementEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TYPE type;
	
	public WorkerManagementEvent(){}
	public WorkerManagementEvent(TYPE type) {
		super();
		this.type = type;
	}

	// BEGIN: getters and setters
	public TYPE getType() 			{ return type; }
	public void setType(TYPE type) 	{ this.type = type; }
	// END: getters and setters

	public enum TYPE {
		TASK_STARTED,
		TASK_COMPLETED,
		TASK_DELETED,
		TASK_FAILED
	}
	
	public String toString(){
		return "WME:{"+(type==null?"null":type.toString())+"}";
	}
}
