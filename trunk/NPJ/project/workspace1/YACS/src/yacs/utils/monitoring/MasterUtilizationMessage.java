package yacs.utils.monitoring;

public class MasterUtilizationMessage extends MonitoringMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long completedJobs=0;
	private long ongoingJobs=0;
	
	
	public long getCompletedJobs() {
		return completedJobs;
	}
	public void setCompletedJobs(long completedJobs) {
		this.completedJobs = completedJobs;
	}
	public long getOngoingJobs() {
		return ongoingJobs;
	}
	public void setOngoingJobs(long ongoingJobs) {
		this.ongoingJobs = ongoingJobs;
	}
	
	public String toString(){
		return "MUM:{"+ongoingJobs+","+completedJobs+"}" ;
	}
}
