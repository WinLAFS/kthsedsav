package yacs.job.tasks;

import yacs.interfaces.YACSNames;
import yacs.job.TaskContainer;

import java.io.Serializable;

public class CpuTask extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long workTime=0;
	
	public CpuTask( long workTime ){
		this.workTime = workTime;
	}
	
	@Override
	public void execute() {
		log("CpuTask.execute: starting with work time of "+workTime+" msek...");
		try {
			long end = System.currentTimeMillis() + workTime;
			
			long count = 0; 
			while( System.currentTimeMillis() <= end ){
				count++;
			}
			log("CpuTask.execute: endcount is not important, but still: " + count);
		}
		catch( Exception e ){
			e.printStackTrace();
			this.setResultCode( YACSNames.RESULT_ERROR );
		}
		finally {
			log("WorkTask.execute: done...");
			this.setStatus( YACSNames.TASK_COMPLETED );
		}
	}

	@Override
	public void initFromState(){}
}
