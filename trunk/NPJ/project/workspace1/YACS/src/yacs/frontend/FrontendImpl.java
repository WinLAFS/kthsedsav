package yacs.frontend;

import java.util.*;
import java.io.*;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.nicheOS.OverlayAccess;
import org.objectweb.jasmine.jade.util.FractalUtil;
import org.objectweb.jasmine.jade.util.NoSuchComponentException;

import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;

import yacs.interfaces.YACSNames;
import yacs.job.*;
import yacs.job.interfaces.JobManagementInterface;
import yacs.job.interfaces.JobResultInterface;
import yacs.job.tasks.*;
import yacs.resources.interfaces.ResourceServiceRequestInterface;
import yacs.resources.data.*;
import yacs.utils.YacsUtils;
import yacs.job.helpers.SubmissionReply;
import yacs.interfaces.YACSSettings;
import yacs.utils.YacsTimer;

import yacs.frontend.gmovie.*;

public class FrontendImpl extends yacs.YacsComponent implements JobResultInterface, Runnable, BindingController, LifeCycleController, FrontendInterface 
{

	// Client Interfaces
	private Component myself;
	private JobManagementInterface jobManagement;
	private ResourceServiceRequestInterface resourceServiceRequest;

	private boolean status;

	ComponentId myGlobalId;
	
	NicheComponentSupportInterface nicheOSSupport;
	NicheAsynchronousInterface logger;
	
	private FrontendClientInterface frontendClient = null;
	private long tuid;
	
	public FrontendImpl() {
		log("FrontendImple created: " + this);
	}
	
	// Runnable
	// TODO: Why is Frontend runnable?
	public void run(){log("FrontendImpl.run(): "+this);}
	
	// BEGIN: WORKERS
	abstract class WorkerThread extends Thread implements FrontendClientInterface {}
	class EvaluationWorker extends WorkerThread {
		private long start;
		private YacsTimer jobProcessingTime;
		private Job job;
		
