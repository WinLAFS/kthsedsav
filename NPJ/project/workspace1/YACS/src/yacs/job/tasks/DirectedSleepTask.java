package yacs.job.tasks;

import yacs.interfaces.YACSNames;
import yacs.job.TaskContainer;

import java.io.Serializable;

public class DirectedSleepTask extends DirectedTask implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private long sleepTime=0;
	
	public DirectedSleepTask(){}
	
	public DirectedSleepTask( long sleepTime ){
		this.sleepTime = sleepTime;
	}
	
	protected void initTaskInfo(){
		this.logName = "DirectedSleepTask";
	}
	
	@Override
	public void doTask() {
		log("DirectedSleepTask("+this.getTid()+").execute: starting with sleep of "+sleepTime+" msek...");
		try {
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
	public void initFromState(){
		super.initFromState();
		
		Serializable[] state = this.getState();
		if( state == null || state.length < 3 ){
			log( "Sleep value missing from init state" );
			return;
		}
		try {
			sleepTime = (Long)state[2];
		}
		catch( Exception e ){
			log( "SleepTime of unknown type: " + e.getMessage() );
			e.printStackTrace();
		}
	}
}
