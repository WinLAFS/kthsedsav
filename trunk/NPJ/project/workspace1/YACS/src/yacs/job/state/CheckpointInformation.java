package yacs.job.state;

import java.io.Serializable;

import dks.niche.ids.ComponentId;

public abstract class CheckpointInformation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long version;
	private ComponentId source;
	
	public CheckpointInformation(long version){
		this.version = version;
	}
	
	public long getVersion(){ return version; }

	public ComponentId getSource() {
		return source;
	}
	public void setSource(ComponentId source) {
		this.source = source;
	}
}
