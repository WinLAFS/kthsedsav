/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.fractal;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.jasmine.jade.util.Invocation;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.events.ComponentFailEvent;
import dks.niche.exceptions.DestinationUnreachableException;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.fractal.interfaces.DeployActuatorsInterface;
import dks.niche.fractal.interfaces.DeploySensorsInterface;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.fractal.interfaces.InitInterface;
import dks.niche.fractal.interfaces.ManagementElementAttributeController;
import dks.niche.fractal.interfaces.MovableInterface;
import dks.niche.fractal.interfaces.SensorInitInterface;
import dks.niche.fractal.interfaces.ActuatorInitInterface;
import dks.niche.fractal.interfaces.TriggerInterface;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.NicheId;
import dks.niche.ids.ResourceId;
import dks.niche.ids.SNR;
import dks.niche.ids.SNRElement;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.LoggerInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.OperationManagerInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.messages.UpdateSNRRequestMessage;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.NicheOSSupportFork;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.SimpleResourceManager;
import dks.niche.wrappers.Subscription;

/**
 * The <code>ManagerId</code> class
 * 
 * @author Joel
 * @version $Id: ManagerId.java 294 2006-05-05 17:14:14Z joel $
 */
public class ManagementElement implements
		ManagementElementInterface,
		NicheActuatorInterface,
		DeploySensorsInterface,
		DeployActuatorsInterface,
		TriggerInterface,
		OperationManagerInterface,
		ManagementElementAttributeController,
		BindingController,
		LifeCycleController {

	// Client interfaces
	transient InitInterface initInterface;

	transient EventHandlerInterface eventHandlerInterface;

	transient MovableInterface movableInterface;

	transient BindingController hostedComponent;
	
	private static final int MAX_CONCURRENT_OPERATIONS = 500;

	final String waitForSynchronousReturnValue = "_isSynchronous";

	transient Object[] waitForResults = new Serializable[MAX_CONCURRENT_OPERATIONS];

	protected int operationId = 0;

	transient Serializable[] synchronizedObjects = new Serializable[MAX_CONCURRENT_OPERATIONS];

	// BindingController applicationSpecificManager;

	// ///////////////////
	Component mySelf;

	int replicaNumber;
	int mode;
	
	private boolean status;

	// can be discussed which should / should not be visible!
	protected transient NicheManagementInterface myManagementInterface;

	protected transient NicheAsynchronousInterface myPrivateHost;
	
	protected transient NicheActuatorInterface fork;
	
	

	transient SimpleResourceManager myRM;
	transient Random myRandom;


	protected NicheId myId;

	protected Serializable[] applicationParameters;
	protected ArrayList<Subscription> mySources;
	
	ArrayList<Subscription> myUserSinks; // can/could be hashmap
	HashMap<String, Subscription> mySystemSinks; // 
	
	private ManagementDeployParameters myManagementDeployParameters;

	//Sensor stuff: OPTIONAL!
	
	protected String sensorClassName;
	protected String sensorEventClassName;
	protected Serializable[]sensorParameters;
	
	//Actuator stuff: OPTIONAL!
	
	protected String actuatorClassName;
	protected String actuatorEventClassName;
	protected Serializable[]actuatorParameters;

	
	
	
	// local parameters:
	boolean activated;

	//private NicheActuatorInterface myContainedComponentActuatorInterface;

	private boolean replicate;

	private NicheId watchedComponentId;

	private NicheId actuatedComponentId;

	private ComponentId myComponentId;

	public static final boolean randomizeDiscover =
		System.getProperty("niche.randomizeDiscover") instanceof String ? 
			Integer.parseInt(System.getProperty("niche.randomizeDiscover")) == 1 ?
			true : false
		: false; 

	public static final boolean STABLE_ID =
		System.getProperty("niche.stableid.mode") instanceof String ?
				System.getProperty("niche.stableid.mode").equals("1")
			:
				false
		;

				
	/*
	 * Let's agree on an ordering of things concerning ME and SNRs:
	 * 
	 * 1 - constructors 2 - activation, sending itself to receiver side 3 -
	 * connection, connecting itself to MEContainer on receiver side 4 - saved
	 * for migration, store/restore state! 5 - add/remove sources 6 - add/remove
	 * sinks 7 - other helper methods n - getters and setters, last
	 * 
	 */
	public ManagementElement() {

	}

	private void connect() {

		/*#%*/ String message = "ME " + myId + ":" + replicaNumber + " says: ";
		
		myUserSinks = new ArrayList<Subscription>();
		mySystemSinks = new HashMap<String, Subscription>();
		
		myPrivateHost = myManagementInterface.getNicheAsynchronousSupport();
		fork = myManagementInterface.getNicheActuator(myId);
		
		myRM = myPrivateHost.getResourceManager();
		myRandom = new Random(myRM.getRandomSeed(this));
		
		myId.setReplicaNumber(replicaNumber);
		
		this.replicate = 1 < myRM.getReplicationFactor();
		
		Serializable[] reInitParams = myManagementDeployParameters
				.getReInitParameters();
		
		if(mode != ManagementElementInterface.NEW) {
			
			if (reInitParams != null) {
				
				applicationParameters = (Serializable[]) reInitParams[0];
				mySystemSinks = (HashMap<String, Subscription>) reInitParams[1];
				myUserSinks = (ArrayList<Subscription>) reInitParams[2];
				
				/*#%*/ 	message += " re-initializing with " + myUserSinks.size() + " sinks";
								
				/*#%*/ //System.out.println(message);
				initInterface.reinit(applicationParameters);
				//This is a delicate question: we need to _start_ all replicas,
				//but only the primary one should be allowed to _act_

			
			} else {
				System.err.println("Reinit parameters must NOT be null on recreation!");
				myPrivateHost.log("Reinit parameters must NOT be null on recreation!");
			}
		}
		else {
			/*#%*/ message += " initializing.";
			//if(myId.getType() == )
			if(myId.getType() == NicheId.TYPE_SENSOR) {
				if (reInitParams != null) {
					
					applicationParameters = (Serializable[]) reInitParams[0];
					mySystemSinks = (HashMap<String, Subscription>) reInitParams[1];
					myUserSinks = (ArrayList<Subscription>) reInitParams[2];
					
				}
				/*#%*/ message += " initializing with " + myUserSinks.size() + " given sink(s)";
				((SensorInitInterface)initInterface).initComponentId((ComponentId)reInitParams[3]);
			} else if(myId.getType() == NicheId.TYPE_ACTUATOR) {
				if (reInitParams != null) {
					
					applicationParameters = (Serializable[]) reInitParams[0];
					mySystemSinks = (HashMap<String, Subscription>) reInitParams[1];
					//myUserSinks = (ArrayList<Subscription>) reInitParams[2];
					
					for(Subscription sub : (ArrayList<Subscription>) reInitParams[2]) {
//						System.err.println("Subscribing Effector " + sub.getEventName());
						fork.subscribe(sub.getSourceId(), myId, sub.getEventName());
					}
					
				}
				/*#%*/ message += " initializing with " + myUserSinks.size() + " given sink(s)";
				((ActuatorInitInterface)initInterface).initComponentId((ComponentId)reInitParams[3]);
			} 
			
			initInterface.init(applicationParameters);
		}


		initInterface.init(this);
		initInterface.initId(myId);
		
		
		/*#%*/ myPrivateHost.log(message);
		myManagementInterface.registerManagementElement(replicaNumber, mode, this);

	}

	public void eventHandler(Serializable event, int flag) {
		// just forward the event to the application side of the manager
		eventHandlerInterface.eventHandler(event, flag);
		
	}

	public void messageHandler(Message message) {
		if (message instanceof UpdateManagementElementMessage) {
			handleUpdateMEMessage((UpdateManagementElementMessage) message);
		}

	}

	private void handleUpdateMEMessage(UpdateManagementElementMessage m) {
		/*
		 * ok, this is/will be a bit confusing. the aggregators might get these
		 * messages from anyone, and they will be responsible for converting &
		 * possible sending them along to the watcher they are instructed to
		 * subscribe to.
		 * 
		 * it's different for watchers, since the SNRs do parts of this for them
		 */
		/*#%*/ String logMessage;
		
		if (m.getType() == UpdateManagementElementMessage.TYPE_ADD_SINK) {
			
			addSink((Subscription) m.getReference());
			
		} /*#%*/ else {
			/*#%*/ logMessage = "Error, the manager got an UpdateManagementElementMessage of type "
			/*#%*/ 	+ m.getType()
			/*#%*/ 	+ " which it doesn't handle"
			/*#%*/ 	;
			/*#%*/ System.err.println(logMessage);
			/*#%*/ myPrivateHost.log(logMessage);
		/*#%*/ }

	}

	public void addSink(Subscription subscription) {

		if (subscription.getEventName().equals(
				ComponentFailEvent.class.getName())) {

			DelegationRequestMessage dm;
			
			 SensorSubscription s;

			 NicheId destination = myRM.getSuccessorNodeContainerId(null);
				 //Remember, the source is the _real_ component that we are
				 //interested in, therefore source = thing.getId()
				 s = new SensorSubscription(
						 myId,
						 subscription.getSinkId(), //thing.getId() or subscription.getSinkId(), if doing the brokering style...
						 subscription.getSinkId(),
						 subscription.getEventName(),
						 myRM.getNodeRef()
				);
								
				 
				dm = new DelegationRequestMessage(				
					destination,
					DelegationRequestMessage.TYPE_SENSOR,
					new Serializable[] {s}
					);
	
				 
				 myPrivateHost.sendToManagement(destination, dm, (-1 < replicaNumber));								
	 
				/*#%*/ myPrivateHost.log(
				/*#%*/ 			"Manager-addSink "
				/*#%*/ 			+ myId 
				/*#%*/ 			+":"
				/*#%*/ 			+ replicaNumber
				/*#%*/ 			+ " says: I've added a new sink: "
				/*#%*/ 			+ subscription.getSinkId()
				/*#%*/ 			+ " is listening to "
				/*#%*/ 			+ subscription.getEventName()
				/*#%*/ 	);

			 } else {
				 myUserSinks.add(subscription);
				/*#%*/ myPrivateHost.log(
				/*#%*/ 		"ME-addSink "
				/*#%*/ 		+ myId 
				/*#%*/ 		+":"
				/*#%*/ 		+ replicaNumber
				/*#%*/ 		+ " says: I've added a new sink: "
				/*#%*/ 		+ subscription.getSinkId()
				/*#%*/ 		+ " is listening to "
				/*#%*/ 		+ subscription.getEventName()
				/*#%*/ );
			 }
		
//		else {
//				 
//				 String logMessage =
//							"Manager-addSink "
//							+ myId 
//							+":"
//							+ replicaNumber
//							+ " says: didn't understand "
//							+ subscription.getEventName()
//							+ " ERROR "
//					;
//
//					myPrivateHost.log(logMessage);
//					System.err.println(logMessage);
//
//			 }

		 
		
		// any more book-keeping??

	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.TriggerInterface#trigger(java.lang.Object)
	 */
	public void trigger(Serializable e) {
		
		//So, some events should just be passed through the pipe
		
		/*#%*/ String logMessage = "ManagementElement "
		/*#%*/ 					+ myId
		/*#%*/ 					+ ":"
		/*#%*/ 					+ replicaNumber
		/*#%*/ 					+ " is triggered. I have "
		/*#%*/ 					+ myUserSinks.size()
		/*#%*/ 					+ " potential sinks\n"; 
		

		
		String type = e.getClass().getName();
		// DeliverEventMessage message;
		NicheId destination;
		// System.out.println("Aggregator says: Check subscriptions");
		for (Subscription sink : myUserSinks) {

			if (sink.getEventName().equals(type)) {
				destination = sink.getSinkId();

				/*#%*/ logMessage += " informing subscriber "
				/*#%*/ 				+ destination
				/*#%*/ 				+ " about event "
				/*#%*/ 				+ type
				/*#%*/ 				+ "\n";

				myPrivateHost.sendToManagement(
						destination,
						new DeliverEventMessage(destination, e),
						destination.isReliable()
				);
			}
		}
		
		/*#%*/ myPrivateHost.log(logMessage);
	}
	
	public void trigger(Serializable e, Serializable tag) {
		
		//So, some events should just be passed through the pipe
		
		/*#%*/ String logMessage = "ManagementElement "
		/*#%*/ 					+ myId
		/*#%*/ 					+ ":"
		/*#%*/ 					+ replicaNumber
		/*#%*/ 					+ " is triggered. I have "
		/*#%*/ 					+ myUserSinks.size()
		/*#%*/ 					+ " potential sinks\n"; 
		

		
		String type = e.getClass().getName();
		// DeliverEventMessage message;
		NicheId destination;
		// System.out.println("Aggregator says: Check subscriptions");
		for (Subscription sink : myUserSinks) {

			if (sink.getEventName().equals(type) && tag.equals(sink.getTag())) {
				destination = sink.getSinkId();
				/*#%*/ logMessage += " informing subscriber matching TAG"
				/*#%*/ 				+ destination
				/*#%*/ 				+ " about event "
				/*#%*/ 				+ type
				/*#%*/ 				+ "\n";

				myPrivateHost.sendToManagement(
						destination,
						new DeliverEventMessage(destination, e),
						destination.isReliable()
				);
			}
		}
		
		/*#%*/ myPrivateHost.log(logMessage);
	}
	
	
	public void triggerAny(Serializable e) {
		
		//So, some events should just be passed through the pipe
		
		/*#%*/ String logMessage = "ManagementElement "
		/*#%*/ 					+ myId
		/*#%*/ 					+ ":"
		/*#%*/ 					+ replicaNumber
		/*#%*/ 					+ " is triggered. I have "
		/*#%*/ 					+ myUserSinks.size()
		/*#%*/ 					+ " potential sinks\n"; 
		

		
		String type = e.getClass().getName();
		// DeliverEventMessage message;
		NicheId destination;
		ArrayList<NicheId> destinations = new ArrayList<NicheId>();
		// System.out.println("Aggregator says: Check subscriptions");
		for (Subscription sink : myUserSinks) {
			if (sink.getEventName().equals(type)) {
				destinations.add(sink.getSinkId());
			}
		}
		
		destination = destinations.get(myRandom.nextInt(destinations.size()));
		
		/*#%*/ logMessage += " informing any subscriber "
			/*#%*/ 				+ destination
			/*#%*/ 				+ " out of " +destinations.size()+ " about event "
			/*#%*/ 				+ type
			/*#%*/ 				+ "\n";
		
		myPrivateHost.sendToManagement(
				destination,
				new DeliverEventMessage(destination, e),
				destination.isReliable()
		);
		
		/*#%*/ myPrivateHost.log(logMessage);
	}
	
	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.TriggerInterface#removeSink(dks.niche.ids.NicheId)
	 */
	@Override
	public void removeSink(String sinkId) {
		/*#%*/ myPrivateHost.log("removeSink called for "+sinkId+":");
		for (int i=0; i<myUserSinks.size(); i++) {
			Subscription sink = myUserSinks.get(i);
			if (sinkId.equals(sink.getSinkId().getLocation())) {
				myUserSinks.remove(i);
				/*#%*/ myPrivateHost.log("Removing Sink with id " + sink);
				i--;
			}
		}
	}
	
	
	/*
	 * Attribute controller stuff
	 * 
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setId(dks.niche.ids.NicheId)
	 */
	public NicheId getId() {
		return myId;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setId(dks.niche.ids.NicheId)
	 */
	public void setId(NicheId id) {
		//don't assign. copy/clone or else two replicas on the same node will share 
		//the same instance - which is bad...
		myId = new NicheId(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#getNicheManagementInterface()
	 */
	public NicheManagementInterface getNicheManagementInterface() {
		return myManagementInterface;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setNicheManagementInterface(dks.niche.interfaces.NicheManagementInterface)
	 */
	public void setNicheManagementInterface(NicheManagementInterface host) {
		myManagementInterface = host;
	}
	
//	public void setNicheActuatorInterface(NicheActuatorInterface niche) {
//		myContainedComponentActuatorInterface = niche;
//	}

	public void setManagementDeployParameters(ManagementDeployParameters param) {
		this.myManagementDeployParameters = param;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#getInitialParameters()
	 */
	public Serializable[] getInitialParameters() {
		System.out.println("Error, not implemented");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setInitialParameters(java.lang.Serializable[])
	 */
	public void setInitialParameters(Serializable[] parameters) {
		applicationParameters = parameters;

	}

	public void setReplicaNumber(int replicaNumber) {
		this.replicaNumber = replicaNumber;

	}
	
	public int getReplicaNumber() {
		return this.replicaNumber;
	}

	public void setStartupMode(int mode) {
		this.mode = mode;
	}
	
	
	/*
	 * Watcher Stuff: OPTIONAL!
	 * 
	 */

	public String getSensorClassName() {
		return sensorClassName;
	}

	public void setSensorClassName(String sensorClassName) {
		this.sensorClassName = sensorClassName;
	}

	public String getSensorEventClassName() {
		return sensorEventClassName;
	}

	public void setSensorEventClassName(String sensorEventClassName) {
		this.sensorEventClassName = sensorEventClassName;
	}

	public Serializable[] getSensorParameters() {
		return sensorParameters;
	}

	public void setSensorParameters(Serializable[] sensorParameters) {
		this.sensorParameters = sensorParameters;
	}

	public NicheId getWatchedComponentId() {
		return watchedComponentId;
	}

	public void setWatchedComponentId(NicheId watchedComponentId) {
		this.watchedComponentId = watchedComponentId;
		
	}

	
	/*
	 * Executor Stuff: OPTIONAL!
	 * 
	 */
	
	
	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#getActuatedComponentId()
	 */
	@Override
	public NicheId getActuatedComponentId() {
		return actuatedComponentId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#getActuatorClassName()
	 */
	@Override
	public String getActuatorClassName() {
		return actuatorClassName;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#getActuatorEventClassName()
	 */
	@Override
	public String getActuatorEventClassName() {
		return actuatorEventClassName;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#getActuatorParameters()
	 */
	@Override
	public Serializable[] getActuatorParameters() {
		return actuatorParameters;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setActuatedComponentId(dks.niche.ids.NicheId)
	 */
	@Override
	public void setActuatedComponentId(NicheId actuatedComponentId) {
		this.actuatedComponentId=actuatedComponentId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setActuatorClassName(java.lang.String)
	 */
	@Override
	public void setActuatorClassName(String actuatorClassName) {
		this.actuatorClassName = actuatorClassName;
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setActuatorEventClassName(java.lang.String)
	 */
	@Override
	public void setActuatorEventClassName(String actuatorEventClassName) {
		this.actuatorEventClassName = actuatorEventClassName;
	}

	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.ManagementElementAttributeController#setActuatorParameters(java.lang.Serializable[])
	 */
	@Override
	public void setActuatorParameters(Serializable[] actuatorParameters) {
		this.actuatorParameters = actuatorParameters;
	}
	
	
	
	
	
	
	////////DeploySensorInterface
	public void deploySensor(String sensorClassName, String sensorEventClassName, Serializable[] sensorParameters, String[] clientInterfaces, String[] serverInterfaces) {
		// we assume that the sensor class name is the same as the adl file name

		if(replicaNumber < 1) {
			/*#%*/  myPrivateHost.log("The ME-proxy is requesting the deployment of its fractal sensors now");

		this.sensorClassName = sensorClassName;
		this.sensorEventClassName = sensorEventClassName;
		this.sensorParameters = sensorParameters;

		//User given parameters in pos tmp[5], for
		//the system side to track...
		
		Serializable[] tmp = new Serializable[] {
				watchedComponentId,
				sensorClassName,
				sensorEventClassName,
				true, //sensorColocation
				clientInterfaces,
				serverInterfaces,
				sensorParameters,
				myId,
				true
		};
		
		myPrivateHost.sendToManagement(watchedComponentId, new UpdateSNRRequestMessage(watchedComponentId, UpdateSNRRequestMessage.TYPE_ADD_WATCHER, myId, sensorClassName, tmp), watchedComponentId.getType() == NicheId.TYPE_GROUP_ID ? true : false);
		
		} /*#%*/ else {
		/*#%*/ 	myPrivateHost.log("The ME-proxy would have been requesting the deployment of its fractal sensors now, hadn't it been a replica");
		/*#%*/ }
		
		
	}

	
////////DeployActuatorInterface
	/* (non-Javadoc)
	 * @see dks.niche.fractal.interfaces.DeployActuatorInterface#deployActuator(java.lang.String, java.lang.String, java.lang.Serializable[], java.lang.String[], java.lang.String[])
	 */
	@Override
	public void deployActuator(String actuatorClassName,
			String actuatorEventClassName, Serializable[] actuatorParameters,
			String[] clientInterfaces, String[] serverInterfaces) {
		// we assume that the actuator class name is the same as the adl file name

		if(replicaNumber < 1) {
			/*#%*/  myPrivateHost.log("The ME-proxy is requesting the deployment of its fractal actuators now");

		this.actuatorClassName = actuatorClassName;
		this.actuatorEventClassName = actuatorEventClassName;
		this.actuatorParameters = actuatorParameters;

		//User given parameters in pos tmp[5], for
		//the system side to track...
		
		Serializable[] tmp = new Serializable[] {
				actuatedComponentId,
				actuatorClassName,
				actuatorEventClassName,
				true, //actuatorColocation
				clientInterfaces,
				serverInterfaces,
				actuatorParameters,
				myId,
				true
		};
		
		myPrivateHost.sendToManagement(actuatedComponentId, new UpdateSNRRequestMessage(actuatedComponentId, UpdateSNRRequestMessage.TYPE_ADD_EXECUTOR, myId, actuatorClassName, tmp), actuatedComponentId.getType() == NicheId.TYPE_GROUP_ID ? true : false);
		
		} /*#%*/ else {
		/*#%*/ 	myPrivateHost.log("The ME-proxy would have been requesting the deployment of its fractal actuators now, hadn't it been a replica");
		/*#%*/ }
		
	}

	
	
	/*
	 * 
	 * Sensor stuff, OPTIONAL! 
	 * 
	 * Sensor stuff, OPTIONAL!
	 * 
	 */ 
	
	public ComponentId getComponentId() {
		return myComponentId;
	}

	public void setComponentId(ComponentId id) {
		myComponentId = id;
	}

	
	
	/*
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 * Fork-stuff!
	 * 
	 */
	
	
	
	public NodeRef oneShotDiscoverResource(Serializable description) throws OperationTimedOutException {
		
		if(replicaNumber < 1) {
			return fork.oneShotDiscoverResource(description);
		}
		return null;
	}
	

	public ArrayList discover(Serializable description) throws OperationTimedOutException {
		
		if(replicaNumber < 1) {
			return fork.discover(description);
		}
		return null;
	}

	// ALLOCATE

	// Public

	public ArrayList allocate(Serializable resources, Object description) throws OperationTimedOutException {

		if(replicaNumber < 1) {
			return fork.allocate(resources, description);
		}
		return null;
	}

	public void deallocate(ResourceId resource) {
		if(replicaNumber < 1) {
			fork.deallocate(resource);
		}
	}


	public ArrayList deploy(Serializable destinations, Serializable descriptions) throws OperationTimedOutException {

		if(replicaNumber < 1) {
			return fork.deploy(destinations, descriptions);
		}
		return null;
		
	}

	public NicheId deployManagementElement(ManagementDeployParameters description,
			IdentifierInterface destination) throws OperationTimedOutException {
	
		if(replicaNumber < 1) {
			return fork.deployManagementElement(description, destination);
		}
		return null;
	
	}
	
	
	public void redeployManagementElement(ManagementDeployParameters description,
			IdentifierInterface oldId) {
		
		if(replicaNumber < 1) {
			fork.redeployManagementElement(description, oldId);
		}

	}

	
	
	
	//This is the local bind op offered to ordinary components
	public void bind(String clientInterface, Object server,
			String serverInterface, int type) {
		//TODO, fixme
		System.err.println("NOT IMPLEMENTED, ERROR");
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.NicheActuatorInterface#bind(java.lang.Object,
	 *      java.lang.String, java.lang.Object, java.lang.String)
	 */
//	public BindId bind(Object client, String clientInterface, Object server,
//			String serverInterface) {
//		
//		if(replicaNumber < 1) {
//			return fork.bind(client, clientInterface, server, serverInterface);
//		} 
//		return null;
//		
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.NicheActuatorInterface#bind(java.lang.Object,
	 *      java.lang.String, java.lang.Object, java.lang.String, int)
	 */
	public BindId bind(Object sender, String senderInterface, Object receiver,
			String receiverInterface, int type) {

		if(replicaNumber < 1) {
			return fork.bind(sender, senderInterface, receiver, receiverInterface, type);
		}
		return null;
		
	}

	public void unbind(IdentifierInterface binding) {
		// TODO: test
		fork.unbind(binding);
	}

	
	// Upcall! Not for the management elements to call, only for the
	// infrastructure

	public void notify(int operationId, Object result) {

		/*#%*/ myPrivateHost.log("ME-Fork says: notify is trying to enter critical section: "
		/*#%*/ 	+ operationId);
		synchronized (synchronizedObjects[operationId]) {
			/*#%*/ myPrivateHost.log("ME-Fork says: notify is entering critical section: "
			/*#%*/ 		+ operationId + " mySyncObj: "
			/*#%*/ 		+ synchronizedObjects[operationId]);
			waitForResults[operationId] = result;
			synchronizedObjects[operationId].notify();
			/*#%*/ myPrivateHost.log("ME-Fork says: notify is exiting critical section: "
			/*#%*/ 		+ operationId);

		}
	}

	// Subscribe

	public Subscription subscribe(IdentifierInterface source,
			IdentifierInterface sink, String eventName) {
		return subscribe(source, sink, eventName, null);
	}
	public Subscription subscribe(IdentifierInterface source,
			IdentifierInterface sink, String eventName, Serializable tag) {
		
		
		if(replicaNumber < 1) {
			
			/*#%*/ myPrivateHost.log(myId +" subscribes " + source.getId() + " on behalf of " + sink.getClass());
			
			return fork.subscribe(source, sink, eventName, tag);
			
		}
		/*#%*/ else {
		/*#%*/	myPrivateHost.log("Replica " + myId +" does not subscribe");
		/*#%*/ }
		
		return null;
		
	}

	public Subscription subscribe(IdentifierInterface source, String sinkName,
			String eventName, IdentifierInterface sinkLocation) {

		if(replicaNumber < 1) {
			return fork.subscribe(source, sinkName, eventName, sinkLocation);
		}
		return null;
	}

	public boolean unsubscribe(Subscription subscription) {
		if(replicaNumber < 1) {
			return fork.unsubscribe(subscription);
		} 
		return false;
	}

	// Sending things

	// Sending things - Management

//	public void sendToManagement(NicheId destination, Message message) {
//		myPrivateHost.sendToManagement(destination, message);
//
//	}

	//
	// public void sendToManagement(DKSRef destination, Message message) {
	// myPrivateHost.sendToManagement(destination, message);
	//	
	// }
	//
	// public void sendToNode(DKSRef destination, Message message) {
	// myPrivateHost.sendToNode(destination, message);
	//
	// }
	//
	// public Object requestFromManagement(NicheId destination, Message
	// requestMessage) {
	// waitForResults = waitForSynchronousReturnValue;
	// myPrivateHost.requestFromManagement(destination, requestMessage, this);
	// myWait();
	// System.out.println("Done with request!");
	// return waitForResults;
	//
	// }

	// Sending things - Components

//	public void send(Object localBindId, Object message) {
//		myPrivateHost.send(localBindId, message);
//	}
//
//	public void send(Object localBindId, Object message, ComponentId destination) {
//		myPrivateHost.send(localBindId, message, destination);
//	}

	// public void registerBindNotifyHandler(Object receiverId, Object
	// handlerObject) {
	// //FIXME
	// }
	// GETTERS

	// ID-GETTERS

//	public NicheId getUniqueCollocatedId(NicheId id) {
//		return myPrivateHost.getUniqueCollocatedId(id);
//	}
//
//	public NicheId getUniqueId() {
//		return myPrivateHost.getUniqueId();
//	}
//
//	public NicheId getCloseNode(DKSRef nodeOfRef) {
//		return myPrivateHost.getCloseNode(nodeOfRef);
//	}

	// OTHER GETTERS

	public SimpleResourceManager getResourceManager() {

		return myRM;
	}

	
	public ComponentType getComponentType(String adlName) {
		return myRM.getComponentType(adlName);
	}
	// TIMERS

	public long registerTimer(EventHandlerInterface me, Class name, int period) {
		return myPrivateHost.registerTimer(me, name, period);

	}

	public void cancelTimer(long timerId) {
		myPrivateHost.cancelTimer(timerId);

	}

	// GROUPING

	public GroupId createGroup(ArrayList items) {
	
	return createGroup(new String(), items);
}
	
	public GroupId createGroup(String templateName, ArrayList items) {
	
		return createGroup(myRM.getSNRTemplate(templateName), items);
	}
	
	public GroupId createGroup(SNR template, ArrayList items) {
		
		if(replicaNumber < 1) {
			return createGroup(template, items);
		}
		return null;
		
		
	}

	public void removeGroup(GroupId gid) {
		update(gid, null, NicheComponentSupportInterface.REMOVE_GROUP);
	}

	public GroupId getGroupTemplate() {
		return new GroupId();
	}
	
	public void addToGroup(Object itemToAdd, Object groupId) {
		update(groupId, itemToAdd, NicheComponentSupportInterface.ADD_TO_GROUP);
	}

	public boolean registerGroupTemplate(String templateName, SNR template) {
		return myRM.registerSNRTemplate(templateName, template);
	}

	public void removeFromGroup(Object itemToRemove, Object groupId) {
		update(groupId, itemToRemove,
				NicheComponentSupportInterface.REMOVE_FROM_GROUP);
	}

	public void update(Object objectToBeUpdated, Object argument, int type) {
		
		fork.update(objectToBeUpdated, argument, type);
				
	}

	public Object query(IdentifierInterface queryObject, int queryType) {
		
		return fork.query(queryObject, queryType);
				
	}
	
	public ArrayList<SNRElement> getCurrentMembers(SNRElement snrName) {
		return null;
	}
	public synchronized Object sendWithReply(Object localBindId, Serializable invocation) throws DestinationUnreachableException {
		
		if(replicaNumber < 1) {
			return fork.sendWithReply(localBindId, invocation);
		}
		return null;
		
	}
	public synchronized Object sendOnBinding(Object localBindId, Invocation invocation, ComponentId shortcut) throws OperationTimedOutException, DestinationUnreachableException {
		
		if(replicaNumber < 1) {
			return fork.sendOnBinding(localBindId, invocation, shortcut);
		}
		return null;
		
	}

//
	public NicheAsynchronousInterface testingOnly() {
		return myPrivateHost;
	}
	public LoggerInterface getLogger() {
		return myPrivateHost;
	}

//		protected void prepareWait() {
//		operationId = (operationId + 1) % MAX_CONCURRENT_OPERATIONS;
//		waitForResults[operationId] = waitForSynchronousReturnValue;
//		synchronizedObjects[operationId] = new Object();
//	}

	
	

	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */
	/*
	 * Fractal stuff!
	 */

	public String[] listFc() {
		// Client interfaces
		return new String[] { FractalInterfaceNames.COMPONENT,
				FractalInterfaceNames.INIT_CLIENT_INTERFACE,
				FractalInterfaceNames.MOVABLE_CLIENT_INTERFACE,
				FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE,
				FractalInterfaceNames.CONTROLLER_CLIENT_INTERFACE,
				"restoreReplica"
			};
	}

	public Object lookupFc(final String interfaceName)
			throws NoSuchInterfaceException {
		
		//System.out.println("Got lookup for " + interfaceName);
		if (interfaceName.equals(FractalInterfaceNames.INIT_CLIENT_INTERFACE))
			return initInterface;
		else if (interfaceName
				.equals(FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE))
			return eventHandlerInterface;
		else if (interfaceName
				.equals(FractalInterfaceNames.MOVABLE_CLIENT_INTERFACE))
			return movableInterface;
		else if (interfaceName.equals(FractalInterfaceNames.CONTROLLER_CLIENT_INTERFACE))
			return hostedComponent;
		
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT))
			return mySelf;
		else if (hostedComponent != null) {
			//System.out.println("Will be handled by my hosted component");
			return hostedComponent.lookupFc(interfaceName);
		}
		//System.out.println("FAIL");
		throw new NoSuchInterfaceException(interfaceName);
		
	}

	public void bindFc(String interfaceName, Object stub)
			throws NoSuchInterfaceException, IllegalBindingException,
			IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.INIT_CLIENT_INTERFACE))
			initInterface = (InitInterface) stub;
		else if (interfaceName
				.equals(FractalInterfaceNames.MOVABLE_CLIENT_INTERFACE))
			movableInterface = (MovableInterface) stub;

		else if (interfaceName
				.equals(FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE))
			eventHandlerInterface = (EventHandlerInterface) stub;
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
			mySelf = (Component) stub;
		} else if(interfaceName.equals(FractalInterfaceNames.CONTROLLER_CLIENT_INTERFACE)) {
			hostedComponent = (BindingController) stub;
		} else if (hostedComponent != null) {
			hostedComponent.bindFc(interfaceName, stub);
		}
		else {
			throw new NoSuchInterfaceException(interfaceName);
		}

	}

	public void unbindFc(String interfaceName) throws NoSuchInterfaceException,
			IllegalBindingException, IllegalLifeCycleException {
		if (interfaceName.equals(FractalInterfaceNames.INIT_CLIENT_INTERFACE))
			initInterface = null;
		else if (interfaceName
				.equals(FractalInterfaceNames.MOVABLE_CLIENT_INTERFACE))
			movableInterface = null;
		else if (interfaceName
				.equals(FractalInterfaceNames.EVENT_HANDLER_CLIENT_INTERFACE))
			eventHandlerInterface = null;
		else if (interfaceName.equals(FractalInterfaceNames.CONTROLLER_CLIENT_INTERFACE)) {
			hostedComponent = null;
		}
		else if (interfaceName.equals(FractalInterfaceNames.COMPONENT)) {
				mySelf = null;
		} else if (hostedComponent != null) {
			hostedComponent.unbindFc(interfaceName);
		}
		else {		
			throw new NoSuchInterfaceException(interfaceName);
		}

	}

	public String getFcState() {
		return status ? "STARTED" : "STOPPED";
	}

	public void startFc() throws IllegalLifeCycleException {

		// starting the fractal component
		status = true;
		connect();
		/*#%*/ myPrivateHost.log("Manager-proxy started with id: " + myId + ":"+replicaNumber);

	}

	public void stopFc() throws IllegalLifeCycleException {
		status = false;

	}

	public DelegationRequestMessage transfer(int mode) {
		
		//Always grab the state, also from replicas
		// - assuming they're also maintained
		// in the proper state...
		if(movableInterface == null) {
			/*#%*/ myPrivateHost.log("Manager-proxy with id " + myId + " cannot move!");
			return null;
		} 
		applicationParameters = movableInterface.getAttributes();
		myManagementDeployParameters.setReInitParameters(
				new Serializable[] {
						applicationParameters,
						mySystemSinks,
						myUserSinks
				}
		);
		return new DelegationRequestMessage(myId,
				DelegationRequestMessage.TYPE_FRACTAL_MANAGER,
				myManagementDeployParameters);
	}


	public boolean isReliable() {
		return 0 <= replicaNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#init(java.lang.Serializable[])
	 */
	public void init(Serializable[] applicationParameters) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.IdentifierInterface#getDKSRef()
	 */
	public DKSRef getDKSRef() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#connect(dks.niche.ids.NicheId, int, dks.niche.interfaces.NicheManagementInterface)
	 */
	
	public void connect(NicheId id, int replicaNumber,
			NicheManagementInterface myHost,
			NicheNotifyInterface callBack) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#reinit(java.lang.Serializable[])
	 */
	
	public void reinit(Serializable[] parameters) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheActuatorInterface#testingOnly()
	 */
//	@Override
//	public NicheAsynchronousInterface testingOnly() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheComponentSupportInterface#setOwner(dks.niche.interfaces.IdentifierInterface)
	 */
//	@Override
//	public void setOwner(IdentifierInterface owner) {
//		// TODO Auto-generated method stub
//		
//	}


	
	
}
