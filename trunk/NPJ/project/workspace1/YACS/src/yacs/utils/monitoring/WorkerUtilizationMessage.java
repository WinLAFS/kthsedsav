package yacs.utils.monitoring;

public class WorkerUtilizationMessage extends MonitoringMessage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long completedTasks=0;
	private long ongoingTasks=0;
	
	
	
	public long getCompletedTasks() {
		return completedTasks;
	}
	public void setCompletedTasks(long completedTasks) {
		this.completedTasks = completedTasks;
	}
	public long getOngoingTasks() {
		return ongoingTasks;
	}
	public void setOngoingTasks(long ongoingTasks) {
		this.ongoingTasks = ongoingTasks;
	}
	
	public String toString(){
		return "WUM:{"+ongoingTasks+","+completedTasks+"}" ;
	}
}
