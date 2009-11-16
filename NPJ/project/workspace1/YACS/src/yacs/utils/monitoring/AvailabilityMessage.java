package yacs.utils.monitoring;

public class AvailabilityMessage extends MonitoringMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long minFreeResourceComponents=0;
	private long freeResourceComponents=0;
	private long busyResourceComponents=0;
	
	private long minFreeMasterComponents=0;
	private long freeMasterComponents=0;
	private long busyMasterComponents=0;
	
	private long minFreeWorkerComponents=0;
	private long freeWorkerComponents=0;
	private long busyWorkerComponents=0;
	
	// constructors
	public AvailabilityMessage(){}

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

	
	
	public long getMinFreeResourceComponents() {
		return minFreeResourceComponents;
	}
	public void setMinFreeResourceComponents(long minFreeResourceComponents) {
		this.minFreeResourceComponents = minFreeResourceComponents;
	}
	public long getMinFreeMasterComponents() {
		return minFreeMasterComponents;
	}
	public void setMinFreeMasterComponents(long minFreeMasterComponents) {
		this.minFreeMasterComponents = minFreeMasterComponents;
	}
	public long getMinFreeWorkerComponents() {
		return minFreeWorkerComponents;
	}
	public void setMinFreeWorkerComponents(long minFreeWorkerComponents) {
		this.minFreeWorkerComponents = minFreeWorkerComponents;
	}
}