		public void run(){
			YacsTimer timer = new YacsTimer( tuid );
			try {
				log("EvaluationWorker starting. Will wait until RS binding and then 45 seconds before finding Master and submitting.");
				do{		sleep(5000); }
				while( resourceServiceRequest == null );
				
				sleep(30000);
				
				Serializable[] initParams = new Serializable[]{	"/taskfiles/commands.txt",
																null,
																new Long(YACSSettings.TEST__TASK_BUSY_WAIT_MSEK) };

				job = new Job("EJ");
				job.setCreator(myGlobalId);
				
				for( int a=1; a<=YACSSettings.TEST__NUMBER_OF_TASKS; a++ ){
				
					job.getRemaining().add( TaskContainer.contain(	a, YACSNames.DEFAULT_REDEPLOYABLE,
																	YACSSettings.TEST__TASK_CLASS_NAME,
																	YACSSettings.TEST__TASK_CLASS_LOCATION,
																	initParams) );
				}
				
		
				log("Starting job submission process...");
				
				// create ResourceRequest for Master
				ResourceRequest specs = new ResourceRequest();
				specs.setComponentType( YACSNames.MASTER_COMPONENT );
				
				start = System.currentTimeMillis(); // record start of interacting with YACS
				ComponentId master = null;
				for( int a=0; a<5 && master==null; a++ ){
					ResourceRequest reply  = resourceServiceRequest.request( specs );
					time("EW",job.getName(),"MRR",""+(a+1),(System.currentTimeMillis()-start),null); // Master Resource Request #
					if( reply.getAvailableComponents().size() > 0 ){
						master = reply.getAvailableComponents().get(0);
						log("Try "+(a+1)+" gave Master:" + master.getId());
						time("EW",job.getName(),"MF",null,(System.currentTimeMillis()-start),null); // Master Found
					}
				}
				if( master == null ){
					log("Didn't find master. Quitting!");
					return;
				}
				
				
				timer.reset();
				BindId bid = nicheOSSupport.bind(	myGlobalId,	YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE, 
													master, 	YACSNames.JOB_MANAGEMENT_SERVER_INTERFACE, 
													JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
				timefx("EW",job.getName(),timer.getTtid(),"MBC",null,timer.elapsed(),null); // Master Binding Called
				log("Bound to Master:" + bid.getId());
				while( jobManagement == null )
					YacsUtils.ignorantSleep(10);
				timefx("EW",job.getName(),timer.getTtid(),"MBD",null,timer.elapsed(),null); // Master Binding Done
				
				time("EW",job.getName(),"MB",null,(System.currentTimeMillis()-start),null); // Before Job Submission
				
				log("Sending process request!");
				SubmissionReply jr = null;
				try {
					timer.reset(); jobProcessingTime = new YacsTimer( timer.getTtid() );
					jr = jobManagement.performJob( job, false );
					timefx("EW",job.getName(),timer.getTtid(),"PJFT",null,timer.elapsed(),null); // Perform Job Function Time
				}
				catch( Exception e ){
					log( "Exception when submitting job: " +  e.getMessage() );
					e.printStackTrace();
				}
				time("EW",job.getName(),"JS",null,(System.currentTimeMillis()-start),null); // Job submitted
				
				if( !jr.isAccepted() ){
					log("Job not accepted by master!");					
					time("EW",job.getName(),"TYT-NA",null,(System.currentTimeMillis()-start),null); // Total Yacs Time (-Not-Accepted), spent interacting with YACS service, including initial master find etc
				}	
				else
					log("Job accepted by JMG:" + jr.getMasterGroup().getId());
			}
			catch( Exception e ){
				log("EvaluationWorker exception: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		public void jobResult(Job result){
			log("\tRemaining: " 	+ result.getRemaining().size());
			log("\tPending: " 	+ result.getPending().size());
			log("\tCompleted: " 	+ result.getDone().size());
			log("\tFailed:    " 	+ result.getFailed().size());
			
			if( result.getRemaining().size()>0 || result.getPending().size()>0 || result.getFailed().size()>0 ){
				timefx("EW",job.getName(),jobProcessingTime.getTtid(),"TJPT-F",null,jobProcessingTime.elapsed(),null); // Total Job Processing (-Failed) time
				time("EW",job.getName(),"TYT-F",null,(System.currentTimeMillis()-start),null); // Total Yacs Time (-Failed) spent interacting with YACS service, including initial master find etc
			}
			else {
				timefx("EW",job.getName(),jobProcessingTime.getTtid(),"TJPT",null,jobProcessingTime.elapsed(),null); // Total Job Processing time
				time("EW",job.getName(),"TYT",null,(System.currentTimeMillis()-start),null); // Total Yacs Time spent interacting with YACS service, including initial master find etc
			}
		}
		public void taskChange(TaskContainer task){
			String msg = 	"Task change: T:"
				+ task.getTid() + " @"
				+ (task.getWorker()==null?"NULL":task.getWorker().getId().toString()) + ", S:"
				+ task.getStatus();
			
			log("\t"+msg);
		}
	}
	// END: WORKERS
	
	
	public void jobEnded(String jobName){
		log("FrondendImpl.jobEnded: "+jobName);
	}
	
	// Interface: JobResultInterface
	public void receiveJobResult( Job result ){
		log("Job result received: " + result);
		if( this.frontendClient != null )
			frontendClient.jobResult(result);
	}
	public void receiveTaskChange( TaskContainer task ){
		log("TaskChange: " + task);
		if( this.frontendClient != null )
			frontendClient.taskChange( task );
	}
	
	/* (non-Javadoc)
	 * @see yacs.frontend.FrontendInterface#submitMovie(yacs.job.Job)
	 */
	public String submit( Job job ) throws Exception {
		
		job.setCreator(this.myGlobalId);
		
		// finding worker to take on job
		ResourceRequest specs = new ResourceRequest();
		specs.setComponentType( YACSNames.MASTER_COMPONENT );
		
		ComponentId master = null;
		for( int a=0; a<5 && master==null; a++ ){
			ResourceRequest reply  = resourceServiceRequest.request( specs );
			if( reply.getAvailableComponents().size() > 0 )
				master = reply.getAvailableComponents().get(0);
			else{
				try{
					Thread.sleep(1000);
				}
				catch( Exception e ){}
			}
		}
		if( master == null ){
			log("Frontend didn't find master. Quitting!");
			throw new Exception("No master found");
		}
		
		// bind to the supplied master
		BindId bid = nicheOSSupport.bind(	myGlobalId,	YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE, 
											master, 	YACSNames.JOB_MANAGEMENT_SERVER_INTERFACE, 
											JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE );
		log("Bound to master: " + bid.getId());
		
		// wait until the binding is completed
		while( jobManagement == null )
			YacsUtils.ignorantSleep(10);
		
		// submit the job
		SubmissionReply reply = jobManagement.performJob( job, false );
		
		if( reply == null )
			throw new Exception("SubmissionReply is null!?");
		else if( reply.isAccepted() )
			return "JM.GID: " + reply.getMasterGroup().getId().toString();
		else
			throw new Exception("Job not accepted by master: " + master.getId().toString());
		//return master.getId().toString();
	}
	/* (non-Javadoc)
	 * @see yacs.frontend.FrontendInterface#deleteJob(yacs.job.Job)
	 */
	public boolean deleteJob( Job job ){
		log("FrontendImpl.deleteJob: " + job.getName());
		
		if( jobManagement == null ){
			log("\tJobMgmt interface is null. Cannot delete!");
			return false;
		}
		
		return jobManagement.deleteJob( job );
	}

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////// Fractal Stuff ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public String[] listFc() {
		log("FrontendImpl.listFc");
		return new String[] {	"component", 
								YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE,
								YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE };
	}

	public Object lookupFc(final String itfName)
			throws NoSuchInterfaceException {
		log("FrontendImpl.lookupFc: "+itfName);
		if (itfName.equals(YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE))
			return jobManagement;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			return resourceServiceRequest;
		else if (itfName.equals("component"))
			return myself;
		else
			throw new NoSuchInterfaceException(itfName);

	}

	public void bindFc(final String itfName, final Object itfValue)
			throws NoSuchInterfaceException {
		log("FrontendImpl.bindFc: " + itfName );
		if(itfName.equals(YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE))
			jobManagement = (JobManagementInterface)itfValue;
		else if(itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = (ResourceServiceRequestInterface)itfValue;
		else if (itfName.equals("component"))
			myself = (Component) itfValue;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public void unbindFc(final String itfName) throws NoSuchInterfaceException {
		log("FrontendImpl.unbindFc: " + itfName);
		if (itfName.equals(YACSNames.JOB_MANAGEMENT_CLIENT_INTERFACE))
			jobManagement = null;
		else if (itfName.equals(YACSNames.RESOURCE_SERVICE_REQUEST_CLIENT_INTERFACE))
			resourceServiceRequest = null;
		else if (itfName.equals("component"))
			myself = null;
		else
			throw new NoSuchInterfaceException(itfName);
	}

	public String getFcState() {
		log("FrontendImpl.getFcState");
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		log("FrontendImpl.startFc");
		
		Component jadeNode = null;
		Component niche = null;
		OverlayAccess overlayAccess = null;

		Component comps[] = null;
		try {
			comps = Fractal.getSuperController(myself).getFcSuperComponents();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < comps.length; i++) {
			try {
				if (Fractal.getNameController(comps[i]).getFcName().equals("managed_resources")) {
					jadeNode = comps[i];
					break;
				}
			}
			catch (NoSuchInterfaceException e) {
				e.printStackTrace();
			}
		}

		try {
			niche = FractalUtil.getFirstFoundSubComponentByName(jadeNode,"nicheOS");
		}
		catch (NoSuchComponentException e1) {
			e1.printStackTrace();
		}

		try {
			overlayAccess = (OverlayAccess)niche.getFcInterface("overlayAccess");
		}
		catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}

		nicheOSSupport = overlayAccess.getOverlay().getComponentSupport(myself);
		logger = overlayAccess.getOverlay().getNicheAsynchronousSupport();
		this.createYacsLogger( "Frontend", null, true, true, logger );
		myGlobalId = nicheOSSupport.getResourceManager().getComponentId(myself);

		nicheOSSupport.setOwner(myGlobalId);
		
		status = true;
		
		//new Worker().start(); log("\tWorker thread started");
		//if( YACSSettings.TEST__SUBMIT_JOB ){ worker = new TestWorker(); worker.start(); log("\tWorker thread started"); }
		//worker = new TranscodingWorker(); worker.start(); log("\tWorker thread started");
		if( YACSSettings.TEST__SUBMIT_JOB ){ log("EvaluationWorker setup beginning!"); EvaluationWorker ew = new EvaluationWorker(); log("EvaluationWorker created!"); this.frontendClient=(FrontendClientInterface)ew; ew.start(); log("EvaluationWorker started!"); }
		//if( YACSSettings.TEST__SUBMIT_JOB ){ log("gMovie GUI setup beginning!"); gmovie = new GMGui(this); log("gMovie GUI created!"); javax.swing.SwingUtilities.invokeLater(new Runnable() {public void run() {gmovie.setLocationRelativeTo(null);gmovie.setVisible(true);}}); log("gMovie GUI started!"); }
		
		log("Started YACS FRONTEND component = " + myGlobalId.getId());
	}

	public void stopFc() throws IllegalLifeCycleException {
		log("FrontendImpl.stopFc");
		status = false;

	}
}
