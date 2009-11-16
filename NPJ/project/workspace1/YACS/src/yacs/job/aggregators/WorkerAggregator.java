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
import yacs.utils.monitoring.MProducer;
import yacs.utils.monitoring.WorkerUtilizationMessage;

public class WorkerAggregator extends yacs.YacsManagementElement implements 
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
	private int tasksStarted = 0;
	private int tasksCompleted = 0;
	private int tasksDeleted = 0;
	// members for replication - end
	
	private MProducer monitor = new MProducer();
	
	// empty constructor always needed!
	public WorkerAggregator() {

	}
	
	public void eventHandler(Object e, int flag) {
		//logger.log("WorkerAggregator got event of type "+e.getClass().getSimpleName());
		log("WorkerAggregator.eventHandler: "+e);
		
		if( !(e instanceof WorkerManagementEvent) ){
			log("Event is not WME event!");
			return;
		}
		
		WorkerManagementEvent wme = (WorkerManagementEvent)e;
		
		handleWorkerManagementEvent( wme );
		
		// forward to CM... hmmm
		trigger( wme );
	}
	
	// handlers
	private void handleWorkerManagementEvent( WorkerManagementEvent mme ){
		if( mme.getType() == WorkerManagementEvent.TYPE.TASK_STARTED ){
			tasksStarted++;
		}
		else if( mme.getType() == WorkerManagementEvent.TYPE.TASK_COMPLETED ){
			tasksCompleted++;
		}
		else if( mme.getType() == WorkerManagementEvent.TYPE.TASK_DELETED ){
			tasksDeleted++;
		}
		
		if( !isActiveReplica() )
			return;
		
		WorkerUtilizationMessage wum = new WorkerUtilizationMessage();
		wum.setOngoingTasks(tasksStarted);
		wum.setCompletedTasks(tasksCompleted);
		log( "Sending monitoring message: " + wum );
		monitor.send( wum );		
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
		log("WorkerAggregator.getAttributes");
		return new Object[]{	new Integer(tasksStarted),
								new Integer(tasksCompleted),
								new Integer(tasksDeleted) };
	}
	
	// //////////////////////////////////////////////////////////////////////////
	// ////////////////////////////// init
	// ////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected void doInit(Object[] parameters) {
	}

	protected void doInit(NicheActuatorInterface actuator) {
		this.actuator = actuator;
		this.logger = actuator.testingOnly();
		
		if( myId != null ){
			this.createYacsLogger( "WorkerAggregator", String.valueOf(myId.getReplicaNumber()), true, true, logger );
		}
		else {
			this.createYacsLogger( "WorkerAggregator", null, true, true, logger );
		}
		this.logReinit(); // will log only if re-inited
	}

	protected void doInitId(Object id) {
		log("WorkerAggregator.initId: " + id + ", rep#: " + (id!=null&&(id instanceof NicheId)?((NicheId)id).getReplicaNumber():"NULL"));
		myId = (NicheId) id;
		setActiveReplica( myId.getReplicaNumber() == 0 );
		log("\tWA-Rep.#: " + myId.getReplicaNumber());
		
		if( yacsLog != null ){
			yacsLog.setId( String.valueOf(myId.getReplicaNumber()) );
		}
	}
	
	protected void doReinit(Object[] applicationParameters) {
		log("WorkerAggregator.REinit(Object[]): "  + applicationParameters);
		
		this.setReinited(true);
		
		if( applicationParameters == null || applicationParameters.length != 3 ){
			log("\tArray ABNORMAL!");
			
			this.setAbnormalReinit( "Params null or length!=3" );
			if( yacsLog != null ) this.logReinit();
			
			return;
		}
		if( yacsLog != null ) this.logReinit();
		
		tasksStarted = 		(applicationParameters[0]==null?0:(Integer)applicationParameters[0]);
		tasksCompleted = 	(applicationParameters[1]==null?0:(Integer)applicationParameters[1]);
		tasksDeleted = 		(applicationParameters[2]==null?0:(Integer)applicationParameters[2]);
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
		log("WorkerAggregator.bindFc: " + interfaceName );
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
		log("WA started. Version: " + YACSSettings.YACS_VERSION);
	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;
	}
}
