package yacs.job.helpers;

import java.io.Serializable;

import dks.niche.ids.*;

public class DiscoveryReply implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ComponentId resource;
	private boolean available = false;
	
	public DiscoveryReply(ComponentId resource,boolean available) {
		super();
		this.available = available;
		this.resource = resource;
	}

	
	public ComponentId getResource() {
		return resource;
	}
	public void setResource(ComponentId resource) {
		this.resource = resource;
	}

	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
}
