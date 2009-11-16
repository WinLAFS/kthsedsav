package yacs.zemulation;

import dks.niche.ids.ComponentId;

import yacs.frontend.FrontendInterface;
import yacs.interfaces.YACSNames;
import yacs.job.*;
import yacs.frontend.gmovie.*;
import yacs.job.interfaces.JobMasterGroupInterface;

public class FrontendEmulator implements FrontendInterface {

	private GMGui gmovie = null;
	private Job submittedJob = null;
	
	public void emulate(){
		gmovie = new GMGui(this); 
		javax.swing.SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						gmovie.setLocationRelativeTo(null);
						gmovie.setVisible(true);
					}
				}
			);
	}
	
	@Override
	public boolean deleteJob(Job job) {
		System.out.println("FE: Job deleted");
		this.submittedJob = null;
		return true;
	}

	@Override
	public String submit(Job job) throws Exception {
		submittedJob = job;
		new ResultReturner().start();
		return "Fake";
	}
	
	private class ResultReturner extends Thread implements JobMasterGroupInterface {
		public void run(){
			try {
				//sleep(5000);
				
				useWorkerEmulatorForProcessing();
				//customTaskProcessing();
			}
			catch( Exception e ){
				e.printStackTrace();
			}
		}
		
		private TaskContainer currentTask = null;
		private void useWorkerEmulatorForProcessing() throws Exception {
			Job result = new Job( submittedJob.getName() );
			
			int a=1;
			for( TaskContainer task : submittedJob.getRemaining() ){
				currentTask = task;
				
				WorkerEmulator worker = new WorkerEmulator("FE.emu."+(a++));
				currentTask.setWorker( worker.getComponentId() );
				worker.registerMasterInterface( this );
				
				/**
				 * Emulate the change-report the master makes after assigning the task.
				 * TODO: Consider skipping this in the Master since the Worker also reports in: reportTaskChange
				 */
				gmovie.taskChange( currentTask );
				
				worker.performTask( NetworkSimulator.serialize(task), false );
				worker.join(); // wait for it to complete
				
				TaskContainer taskResult = NetworkSimulator.serialize( worker.getTask() );
				
				if( taskResult.getStatus() == YACSNames.TASK_COMPLETED )
					result.getDone().add( taskResult );
				else
					result.getFailed().add( taskResult );
			}
			
			gmovie.jobResult( result );
		}

		private void customTaskProcessing() throws Exception {
			Job result = new Job( submittedJob.getName() );
			
			for( TaskContainer task : submittedJob.getRemaining() ){
				
				TaskContainer copy = NetworkSimulator.serialize( task );
				copy.setStatus( YACSNames.TASK_IS_PROCESSING );
				copy.setWorker( new ComponentIdEmulator("Fid:1") );
				gmovie.taskChange( copy );
				sleep(1000);
				
				TaskContainer copy2 = NetworkSimulator.serialize( task );
				copy2.setStatus( YACSNames.TASK_IS_PROCESSING );
				copy2.setResultCode( YACSNames.RESULT_OK );
				copy2.setWorker( new ComponentIdEmulator("Fid:2") ); // test worker change
				gmovie.taskChange( copy2 );
				sleep(1000);
				
				TaskContainer copy3 = NetworkSimulator.serialize( task );
				copy3.setStatus( YACSNames.TASK_COMPLETED );
				copy3.setResultCode( YACSNames.RESULT_OK );
				copy3.setWorker( new ComponentIdEmulator("Fid:3") ); // test worker change
				gmovie.taskChange( copy3 );
				sleep(1000);
				
				result.getDone().add( copy3 );
			}
			
			gmovie.jobResult( result );
		}

		// MasterGroupInterface: for receiving "task-processing-status-events" from the worker emulator
		public void volunteer( ComponentId worker, boolean dummy ){
			System.out.println("FrontendEmulator.ResultReturner.volunteer: fake");
		}
		public void reportTaskStatus( TaskContainer task, ComponentId worker, boolean dummy ){
			if( currentTask == null ){
				System.out.println("FrontendEmulator.ResultReturner.reportTaskStatus: current task is null!");
				return;
			}
			TaskContainer held = currentTask; // just to be able to use almost the same code as from Master.java
			
			boolean reportChange = false;
			
			reportChange = !( held.getWorker().getId().toString().equals( worker.getId().toString()));
			// task state on worker should be more recent... merge with local
			held.merge( task );
			
			if( held.getStatus() == YACSNames.TASK_COMPLETED ){
				reportChange = true;
			}
			else if ( held.getStatus() == YACSNames.TASK_FAILED ){
				reportChange = true;
			}
		
			if( reportChange ){
				gmovie.taskChange( held );
			}
		}
		public void workerWatcherInitalized( boolean dummy ){
			System.out.println("FrontendEmulator.ResultReturner.workerWatcherInitalized: fake");
		}
		public void masterWatcherInitalized( boolean dummy ){
			System.out.println("FrontendEmulator.ResultReturner.masterWatcherInitalized: fake");
		}
		public void irrecoverableWorkerFailure( TaskContainer task ){
			System.out.println("FrontendEmulator.ResultReturner.irrecoverableWorkerFailure: fake");
		}
		public void publishState( boolean dummy ){
			System.out.println("FrontendEmulator.ResultReturner.publishState: fake");
		}
	}

}
