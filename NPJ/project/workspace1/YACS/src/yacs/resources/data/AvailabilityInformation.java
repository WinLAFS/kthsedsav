package yacs.resources.data;

import java.io.Serializable;

/**
 * Information about availability issues of the system.
 * @author LTDATH
 */
public class AvailabilityInformation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long id;
	
	private long freeResourceComponents=0;
	private long busyResourceComponents=0;
	
	private long freeMasterComponents=0;
	private long busyMasterComponents=0;
	
	private long freeWorkerComponents=0;
	private long busyWorkerComponents=0;
	
	// constructors
	public AvailabilityInformation(){}
	public AvailabilityInformation( long id ){ this.id = id; }

	// getters and setters
	public long getFreeResourceComponents() {
		return freeResourceComponents;
	}
	public void setFreeResourceComponents(long freeResourceComponents) {
		this.freeResourceComponents = freeResourceComponents;
	}
	public long getBusyResourceComponents() {
		return busyResourceComponents;
	}
	public void setBusyResourceComponents(long busyResourceComponents) {
		this.busyResourceComponents = busyResourceComponents;
	}
	public long getFreeMasterComponents() {
		return freeMasterComponents;
	}
	public void setFreeMasterComponents(long freeMasterComponents) {
		this.freeMasterComponents = freeMasterComponents;
	}
	public long getBusyMasterComponents() {
		return busyMasterComponents;
	}
	public void setBusyMasterComponents(long busyMasterComponents) {
		this.busyMasterComponents = busyMasterComponents;
	}
	public long getFreeWorkerComponents() {
		return freeWorkerComponents;
	}
	public void setFreeWorkerComponents(long freeWorkerComponents) {
		this.freeWorkerComponents = freeWorkerComponents;
	}
	public long getBusyWorkerComponents() {
		return busyWorkerComponents;
	}
	public void setBusyWorkerComponents(long busyWorkerComponents) {
		this.busyWorkerComponents = busyWorkerComponents;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
}