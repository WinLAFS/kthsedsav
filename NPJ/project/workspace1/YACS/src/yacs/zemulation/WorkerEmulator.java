package yacs.zemulation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import dks.niche.ids.ComponentId;

import yacs.interfaces.YACSNames;
import yacs.job.interfaces.TaskExecutionContext;
import yacs.job.state.TaskCheckpoint;
import yacs.utils.YacsLogger;
import yacs.utils.YacsUtils;
import yacs.job.TaskContainer;
import yacs.job.interfaces.JobMasterGroupInterface;

public class WorkerEmulator extends yacs.YacsComponent implements TaskExecutionContext 
{
	
	private TaskContainer myTask;
	private ComponentIdEmulator myGlobalId;
	private long uid=0;
	private WorkerThread worker;
	private StateChangeSensorEmulator stateChangeInterface = new StateChangeSensorEmulator();
	private JobMasterGroupInterface masters;
	
	public WorkerEmulator( String workerNicheId ){
		this.createYacsLogger( "WorkerEmulator", null, false, true, null );
		this.yacsLog.setConsole( System.out );
		myGlobalId = new ComponentIdEmulator( workerNicheId );
	}
	public TaskContainer getTask(){
		return myTask;
	}
	public ComponentId getComponentId(){
		return myGlobalId;
	}
	
	public boolean performTask( TaskContainer task, boolean redeployment ){
		log("Worker(Emulator).performTask: " + task.getTid() + " for master: NULL, redep: " + redeployment );
		log("\tholder:" + (task.getWorker()==null?"NULL!":task.getWorker().getId()));
		task.setWorker( this.myGlobalId );
		
		//this.masterGroup = masterGroup;
		
		if( myTask != null ){
			log("Worker.performTask: Can't accept new task. I'm already busy!" );
			return false;
		}
				
		myTask = task;
		myTask.setStatus( YACSNames.TASK_NOT_INITIALIZED ); // TODO: examine well the semantics of processing status being transported between processing Workers.
		myTask.setExecutionContext(this);
		
		worker = new WorkerThread();
		worker.start();
		
		return true;
	}
	private void reportTaskStatus(){
		if( this.masters != null ){
			masters.reportTaskStatus( this.myTask, myGlobalId, YACSNames.DUMMY_PARAM );
		}
		else
			log( "No MasterInterface registered!" );
	}
	
	// emulation specific functions
	public void join() throws Exception {
		if( worker != null )
			worker.join();
	}
	public void registerMasterInterface( JobMasterGroupInterface masters ){
		this.masters = masters;
	}
	
	
	
	// the actual work
	class WorkerThread extends Thread {
		public void run(){
			try {
				log("Worker.WorkerThread.run: starting..." );
				
				// TODO: report to masters before or after checkpointing worker state?
				// is it more likely to fail on checkpointing or reporting... event vs binding...?
				
				// report initial state to master
				reportTaskStatus();
				
				// wait until sensor deployed and bound
				log("\tWaiting for sensor interface...");
				while( stateChangeInterface == null )
					YacsUtils.ignorantSleep(10);
				
				log("Worker.WorkerThread.run: meta-checkpointing: initial state..." );
				// TODO: hmmm, or should I not use the way that tells the task to perpare checkpoint... it shouldn't even have started to do anything, i.e. its state is already known by the system 
				stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,myTask) ); //checkpoint();
				
				log("Worker.WorkerThread.run: invoking task..." );
				myTask.execute( false );
				
				// report end state to master
				reportTaskStatus();
				
				log("Worker.WorkerThread.run: meta-checkpointing: end state..." );
				// TODO: might be wise to skip the prepareCheckpointing of checkpoint(). The task is already finished what it was doing and should have checkpointed what it wanted
				stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,myTask) ); //checkpoint();				
				
				log("WorkerEmulator.WorkerThread.run: Done!" );
			}
			catch( Exception e ){
				log("Worker.WorkerThread.run: error: " + e.getMessage() );
				e.printStackTrace();
			}
		}
	}
	
	

	// TaskExecutionInterface
	public void metacheckpoint() {
		if( myTask != null ){
			myTask.prepareCheckpoint();
			myTask.debug_printMetacheckpoint();
		}
		stateChangeInterface.checkpoint( myGlobalId, new TaskCheckpoint(uid++,myTask) );
	}
	public void copyFile(Object from, Object to) throws Exception {
		try {
			File fi = new File((String)from);
			File fo = new File((String)to);
			
			FileChannel ic = new FileInputStream(fi).getChannel();
			FileChannel oc = new FileOutputStream(fo).getChannel();
			
			ic.transferTo(0, ic.size(), oc);
			
			ic.close();
			oc.close();
		}
		catch( Exception e ){
			throw e;
		}
	}
	public boolean deleteFile(Object location) throws Exception {
		File del = new File((String)location);
		if( !del.isFile() ){
			throw new Exception(location+" is not a file. Cannot delete!");
		}
		return del.delete();
	}

	// helpers
	public YacsLogger createLogger(	String name, String id, boolean toNiche, boolean toConsole ){
		return new YacsLogger( name, id, toNiche, toConsole, null );
	}
	
}
