package yacs.job.events;

import java.io.Serializable;

import dks.arch.Event;

import yacs.job.state.CheckpointInformation;

public class ManagementEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Object globalId;
	
	public ManagementEvent(){
	}

}
