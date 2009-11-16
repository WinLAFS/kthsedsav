package yacs.zemulation;

import java.io.Serializable;
import java.text.*;

import yacs.interfaces.YACSNames;
import yacs.job.TaskContainer;
import yacs.job.tasks.DirectedTask;
import yacs.job.tasks.MovieTranscodingDirectedTask;
import yacs.job.tasks.MovieTranscodingDirectedTask2;
import yacs.utils.YacsUtils;
import yacs.job.state.*;
import yacs.job.*;
import yacs.resources.data.*;

import dks.niche.interfaces.IdentifierInterface;

public class Emulate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		//System.setProperty("yacs.os.name","Windows");
		System.setProperty("yacs.nfs.base","z:\\");
		//System.setProperty("yacs.os.name","Linux");
		//System.setProperty("yacs.nfs.base","/home/atli/");
		System.out.println("Non.existing prop: "+ System.getProperty("bleh"));
		System.out.println( DirectedTask.class.getName() );
		
		
		long a=1, start=0,stop=0, its=1000;
		
		AvailabilityInformation ai = new AvailabilityInformation();
	
		StringBuilder builder = new StringBuilder( 1000000 );
		
		start=System.currentTimeMillis();
		for( int i=0;i<its;i++ ){
			
			ai.setBusyMasterComponents(a++);
			ai.setBusyWorkerComponents(a++);
			ai.setBusyResourceComponents(a++);
			ai.setFreeMasterComponents(a++);
			ai.setFreeWorkerComponents(a++);
			ai.setFreeResourceComponents(a++);
			
			String str = String.format("AvInfo sent: R:(%d),M:(%d/%d),W:(%d/%d)", 	
					ai.getFreeResourceComponents()+ai.getBusyResourceComponents(),
					ai.getFreeMasterComponents(),ai.getFreeMasterComponents()+ai.getBusyMasterComponents(),
					ai.getFreeWorkerComponents(),ai.getFreeWorkerComponents()+ai.getBusyWorkerComponents() );

			
			builder.append( str );
		}
		stop=System.currentTimeMillis();
		System.out.println(stop-start);
		
		a=1;
		start=System.currentTimeMillis();
		for( int i=0;i<its;i++ ){
			
			ai.setBusyMasterComponents(a++);
			ai.setBusyWorkerComponents(a++);
			ai.setBusyResourceComponents(a++);
			ai.setFreeMasterComponents(a++);
			ai.setFreeWorkerComponents(a++);
			ai.setFreeResourceComponents(a++);
			
			String str = "AvInfo sent: R:("+(ai.getFreeResourceComponents()+ai.getBusyResourceComponents())+"),"
							+"M:("+ai.getFreeMasterComponents()+"/"+(ai.getFreeMasterComponents()+ai.getBusyMasterComponents())+"),"
							+"W:("+ai.getFreeWorkerComponents()+"/"+(ai.getFreeWorkerComponents()+ai.getBusyWorkerComponents())+")"; 	
			
			builder.append( str );
		}
		stop=System.currentTimeMillis();
		System.out.println(stop-start);
		
		//emulateDirectedTask();
		//emulateDirectedTranscoding();
		//emulateGMovieGui();
		//testNfsMappings();
		//testSerialization();
	}
	
	public static void testSerialization(){
		
		try {
			// TaskCheckpoint... for WW serialization
			/*Serializable[] initParams = new Serializable[]{
					"C:\\SEDS\\Lokaverkefni\\testing\\task_commands.txt",
					null 
			};
			
			TaskContainer task = null;
			task = TaskContainer.contain(	1,	YACSNames.DEFAULT_REDEPLOYABLE, 
											DirectedTask.class.getName(),
											"C:\\SEDS\\Lokaverkefni\\code3\\YACS\\bin\\yacs\\job\\tasks\\DirectedTask.class",
											initParams );
			
			TaskCheckpoint tp = new TaskCheckpoint(-1,task);
			
			System.out.println(" Tp.id:  " + tp.getId());
			System.out.println(" Tp.tid: " + tp.getTask().getTid());
			
			TaskCheckpoint stp = (TaskCheckpoint)NetworkSimulator.serializeAny(tp);
			
			System.out.println("Stp.id:  " + stp.getId());
			System.out.println("Stp.tid: " + stp.getTask().getTid());*/
			
			// componentid
			/*ComponentIdEmulator cid = new ComponentIdEmulator("sertest");
			System.out.println( "ser.cid: " + ((cid instanceof Serializable) ? "YES" : "NO")  );
			IdentifierInterface id = cid;
			System.out.println( "ser. id: " + ((id instanceof Serializable) ? "YES" : "NO")  );
			
			System.out.println( cid );
			
			IdentifierInterface sid = (IdentifierInterface)NetworkSimulator.serializeAny(id);
			System.out.println( sid );*/
			
			Job job = new Job("serjob");
			JobCheckpoint jcp = new JobCheckpoint(-2,job, new java.util.Hashtable<String,String>());
			
			System.out.println(" Jcp.id:  " + jcp.getVersion());
			System.out.println(" Job.name: " + jcp.getJob().getName() );
			
			JobCheckpoint sjcp = (JobCheckpoint)NetworkSimulator.serializeAny(jcp);
			
			System.out.println("SJcp.id:  " + sjcp.getVersion());
			System.out.println("SJob.name: " + sjcp.getJob().getName() );
			
			
		}
		catch( Exception e ){
			e.printStackTrace();
		}
		
	}
	public static void testNfsMappings(){
		String rel = "/transcoding/demonstrator/yacs.bat";
		String path = YacsUtils.nfsRelativeToNfsAbsolute(rel);
		System.out.println( path );
		path = YacsUtils.nfsAbsoluteToNfsRelative( path );
		System.out.println( path );
	}
	public static void emulateGMovieGui() throws Exception {
		new FrontendEmulator().emulate();
	}
	public static void emulateDirectedTask() throws Exception {
		Serializable[] initParams = new Serializable[]{
				"C:\\SEDS\\Lokaverkefni\\testing\\task_commands.txt",
				null 
		};
		
		TaskContainer task = null;
		task = TaskContainer.contain(	1,	YACSNames.DEFAULT_REDEPLOYABLE, 
										DirectedTask.class.getName(),
										"C:\\SEDS\\Lokaverkefni\\code3\\YACS\\bin\\yacs\\job\\tasks\\DirectedTask.class",
										initParams );
										
		WorkerEmulator worker = new WorkerEmulator("DT.emu");
		worker.performTask( NetworkSimulator.serialize(task), false );
		worker.join();
		
		TaskContainer result = NetworkSimulator.serialize( worker.getTask() );
		System.out.println("Task status is: " + result.getStatus());
		System.out.println("Task result is: " + result.getResultCode());
	}
	public static void emulateDirectedTranscoding() throws Exception {
		int a=1;
		String movie = "C:\\SEDS\\Lokaverkefni\\temp\\transcoding_test\\230549.avi";
		
		Serializable[] initParams = new Serializable[]{
				"C:\\SEDS\\Lokaverkefni\\testing\\task_commands.txt",
				null,
				"C:\\SEDS\\Lokaverkefni\\temp\\transcoding_test\\yacs_gmovie_vlc.bat",
				"mp4v", "128",
				"mpga", "128",
				"1.0",
				movie,
				movie+a 
		};
		
		TaskContainer task = null;
		task = TaskContainer.contain(	a,	YACSNames.DEFAULT_REDEPLOYABLE, 
										MovieTranscodingDirectedTask2.class.getName(),
										"C:\\SEDS\\Lokaverkefni\\code3\\YACS\\bin\\yacs\\job\\tasks\\MovieTranscodingDirectedTask2.class",
										initParams );
										
		WorkerEmulator worker = new WorkerEmulator("DTT.emu");
		worker.performTask( NetworkSimulator.serialize(task), false );
		worker.join();
		
		TaskContainer result = NetworkSimulator.serialize( worker.getTask() );
		System.out.println("Task status is: " + result.getStatus());
		System.out.println("Task result is: " + result.getResultCode());
	}
	public static void emulateTranscoding() throws Exception {
		int a=1;
		String movie = "C:\\SEDS\\Lokaverkefni\\temp\\transcoding_test\\230549.avi";
		
		Serializable[] initParams = new Serializable[]{
				"C:\\SEDS\\Lokaverkefni\\temp\\transcoding_test\\yacs_gmovie_vlc.bat",
				"mp4v", "128",
				"mpga", "128",
				"1.0",
				movie,
				movie+a 
		};
		
		TaskContainer task = null;
		task = TaskContainer.contain(	a,	YACSNames.DEFAULT_REDEPLOYABLE, 
										MovieTranscodingDirectedTask.class.getName(),
										"C:\\SEDS\\Lokaverkefni\\code3\\YACS\\bin\\yacs\\job\\tasks\\MovieTranscodingDirectedTask.class",
										initParams );
										
		WorkerEmulator worker = new WorkerEmulator("T.emu");
		worker.performTask( NetworkSimulator.serialize(task), false );
		worker.join();
		
		TaskContainer result = NetworkSimulator.serialize( worker.getTask() );
		System.out.println("Task status is: " + result.getStatus());
		System.out.println("Task result is: " + result.getResultCode());
	}

}
