package yacs.resources.data;

import java.io.Serializable;

public class ResourceHolder implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long time;
	private String id;
	private ResourceInfo resource;
	
	public ResourceHolder(String id, ResourceInfo resource){
		this.time = System.currentTimeMillis();
		this.id=id;
		this.resource=resource;
	}
	
	public boolean equals(Object obj){
		if( obj == null || !(obj instanceof ResourceHolder) )
			return false;
		
		ResourceHolder comp = (ResourceHolder)obj;
		
		return this.id.equals(comp.id);
	}

	public long getTime(){ return time; }
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public ResourceInfo getResource() {
		return resource;
	}
	public void setResource(ResourceInfo resource) {
		this.resource = resource;
	}
}
