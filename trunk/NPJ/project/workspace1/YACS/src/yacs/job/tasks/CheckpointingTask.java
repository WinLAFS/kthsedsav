package yacs.job.tasks;

import yacs.interfaces.YACSNames;
import yacs.job.TaskContainer;

import java.io.Serializable;

public class CheckpointingTask extends Task implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean checkpointingDone = false;
	
	@Override
	public void execute() {
		log("CheckpointingTask("+this.getTid()+").execute: starting...");
		try {
			if( !checkpointingDone ){
				log("\tCheckpointing NOT done, sleeping for 5 sec...");
				Thread.sleep(5000);
				
				getExecutionContext().metacheckpoint();
				
				log("\tCheckpointing done! Sleeping until forcefully killed...");
				while( true ){
					Thread.sleep(1000);
				}
			}
			else {
				log("\tCheckpointing already done, sleeping for 10 sec...");
				Thread.sleep(10000);
				log("\tCheckpointing task done!");
			}
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
		log("CheckpointingTask.initialize:");
		Serializable[] state = this.getState();
		log("\tState: " + state);
		
		if( state != null && state.length >= 1 && (state[0] instanceof Boolean) )
			checkpointingDone = (Boolean)state[0];
		
		log("\tCheckpointing: " + checkpointingDone);
	}
	
	@Override
	public void prepareMetacheckpoint(){
		log("CheckpointingTask.prepareCheckpoint: " + checkpointingDone);
		this.setState( 
				new Serializable[]{ new Boolean(checkpointingDone) } 
			);
	}
}
