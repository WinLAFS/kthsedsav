package yacs.job.state;

import java.io.*;
import yacs.job.*;

public class TaskCheckpoint extends CheckpointInformation {
	
	private static final long serialVersionUID = 1L;
	
	private TaskContainer task;
	
	public TaskCheckpoint(long id, TaskContainer task){
		super(id);
		this.task = task;
	}

	// getters and setters
	public TaskContainer getTask() {
		return task;
	}
	public void setTask(TaskContainer task) {
		this.task = task;
	}
	
	public String toString(){
		return (task != null ? 
				(task.getTid() + "@v:" + getVersion() + "@W:" + (task.getWorker()!=null?task.getWorker().getId():"NULL"))
				:
				("NULL@v:" + getVersion() + "@W:NULL"))
				+ " [@" + this.hashCode()+"]";
	}
}
