package yacs.job.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.ComponentId;

import yacs.job.state.CheckpointInformation;

public class StateChangeEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ComponentId source;
	private CheckpointInformation cp;
	
	public StateChangeEvent(ComponentId source, CheckpointInformation cp){
		this.source = source;
		this.cp = cp;
	}

	public CheckpointInformation getStateInformation() {
		return cp;
	}

	public ComponentId getSource() {
		return source;
	}
}
