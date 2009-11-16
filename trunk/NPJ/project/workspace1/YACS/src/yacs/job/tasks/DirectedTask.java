package yacs.job.tasks;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.Serializable;

import yacs.interfaces.YACSNames;
import yacs.utils.YacsUtils;

public class DirectedTask extends Task {

	protected String DO_TASK = ":T";
	protected String FAIL = ":F";
	
	protected String CHECKPOINT = ":C";
	protected String SKIP_TO_SUCCESS = ":S"; // don't perform, just pretend it succeeded
	
	// TODO: restore from last checkpoint command... and YACS/Worker/ExecutionContext function to do that
	
	protected String commandFile = null;
	protected String checkpointName = null;
	

	// Task related
	@Override
	public void execute() {
		try {
			log("DirectedTask.execute: task.#" + this.getTid());
			if( checkpointName != null && checkpointName.length() != 0 )
				log("\tStarting from checkpoint: " + checkpointName);
			log("\tWaiting for commands...");
			
			String read = null;
			do{
				read = readCommand( commandFile );				
				
				if( isCommand(read,DO_TASK) ){
					this.doTask();
					// responsibility of task logic to set result: this.setResultCode( YACSNames.RESULT_OK );
				}
				else if( isCommand(read,FAIL) ){
					log("\t\tFail!");
					this.setResultCode( YACSNames.RESULT_ERROR );	
				}
				else if( isCommand(read,CHECKPOINT) ){
					log("\t\tCheckpointing: " + checkpointName);
					this.getExecutionContext().metacheckpoint();
				}
				else if( isCommand(read,SKIP_TO_SUCCESS) ){
					log("\t\tSkipping task. Assuming success!");
					this.setResultCode( YACSNames.RESULT_OK );
				}
				else if( read != null ){
					log("\tUnknown command: " + read);
				}
				else
					Thread.sleep(5000);
			}
			while( read == null || isCommand(read,CHECKPOINT) );
			/*while( read == null || (	
										!read.equals(DO_TASK) &&
										!read.equals(FAIL) &&
										!read.equals(SKIP_TO_SUCCESS)
					) );*/
		}
		catch( Exception e ){
			log( e.getMessage() );
			this.setResultCode( YACSNames.RESULT_ERROR );
		}
	}
	@Override
	public void initFromState() {
		
		Serializable[] state = this.getState();

		// assuming that command-file full-path is at i=0
		if( state == null || state.length == 0 || state[0] == null || !(state[0] instanceof String) ){
			log( "Could not read command file from init state!" );
			return;
		}
		commandFile = YacsUtils.nfsRelativeToNfsAbsolute((String)state[0]);
		log( "Read commandfile: " + commandFile  );
		
		// assuming that checkpoint-name is at i=1 
		if( state.length == 1 || state[1] == null || !(state[1] instanceof String) ){
			log( "No checkpoint-name in init-state!" );
			return;
		}
		checkpointName = (String)state[1];
		log( "Read checkpoint-name: " + checkpointName  );

	}
	@Override
	protected void initTaskInfo(){
		this.logName = "DirectedTask";
	}
	@Override
	public void prepareMetacheckpoint(){
		log("DirectedTask.prepareCheckpoint");
		this.setState( 
				new Serializable[]{ YacsUtils.nfsAbsoluteToNfsRelative(commandFile), 
									checkpointName } 
			);
	}
	
	// Directed task stuff
	protected void doTask(){
		log( "Doing the task..." );
		this.setResultCode( YACSNames.RESULT_OK );
	}
	
	// internal directed task logic
	private String lastCommand;
	private String readCommand( String commandFile ) throws Exception {
	
		/**
		 * LINE=[TID]:[COMMAND]
		 * TID=[0-9]+
		 * COMMAND=[TRANSCODE|CHECKPOINT|FAIL]
		 * TRANSCODE=T
		 * FAIL=F
		 * CHECKPOINT=V[A-z0-9]+
		 * 
		 * // [] reference to DEF
		 * // + => at least one instance of left-adjacent [DEF]
		 */
		
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader( new FileReader(commandFile) );
			
			String line; int i=-1;
			while( (line=reader.readLine()) != null ){
				
				// if not a command for this task
				if( !line.startsWith(this.getTid()+":") )
					continue;
				
				// if same as last command... will not be remembered between checkpoints
				if( lastCommand != null && lastCommand.equals(line) )
					return null;
				
				lastCommand = line;
				
				// if transcode command
				if( (i=line.indexOf(FAIL)) != -1 )
					return FAIL;
				else if( (i=line.indexOf(SKIP_TO_SUCCESS)) != -1 )
					return SKIP_TO_SUCCESS;
				else if( (i=line.indexOf(DO_TASK)) != -1 )
					return DO_TASK;
				else if( (i=line.indexOf(CHECKPOINT)) != -1 ){
					String valueToCheckpoint = line.substring(i+2);
					
					// if change from last checkpoint then perform it, else skip it 
					if( !valueToCheckpoint.equals(checkpointName) ){
						checkpointName = valueToCheckpoint;
						return CHECKPOINT;
					}
					else
						return null;
				}								
			}
		
			log("No command found for task. Assuming DO_TASK!");
			return DO_TASK;
			//return null;
		}
		catch( Exception e ){
			log( "Unable to read command: " + e.getMessage() );
			// so that the task wont block forever: let it do what it was meant to do
			e.printStackTrace();
			return DO_TASK;
		}
		finally {
			if( reader != null )
				reader.close();
		}
	}
	
	// helpers
	private boolean isCommand( String read, String command ){
		if( read == null || !read.equals(command) )
			return false;
		else
			return true;
	}
	

	

}
