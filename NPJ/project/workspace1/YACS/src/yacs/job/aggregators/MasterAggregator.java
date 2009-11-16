package yacs.job.aggregators;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;

import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;

import yacs.interfaces.YACSSettings;
import yacs.job.events.*;
import yacs.resources.data.AvailabilityInformation;
import yacs.utils.monitoring.MProducer;
import yacs.utils.monitoring.MasterUtilizationMessage;

public class MasterAggregator extends yacs.YacsManagementElement implements 
		EventHandlerInterface, MovableInterface,
		BindingController, LifeCycleController {

	/**
	 * @serialVersionUID -
	 */
	private static final long serialVersionUID = 1L;

	// ///////////////////
	Component mySelf;
	TriggerInterface triggerInterface;
	NicheId myId;

	private NicheActuatorInterface actuator;
	private NicheAsynchronousInterface logger;
	private boolean status;
	
	// members for replication - begin
	private int jobsStarted = 0;
	private int jobsCompleted = 0;
	private int jobsDeleted = 0;
	// members for replication - end
	
	private MProducer monitor = new MProducer();
	
	// empty constructor always needed!
	public MasterAggregator() {

	}
	
	// EventHandlerInterface
	public void eventHandler(Object e, int flag) {
		//logger.log("MasterAggregator got event of type "+e.getClass().getSimpleName());
		log("MasterAggregator.eventHandler: "+e);
		
		if( !(e instanceof MasterManagementEvent) ){
			log("Event is not MME event!");
			return;
		}
		
		MasterManagementEvent mme = (MasterManagementEvent)e;
		
		handleMasterManagementEvent( mme );
		
		// forward to CM... hmmm
		trigger( mme );
	}
	
	// handlers
	private void handleMasterManagementEvent( MasterManagementEvent mme ){
		if( mme.getType() == MasterManagementEvent.TYPE.JOB_STARTED ){
			jobsStarted++;
		}
		else if( mme.getType() == MasterManagementEvent.TYPE.JOB_COMPLETED ){
			jobsCompleted++;
		}
		else if( mme.getType() == MasterManagementEvent.TYPE.JOB_DELETED ){
			jobsDeleted++;
		}
		
		if( !isActiveReplica() )
			return;
		
		MasterUtilizationMessage mum = new MasterUtilizationMessage();
		mum.setOngoingJobs(jobsStarted);
		mum.setCompletedJobs(jobsCompleted);
		log( "Sending monitoring message: " + mum );
		monitor.send( mum );
		
	}
	
	// helpers
	private void trigger( Object event ){
		if( !isActiveReplica() )
			return;
		log( "Triggering: " + event );
		triggerInterface.trigger( event );			
	}

	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// Attributes
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public Object[] getAttributes() {
		log("MasterAggregator.getAttributes");
		return new Object[]{	new Integer(jobsStarted),
								new Integer(jobsCompleted),
								new Integer(jobsDeleted) };
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// init
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected void doInit(Object[] parameters) {}

	protected void doInit(NicheActuatorInterface actuator) {
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
				
		if( myId != null ){
			this.createYacsLogger( "MasterAggregator", String.valueOf(myId.getReplicaNumber()), true, true, logger );
		}
		else {
			this.createYacsLogger( "MasterAggregator", null, true, true, logger );
		}
		this.logReinit(); // will log only if re-inited
	}

	protected void doInitId(Object id) {
		log("MasterAggregator.initId: " + id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL") );
		myId = (NicheId) id;
		setActiveReplica( myId.getReplicaNumber() == 0 );
		log("\tMA-Rep.#: " + myId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myId.getReplicaNumber()) );
		}
	}
	
	protected void doReinit(Object[] applicationParameters) {
		log("MasterAggregator.REinit(Object[]): "  + applicationParameters);
		
		this.setReinited(true);
		
		if( applicationParameters == null || applicationParameters.length != 3 ){
			log("\tArray ABNORMAL!");
			
			this.setAbnormalReinit( "Params null or length!=3" );
			if( yacsLog != null ) this.logReinit();
			
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		jobsStarted = 		(applicationParameters[0]==null?0:(Integer)applicationParameters[0]);
		jobsCompleted = 	(applicationParameters[1]==null?0:(Integer)applicationParameters[1]);
		jobsDeleted = 		(applicationParameters[2]==null?0:(Integer)applicationParameters[2]);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// //////////////////////// Fractal Stuff
	// ///////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	public String[] listFc() {
		return new String[] { 	FractalInterfaceNames.COMPONENT,
								FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE,
								FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE };
	}

	public Object lookupFc(String interfaceName) throws NoSuchInterfaceException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else if (interfaceName.equals(FractalInterfaceNames.ACTUATOR_CLIENT_INTERFACE))
			return null;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			return triggerInterface;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void bindFc(String interfaceName, Object stub) throws NoSuchInterfaceException, IllegalBindingException,	IllegalLifeCycleException {
		log("MasterAggregator.bindFc: " + interfaceName );
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			mySelf = (Component) stub;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = (TriggerInterface) stub;
		else
			throw new NoSuchInterfaceException(interfaceName);
	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException,	IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			mySelf = null;
		else if (interfaceName.equals(FractalInterfaceNames.TRIGGER_CLIENT_INTERFACE))
			triggerInterface = null;
		else
			throw new NoSuchInterfaceException(interfaceName);

	}

	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {
		status = true;
		log("MA started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
	}
}
