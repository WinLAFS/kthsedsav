package yacs.job.interfaces;

import dks.niche.ids.GroupId;
import yacs.job.TaskContainer;
import yacs.job.state.TaskCheckpoint;

public interface TaskManagementInterface {
	
	//public void performTask(Task task);
	public boolean performTask( TaskContainer task,  GroupId masterGroup, boolean dummy );
	public boolean performTask( TaskCheckpoint task, GroupId masterGroup, boolean dummy, boolean dummy2 );
	public TaskContainer taskStatus();
	
	// TODO: use attributes?
	public boolean setWorkerGroup( GroupId workerGroup, boolean dummy );
	public boolean setMasterGroup( GroupId masterGroup, boolean dummy );

	public boolean deleteTask( TaskContainer task );
}
