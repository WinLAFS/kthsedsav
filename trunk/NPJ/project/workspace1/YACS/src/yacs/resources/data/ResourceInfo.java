package yacs.resources.data;

import dks.niche.ids.*;

import java.io.Serializable;

import yacs.interfaces.YACSNames;

/**
 * Used by resource to report their state and specifications to the resource service.
 * @author LTDATH
 */
public class ResourceInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// TODO: use a sequence number instead, set by the class creator?
	private long creationTime;
	private long updateTime;
	private ComponentId componentId;
	private String componentType;
	private long status = YACSNames.AVAILABILITY_STATUS__BUSY; // pessimistic
	
	private double freeMemory; // Mbs
	private double cpuSpeed; // Mhz
	
	public ResourceInfo(ComponentId componentId, String componentType, long creationTime, long status) {
		super();
		this.creationTime = creationTime;
		this.updateTime = System.currentTimeMillis();
		this.componentId = componentId;
		this.componentType = componentType;
		this.status = status;
	}
	
	public boolean isNewerThan( ResourceInfo info ){
		if( !componentId.getId().toString().equals(info.getComponentId().getId().toString()) )
			return true;
		
		// if from a restored resource, i.e. crashed and restarted
		if( creationTime < info.getCreationTime() )
			return false;
		else if( creationTime > info.getCreationTime() )
			return true;
		
		// if from the same resource instance (i.e. same creation time) then note the update time 
		if( updateTime <= info.getUpdateTime() )
			return false;
		else
			return true;
	}
	
	public void updateLastUpdateTime(){
		this.updateTime = System.currentTimeMillis();
	}
	
	
	// getters and setters
	public long getStatus() {
		return status;
	}
	public void setStatus(long status) {
		this.status = status;
	}
	public ComponentId getComponentId() {
		return componentId;
	}
	public void setComponentId(ComponentId componentId) {
		this.componentId = componentId;
	}
	public String getComponentType() {
		return componentType;
	}
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

	public double getFreeMemory() {
		return freeMemory;
	}
	public void setFreeMemory(double freeMemory) {
		this.freeMemory = freeMemory;
	}
	public double getCpuSpeed() {
		return cpuSpeed;
	}
	public void setCpuSpeed(double cpuSpeed) {
		this.cpuSpeed = cpuSpeed;
	}

	public long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	public long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
}
