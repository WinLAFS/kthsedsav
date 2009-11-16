package yacs.job.tasks;

import yacs.interfaces.YACSNames;
import yacs.job.TaskContainer;

import java.io.Serializable;

public class SleepTask extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long sleepTime=0;
	
	public SleepTask(){}
	
	public SleepTask( long sleepTime ){
		this.sleepTime = sleepTime;
	}
	
	protected void initTaskInfo(){
		this.logName = "SleepTask";
	}
	
	@Override
	public void execute() {
		log("SleepTask("+this.getTid()+").execute: starting with sleep of "+sleepTime+" msek...");
		try {
			
			if( this.getState() != null && this.getState().length >= 0 && (this.getState()[0] instanceof Integer) ){
				this.sleepTime = (Integer)this.getState()[0];
				log("\tRead sleeptime from initParams: " + this.sleepTime);
			}
			
			Thread.currentThread().sleep(sleepTime);
			
			this.setResultCode( YACSNames.RESULT_OK );
		}
		catch( Exception e ){
			e.printStackTrace();
			this.setResultCode( YACSNames.RESULT_ERROR );
		}
		finally {
			log("SleepTask.execute: done...");
			this.setStatus( YACSNames.TASK_COMPLETED );
		}
	}

	@Override
	public void initFromState(){}
}
