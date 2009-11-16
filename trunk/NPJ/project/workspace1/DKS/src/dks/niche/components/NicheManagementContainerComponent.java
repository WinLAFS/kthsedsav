/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.components;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;

import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.Scheduler;
import dks.bcast.IntervalBroadcastInfo;
import dks.bcast.events.PseudoReliableIntervalBroadcastStartEvent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.fd.events.SuspectEvent;
import dks.messages.Message;
import dks.niche.events.CreateGroupEvent;
import dks.niche.events.ComponentFailEvent;
import dks.niche.events.ResourceLeaveEvent;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.hiddenEvents.DeliverToManagementEvent;
import dks.niche.hiddenEvents.DeliverToNodeEvent;
import dks.niche.hiddenEvents.ReplyFromManagementEvent;
import dks.niche.hiddenEvents.SendRequestEvent;
import dks.niche.ids.NicheId;
import dks.niche.ids.SNRElement;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.ReliableInterface;
import dks.niche.interfaces.ReplicableMessageInterface;
import dks.niche.interfaces.SensorInterface;
import dks.niche.messages.BindRequestMessage;
import dks.niche.messages.DelegateSubscriptionMessage;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.DeliverEventMessage;
import dks.niche.messages.GetReferenceMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.messages.UpdateSNRRequestMessage;
import dks.niche.messages.UpdateSensorMessage;
import dks.niche.sensors.CreateGroupSensor;
import dks.niche.sensors.SystemSensor;
import dks.niche.sensors.ResourceFailSensor;
import dks.niche.sensors.ResourceLeaveSensor;
import dks.niche.wrappers.DeployMEClass;
import dks.niche.wrappers.ManagementDeployParameters;
import dks.niche.wrappers.SensorSubscription;
import dks.niche.wrappers.SimpleResourceManager;
import dks.niche.wrappers.Subscription;
import dks.ring.events.PredecessorChanged;
import dks.timer.TimerComponent;
import dks.utils.IntervalsList;
import dks.utils.ThreadStatusChecker;

/**
 * The <code>NicheManagementContainerComponent</code> class
 * 
 * @author Joel
 * @version $Id: NicheManagementContainerComponent.java 294 2006-05-05 17:14:14Z
 *          joel $
 */
public class NicheManagementContainerComponent extends Component {

	SystemSensor systemSensor;

	// SensorInterface failSensor;
	// SensorInterface leaveSensor;
	// SensorInterface groupSensor;

	// HashMap<String, HashMap<String, SensorInterface>> sensors;

	// no arraylist, since messages to MEs are sent directly to _one_ ME. might
	// be optimized / expanded
	// HashMap<String, ManagementElementInterface> managementElements;

	// any timout receiver should go in the timout registry!!
	HashMap<String, EventHandlerInterface> myTimeoutReceivers;

	// any location should go in the registry!!
	// HashMap<String, Boolean> myLocations;

	HashMap<String, ArrayList> outstandingRequests;

	HashMap<String, ArrayList<DeliverToManagementEvent>> outstandingGetRequests;

	// HashMap<String, ArrayList> outstandingSNRRequests;

	DKSRef myRef;

	DKSRef[] currentNodes; // Only for testing

	// private Object delegateHandler; private String delegateMethodName;

	private NicheManagementInterface myNicheManagementInterface;

	ExecutorService myThreadPool;

	// private NicheOverlayServiceComponent overlayServices;
	private SimpleResourceManager myRM;

	private TimerComponent timerComponent;

	/*#%*/ private static Logger log = Logger.getLogger(NicheManagementContainerComponent.class);

	private Random myRandom;
	
	static final boolean THREAD_STATUS =
		System.getProperty("dks.test.threadStatus") instanceof String ?
				0 < Integer.parseInt(System.getProperty("dks.test.threadStatus")) ?
						true
					:
						false
			:
				
				false
	;

	
	public NicheManagementContainerComponent(NicheManagementInterface niche,
			Scheduler scheduler, ComponentRegistry registry,
			ExecutorService threadPool, DKSRef myRef) {

		super(scheduler, registry);
		this.myNicheManagementInterface = niche;
		this.myThreadPool = threadPool;
		this.myRef = myRef;

		myRM = myNicheManagementInterface.getResourceManager();

		// Get the TimerComponent
		this.timerComponent = registry.getTimerComponent();
		// System.out.println("NicheManagementContainerComponent says: my timerC
		// is "+timerComponent);

		// localEvents = new HashMap(); //<int, Class>
		// SNRs = new HashMap();

//		infrastructureSensors = new HashMap<String, SensorInterface>();
//		infrastructureSensors.put(ComponentFailEvent.class.getName(), new ResourceFailSensor(myRef, niche));
//		infrastructureSensors.put(SuspectEvent.class.getName(), new ResourceFailSensor(myRef, niche));
//		infrastructureSensors.put(ResourceLeaveEvent.class.getName(), new ResourceLeaveSensor(myRef, niche));
//		infrastructureSensors.put(CreateGroupEvent.class.getName(), new CreateGroupSensor(myRef, niche));
	
		systemSensor = new SystemSensor(myRef, niche);
		
		// sensors.put(SensorInterface.NO_EVENT_SENSOR, new HashMap());

		// managementElements = new HashMap();
		// myRegistry = new HashMap();

		outstandingRequests = new HashMap<String, ArrayList>();
		outstandingGetRequests = new HashMap<String, ArrayList<DeliverToManagementEvent>>();

		myTimeoutReceivers = new HashMap<String, EventHandlerInterface>();

		myRandom = new Random(myRM.getRandomSeed(this));
		
		registerForEvents();
		registerConsumers();
		
		if(THREAD_STATUS) {
			execute(
				new ThreadStatusChecker(
						niche
				)
			);
		}

	}

	public void registerForMessages() {
		// myNicheManagementInterface.registerReceiver(this,
		// "genericMessageHandler");
		// myNicheManagementInterface.registerMEReceiver(this,
		// "genericMessageHandler");
		// myNicheManagementInterface.registerManagementEventReceiver(this,
		// "specificEventHandler");
		// myNicheManagementInterface.registerRequestReceiver(this,
		// "requestReceiver");
	}

	/**
	 * 
	 */
	private void registerConsumers() {
		registerConsumer("receiveManagementElement",
				DelegationRequestMessage.class);
		// registerConsumer("instantiateSNR",
		// InstantiateSNRRequestMessage.class);

		// System.out.println("NMC says: Started"); //ready to start hosting
		// groups, watchers, aggregators, managers, you name it!");

	}

	protected void registerForEvents() {
		register(DeliverToManagementEvent.class, "genericMessageHandler");
		//register(RequestFromManagementEvent.class, "requestReceiver");
		register(PredecessorChanged.class, "handlePredecessorChanged");
		register(SuspectEvent.class, "handleSuspectEvent");
		registerGeneric(CreateGroupEvent.class, "genericEventHandler");
		registerGeneric(ResourceLeaveEvent.class, "genericEventHandler");

	}

	private synchronized void deliverEvent(DeliverEventMessage m) {

		NicheId destinationNicheId = m.getDestination();
		ManagementElementInterface receiver = myRM.getManagementElement(
														m.getDestinationRingId(),
														destinationNicheId
													);
		if (receiver != null) {
			/*#%*/ log.debug("Outside dedicated thread: Delivering event to "
			/*#%*/ 		+ receiver.getClass().getSimpleName()
			/*#%*/ 		+ " "
			/*#%*/ 		+ m.getDestinationRingId()
			/*#%*/ 		+ " "
			/*#%*/ 		+ destinationNicheId.toString());
			
			EventDeliverClass c = new EventDeliverClass(
										receiver,
										m,
										ManagementElementInterface.NEW
			);
			//myThreadPool.
			execute(c);
		} else {
			// System.err.println("Management says: No receiver present yet!");
			
			
			/*#%*/ log.debug("No receiver with id "
			/*#%*/ 		+ m.getDestinationRingId()
			/*#%*/ 		+ " "
			/*#%*/ 		+ destinationNicheId.getId()
			/*#%*/ 		+ " present yet");

			addOutstandingDeliverRequest(destinationNicheId, m.getReplicaNumber(), m);
			

		}

	}

	/*
	 * Below are methods to deal with
	 * 
	 * Installing management elements!
	 * 
	 * The receiver side, the site where the watched event is detected (for
	 * instance, the same as the leaving node, or a neighbour, in case of
	 * failures)
	 * 
	 */

	private synchronized void addSensorSubscriber(DelegationRequestMessage m) {

		// System.out.println("NicheEvenHandlerComponent-handleSensorInstallation
		// says: Sensor installation request received");
		Object param = m.getParameters()[0];

		/*#%*/ String logMessage = "Sensor subscriptions received ";
	
		String eventName;
				
		if(param instanceof SensorSubscription) {
	
			SensorSubscription ss = (SensorSubscription)param;
			
			 eventName = ss.getEventName();
			
			/*#%*/ logMessage +=
			/*#%*/ 		"\nregister sensor says: Adding subscriber "
			/*#%*/ 		+ ss.getSinkId()
			/*#%*/ 		+ " for "
			/*#%*/ 		+ eventName
			/*#%*/ ;
	
			systemSensor.addSink(ss);
				
				//TODO: check if we want to add more sensor types to the list.
				if(eventName.equals(ComponentFailEvent.class.getName()) || eventName.equals(CreateGroupEvent.class.getName())) {
					myRM.addSensorSubscription(m.getReplicaNumber(), m.getDestination(), m);
				}
		}
		else { //instanceof Arraylist
			
			ArrayList<SensorSubscription> sl = (ArrayList<SensorSubscription>)param;
			for (SensorSubscription sensorSubscription : sl) {
				
				eventName = sensorSubscription.getEventName();
				
				/*#%*/ logMessage +=
				/*#%*/ 		"\nregister sensor says: Adding subscriber "
				/*#%*/ 		+ sensorSubscription.getSinkId()
				/*#%*/ 		+ " for "
				/*#%*/ 		+ eventName
				/*#%*/ ;
		
				systemSensor.addSink(sensorSubscription);
					
				//TODO: check if we want to add more sensor types to the list.
				if(eventName.equals(ComponentFailEvent.class.getName()) || eventName.equals(CreateGroupEvent.class.getName())) {
					myRM.addSensorSubscription(
							m.getReplicaNumber(),
							m.getDestination(),
							new DelegationRequestMessage(
									m.getDestination(),
									DelegationRequestMessage.TYPE_SENSOR,
									sensorSubscription
								)
					);
				}
			}
		}
					
		/*#%*/ log.debug(logMessage);
		return;

	
	}

	public synchronized void registerManagementElement(int replicaNumber, int mode,
			NicheId key, ManagementElementInterface mei) {

		//myRM.addManagementElement(replicaNumber, key, mei);
		myRM.addManagementElement(replicaNumber, key, mei);
		// myRM.addLocation(key);
		/*#%*/ log.debug("Register ME says: " + key + ":"+ replicaNumber + " registered");
		// has been
		// regged");
		// check outstanding requests:
		
		

		ArrayList<Message> tempList = getOutstandingDeliverRequests(key, replicaNumber);

		if(tempList != null) {
			
			Runnable nextJob;
			for (Message m : tempList) {

				// System.out.println("Processing stored request for id
				// "+elementId.toString());
				/*#%*/ log.debug("Request of type " + m.getClass().getSimpleName() + " for " + key + ":"+ replicaNumber
				/*#%*/ 		+ " ready to be answer from queue");
				
				nextJob = m instanceof DeliverEventMessage ? new EventDeliverClass(mei, (DeliverEventMessage)m, mode) : new MessageDeliverClass(mei, m); 
				// System.out.println("NicheManagementContainerComponent says:
				// the receiver of the stored request of type "
				// +m.getClass().getSimpleName());
				//myThreadPool.
				execute(nextJob);

			}
		} /*#%*/ else {
		/*#%*/ 	log.debug("No messages waiting for ME " + key);
		/*#%*/ }
		
		ArrayList<DeliverToManagementEvent> tempAL = getOutstandingGetRequests(key, replicaNumber);
		
		if(tempAL != null) {
			
			GetReferenceMessage grm;

			for (DeliverToManagementEvent e : tempAL) {

				grm = (GetReferenceMessage) e.getMessage();
				/*#%*/ log.debug("GetReference request for " + key + ":"+ replicaNumber
				/*#%*/ 		+ " ready to be answer from queue");

				trigger(
						new ReplyFromManagementEvent(
								e,
								((SNRElement) mei).getAll()
						)
				);

			}
		}

	}

	/*
	 * 
	 * 
	 * private void registerWatcher(CommonWatcherInterface wi) {
	 * 
	 * System.out.println("NicheEvenHandlerComponent-handleSensorInstallation
	 * says: Watcher installation request received");
	 * 
	 * managementElements.put(wi.get, value) //TODO //Continue!! //Start
	 * watching! //below, purely for testing: //TEST }
	 * 
	 * private void registerAggregator(CommonWatcherInterface wi) {
	 * 
	 * System.out.println("NicheEvenHandlerComponent-handleSensorInstallation
	 * says: Aggregator installation request received"); //TODO //Continue!!
	 * Start aggregating! //below, purely for testing: //TEST }
	 * 
	 * 
	 */

	private void registerBulk(NicheId id,
			ArrayList<DelegationRequestMessage> bulk) {

		/*#%*/ log.debug("Register bulk request received");
		// TODO
		// Continue!!
		// Start bulking!
		for (DelegationRequestMessage message : bulk) {
			// System.out.println("NicheManagementContainerComponent-registerBulk
			// says: processing!");
			handleManagementElement(message);
		}
		// System.out.println("NicheManagementContainerComponent-registerBulk
		// says: done!");
		// below, purely for testing:
		// TEST
	}


	private void updateME(ReplicableMessageInterface m) {

		// If a request comes here, it means we already know this is the correct
		// location,
		// so, if the ME is not there, it means it just has not arrived yet. the
		// update must
		// be queued, waiting for the ME
		NicheId destinationNicheId = m.getDestination();
		ManagementElementInterface temp = myRM.getManagementElement(
													m.getDestinationRingId(),
													destinationNicheId
												);

		//FIXME - change to ring-id!
		if (temp == null) {
			/*#%*/ log.debug("Storing update message of type "
			/*#%*/ 		+ m.getClass().getSimpleName()
			/*#%*/ 		+ " to "
			/*#%*/ 		+ destinationNicheId
			/*#%*/ 		+ ":"
			/*#%*/ 		+ m.getReplicaNumber()
			/*#%*/ 		);
			
			addOutstandingDeliverRequest(destinationNicheId, m.getReplicaNumber(), m);
			
		} else {
			/*#%*/ log.debug("Delivering update message of type "
			/*#%*/ 		+ m.getClass().getSimpleName()
			/*#%*/ 		+ " to "
			/*#%*/ 		+ destinationNicheId
			/*#%*/ 		+ ":"
			/*#%*/ 		+ m.getReplicaNumber()
			/*#%*/ 		);
			// System.out.println("NicheManagementContainerComponent says: the
			// receiver of the UpdateMERequest is "+ temp.getId()+ " The
			// request-type is "+m.getType());
			//myThreadPool.
			execute(new MessageDeliverClass(temp, (Message)m));

		}

	}
	
	private void updateSensor(UpdateSensorMessage usm) {
		
	}

	/*
	 * Below are methods to deal with
	 * 
	 * Delegation of management
	 * 
	 * 2: The receiver side, the delegatee:
	 * 
	 */

	private void handleManagementElement(DelegationRequestMessage m) {

		/*#%*/ log.debug("Start handling delegationRequest with id "
		/*#%*/ 		+ m.getDestination());
		/*
		 * The flow:
		 * 
		 * Instanciate the class with empty constructor, call the setters for
		 * each of the parameters given
		 * 
		 */
		// this might be redone; now the instantiation must be done on receiver
		// side, at least until we master true migration
		int managementElementType = m.getType();
		NicheId id = m.getDestination();

		if (managementElementType == DelegationRequestMessage.TYPE_BULK) {
			registerBulk(id, m.getBulk());
			return;
		}

		// int replicaNumber = myRM.belongsToMeSymmetrically(id);

		if (managementElementType == DelegationRequestMessage.TYPE_FRACTAL_MANAGER) {
			// m.setReplicaNumber(replicaNumber);
			registerFractalManager(id, m);
			return;
		}

		// TODO
		// Register the exact id!
		// Har skulle man kunna lagga till ett mellansteg, dar man plockar fram
		// ratt sub-component, om sa onskas

		if(managementElementType == DelegationRequestMessage.TYPE_SENSOR) {
			
			addSensorSubscriber(m);
			
		} else {
			
			String className = m.getClassName();
			Serializable[] parameters = m.getParameters();
			//Object[] applicationParameters = m.getApplicationParameters();
	
			// System.out.println("NicheEvenHandlerComponent-handleSensorInstallation
			// says: manager installation request received, classname is
			// "+className);
			Class managementElementClass;
			ManagementElementInterface newME = null;
	
			boolean error = false;
			
			try {
				managementElementClass = Class.forName(className);
				newME = (ManagementElementInterface) managementElementClass
						.newInstance();
	
			} catch (InstantiationException e) {
				e.printStackTrace();
				error = true;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				error = true;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				error = true;
			}
	
			if(!error) {
				// System.out.println("NMC-instantiate at "+myRef.getId()+" says says
				// connecting : "+id);
				/*#%*/ log.debug("Ready to connect ME-" + id);
				newME.connect(
						id,
						m.getReplicaNumber(),
						myNicheManagementInterface,
						m.getInitiator()
				);
				if(m.isLive()) {
					newME.reinit(parameters);
				} else {
					newME.init(parameters);
				}
				// System.out.println("NicheManagementContainerComponent says: the id of
				// the thing is: "+newME.getId());
				registerManagementElement(
						m.getReplicaNumber(),
						m.getMode(),
						id,
						newME
				);
				/*#%*/ log.debug("Register ME says: registration of id " + id + " completed");
				
			}/*#%*/  else {
			/*#%*/ log.error("Something went wrong when trying to connect ME-" + id);
				//potentially generate an error-reply
				//TODO
			/*#%*/ }
		}
		

	}

	/*
	 * Below are methods to deal with all events which have been dynamically
	 * registered, that is, we're on the side of the installed sensor
	 * 
	 * 
	 */

	public void genericEventHandler(Event e) {
		// System.out.println("NicheEvenHandlerComponent-genericEventHandler
		// says: "+e+ " received, check if any of my sensors is interested");
		// retrieve the list of all sensors associated with this particular
		// event
		systemSensor.eventHandler(e);
		// si.eventHandler(e);

	}

	public void handleSuspectEvent(SuspectEvent e) {
		systemSensor.eventHandler(e);
	}
	
	public synchronized void genericMessageHandler(DeliverToManagementEvent e) {
		
		if(e.isBroadcast()) {
			
			IntervalBroadcastInfo info = (IntervalBroadcastInfo) e.getMessage();
			
			if(info.getInterval().isEmpty()) {
				
				/*#%*/ log.debug(
				/*#%*/ 		"A broadcast request for an empty range is " +
				/*#%*/ 		"received and IGNORED by the management who then " +
				/*#%*/ 		"has no matching items"
				/*#%*/ );
				return;
			}
			
			/*#%*/ log.debug(
			/*#%*/ 		"A broadcast request for the range "
			/*#%*/ 		+ info.getInterval().toString()
			/*#%*/ 		+ " is received from "
			/*#%*/ 		+ info.getSource()
			/*#%*/ 		+ " Do a HandoverRange"
			/*#%*/ );
			
			//we've received a broadcast request!
			//myThreadPool.
			execute(new HandoverRange(info));

		} else if (e.isRequest()) {
			requestReceiver(e);
		} else {
			Message m = (Message) e.getMessage();

			/*#%*/ log.debug(
			/*#%*/ 		"genericMessageHandler got message "
			/*#%*/ 		+ m.getClass().getSimpleName()
			/*#%*/ 		+ (m instanceof DeliverEventMessage ?
			/*#%*/ 				" to " + ((DeliverEventMessage) m).getDestination()
			/*#%*/ 			:
			/*#%*/ 				""
			/*#%*/ 		)
			/*#%*/ );
	
			// Here we list / switch those messages we care about:
			// System.out.println("NicheManagementContainerComponent says: I've got
			// message! says: "+m.getClass().getSimpleName());
			if (m instanceof DeliverEventMessage) {
				deliverEvent((DeliverEventMessage) m);
			} else if (m instanceof DelegationRequestMessage) {
				handleManagementElement((DelegationRequestMessage) m);
			} else if ( (m instanceof UpdateSNRRequestMessage) || (m instanceof UpdateManagementElementMessage)) { 
				updateME((ReplicableMessageInterface) m);
			} else if(m instanceof BindRequestMessage) {
				trigger(new DeliverToNodeEvent(m));
			} else if (m instanceof UpdateSensorMessage) {
				updateSensor((UpdateSensorMessage)m);
			}
			else {
				/*#%*/ log.debug(m.getClass().getSimpleName()
				/*#%*/ 		+ " is an unknown type to me, error");
				
				System.err.println(m.getClass().getSimpleName()
						+ " is an unknown type to me, error");
			}
		}
	}

	// Some DelegationRequestMessages are sent directly, so they come here!
	public void receiveManagementElement(DeliverMessageEvent e) {
		handleManagementElement((DelegationRequestMessage) e.getMessage());
	}

	public void handlePredecessorChanged(PredecessorChanged event) {

		/*#%*/ String message = "handlePredecessorChanged says: ";
		
		DKSRef newPred = event.getNewPred();

		PseudoReliableIntervalBroadcastStartEvent requestEvent = myRM
				.updatePredecessor(newPred);
		// it's resonable that the SRM keeps track of replication degree
		// and the resulting ring-intervals which should be contacted

		if (myRef.equals(newPred)) {
			// do nuffin
			/*#%*/ message += "do nuffin, the new predecessor is myself";
		} else {
			
			ArrayList itemsToHandOver = (ArrayList)requestEvent.getAttachment();
			if(itemsToHandOver != null) {
				/*#%*/ message += "time to handover items!";
				//myThreadPool.
				execute(
						new HandoverRange(newPred, itemsToHandOver)
				);
			} else if(requestEvent.getInfo() != null){
				// send requests to either _one_ symmetric neighbour-range, to
				// some, or to all!
				/*#%*/ message += "time to trigger range-cast!";
				trigger(requestEvent);

			} /*#%*/ else {
			/*#%*/ message += "Nothing to handover, nothing to request";
			/*#%*/ }
		}
		
		/*#%*/ log.debug(message);
	}

	// TIMER STUFF

	public void genericTimeoutHandler(Event e) {
		// now and here we only allow one receiver per timout event!
		myTimeoutReceivers.get(e.getClass().getName()).eventHandler(e, ManagementElementInterface.NEW);
	}

	public long registerTimer(EventHandlerInterface receiver, Class eventClass, Object attachment,
			int timeout) {
		myTimeoutReceivers.put(eventClass.getName(), receiver);
		registerGeneric(eventClass, "genericTimeoutHandler");
		return timerComponent.registerTimer(eventClass, attachment, timeout);
	}

	public void cancelTimer(long timerId) {
		timerComponent.cancelTimer(timerId);
	}

	// LOG STUFF

	/*#%*/ 	public void log(String message) {
	/*#%*/ 		log.info(message);
	/*#%*/ 	}

	// EXECUTE STUFF
	
	public void publicExecute(Runnable task) {
		execute(task);
	}

	/**
	 * @param randomDestination
	 * @return
	 */
	// public NicheId getCloseNode(DKSRef node) {
	// //FOR TESTING ONLY!
	// //FIXME
	// return myRM.getUniqueId();
	// }
	// /**
	// * @param
	// * @return
	// */
	// public NicheId getUniqueId() {
	// return myRM.getUniqueId();
	// }
	
	
//	public NicheId getCollocatedId(NicheId location) {
//		return myRM.getUniqueCollocatedId(location);
//	}

	private void requestReceiver(DeliverToManagementEvent e) {
		
		Object request = e.getMessage();
			
		if (request instanceof GetReferenceMessage) {
				// System.out.println("NMC at "+myRef.getId()+" says:
				// "+request.getClass().getSimpleName() + " =
				// GetReferenceMessage!");
				GetReferenceMessage grm = (GetReferenceMessage) request;

				//right now just assume we only request the references from one copy,
				//from the main one
				Object temp = (SNRElement) myRM.getManagementElement(grm.getId());
				if (temp == null) {
					
					/*#%*/ 	log.debug(
					/*#%*/ 			"GetReferenceMessage for id "
					/*#%*/ 		+ grm.getId()
					/*#%*/ 		+ " failed, no receiver"
					/*#%*/ );
					
					addOutstandingGetRequest(grm.getId(), grm.getReplicaNumber(), e);
					// trigger(new ReplyFromManagementEvent(e, "no receiver"));
					// receiver has to retry!! NOO, store & wait
				} else {
					/*#%*/ log.debug(
					/*#%*/ 	"GetReferenceMessage of type "
					/*#%*/ 	+ grm.getType()
					/*#%*/ 	+ " for id "
					/*#%*/ 	+ grm.getId()
					/*#%*/ 	+ " succeeded, reply to request with op id "
					/*#%*/ 	+ e.getOperationId()
					/*#%*/ );
					
					if(temp instanceof SNRElement) {
						if(grm.getType() == GetReferenceMessage.GET_ALL) {
							trigger(new ReplyFromManagementEvent(e, ((SNRElement)temp).getAll()));
						} else {
							trigger(new ReplyFromManagementEvent(e, ((SNRElement)temp).getAny(myRandom.nextInt())));
						}
					} else {
						//Then we just need the reference of this node, since the ME is hosted here!
						//remember we could choose the send the message along with this req.
						//to save 2 hops, but for now we keep this inefficient pattern
						trigger(new ReplyFromManagementEvent(e, grm.getId().getDKSRefCopy(myRef)));
					}
				}
			} else if (request instanceof DelegateSubscriptionMessage) {
				DelegateSubscriptionMessage r = (DelegateSubscriptionMessage) request;
				// System.out.println("NMC at "+myRef.getId()+" says:
				// "+request.getClass().getSimpleName() + " is being processed");
				/*#%*/ log.debug("DelegateSubscriptionMessage for sink " + r.getSinkName()
				/*#%*/ 		+ " received");
				NicheId sinkId = myRM.getComponentId(r.getSinkName());
				if (sinkId != null) {
					
					// System.out.println("NMC at "+myRef.getId()+" says: I had a
					// receiver with the name "+r.getSinkName()+", it is
					// "+sink.getId());
					IdentifierInterface source = r.getEventSource();
					boolean reliable = source instanceof ReliableInterface ? ((ReliableInterface)source).isReliable() : false;

					/*#%*/ log.debug("Request for sink "
					/*#%*/ 		+ r.getSinkName()
					/*#%*/ 		+ " succeeded, resolved to "
					/*#%*/ 		+ sinkId.toString()
					/*#%*/ 		+ " which is "
					/*#%*/ 		+ (reliable ?
					/*#%*/ 				"reliable "
					/*#%*/ 			:
					/*#%*/ 				"not reliable"
					/*#%*/ 		)		
					/*#%*/ );

					
					Subscription delegatedSubscription = new Subscription(source,
							sinkId, r.getEventName());
	
					myNicheManagementInterface
							.getNicheAsynchronousSupport()
							.sendToManagement(
									source.getId(),
									new UpdateManagementElementMessage(
											source.getId(),
											UpdateManagementElementMessage.TYPE_ADD_SINK,
											delegatedSubscription),
									reliable
							);
	
					trigger(new ReplyFromManagementEvent(e, delegatedSubscription));
	
				} else {
					/*#%*/ log.debug("Request for sink " + r.getSinkName() + " failed");
					trigger(new ReplyFromManagementEvent(e, "no receiver"));
				}
			} else if (request instanceof DelegationRequestMessage) {
				//this is kept for backwards compability - if you are the sender creating
				//a binding for yourself you should use the bind-method without yourself...
				handleManagementElement((DelegationRequestMessage) request);
				
			} else {
				/*#%*/ log.debug("Request " + request.getClass().getSimpleName()
				/*#%*/ 		+ " is of unknown type!");
				
				System.err.println("Request " + request.getClass().getSimpleName()
						+ " is of unknown type!");
			}
		

		// trigger(new ReplyFromManagementEvent("no receiver"));
	}

	private synchronized void registerFractalManager(NicheId id,
			DelegationRequestMessage m) {
		/*#%*/ log.debug("registerFractalManager with id " + id);
		DeployMEClass deployManager =
			new DeployMEClass(
					m,
					m.getMode(),
					myNicheManagementInterface
			);
		//myThreadPool.
		execute(deployManager);

	}

	private synchronized void addOutstandingDeliverRequest(NicheId destinationNicheId, int replicaNumber, ReplicableMessageInterface m) {
		
		String meKey = destinationNicheId.toString() + ":" + replicaNumber;
		
		if (outstandingRequests.containsKey(meKey)) {
			(outstandingRequests.get(meKey)).add(m);
		} else {
			ArrayList ta = new ArrayList();
			ta.add(m);
			outstandingRequests.put(meKey, ta);
		}
		
	}
	
	private synchronized ArrayList getOutstandingDeliverRequests(NicheId destinationNicheId, int replicaNumber) {
		String meKey = destinationNicheId.toString() + ":" + replicaNumber;
		return outstandingRequests.remove(meKey);
	}
	
	private synchronized void addOutstandingGetRequest(NicheId destinationNicheId, int replicaNumber, DeliverToManagementEvent e) {
		
		String meKey = destinationNicheId.toString() + ":" + replicaNumber;
		
		if (outstandingGetRequests.containsKey(meKey)) {
			(outstandingGetRequests.get(meKey)).add(e);
		} else {
			ArrayList ta = new ArrayList();
			ta.add(e);
			outstandingGetRequests.put(meKey, ta);
		}
		
	}
	
	private synchronized ArrayList<DeliverToManagementEvent> getOutstandingGetRequests(NicheId destinationNicheId, int replicaNumber) {
		String meKey = destinationNicheId.toString() + ":" + replicaNumber;
		return outstandingGetRequests.remove(meKey);
	}
	
	class EventDeliverClass implements Runnable {
		ManagementElementInterface receiver;

		DeliverEventMessage message;
		int flag;

		EventDeliverClass(ManagementElementInterface receiver, DeliverEventMessage message, int flag) {
			
			this.receiver = receiver;
			this.message = message;
			this.flag = flag;
		}

		public void run() {
			/*#%*/ log.debug("Inside dedicated thread: Delivering event to "
			/*#%*/ 		+ receiver.getClass().getSimpleName() + " "
			/*#%*/ 		+ receiver.getId().toString() + ":" + receiver.getReplicaNumber()
			/*#%*/ );
			receiver.eventHandler(message.getEvent(), flag);
		}
	}

	// class CreateElementClass implements Runnable {
	//		
	// CreateElementClass(ManagerId manager, DeliverEventMessage message) {
	// this.manager = manager;
	// this.message = message;
	// }
	//
	// public void run() {
	//        	
	// manager.eventHandler(message.getEvent());
	// }
	// }

	class MessageDeliverClass implements Runnable {
		ManagementElementInterface mei;

		Message message;
		
		MessageDeliverClass(ManagementElementInterface mei, Message message) {
			this.mei = mei;
			this.message = message;
		}

		public void run() {

			mei.messageHandler(message);
			if (message instanceof UpdateManagementElementMessage) {
				UpdateManagementElementMessage m = (UpdateManagementElementMessage) message;
				if (m.getType() == UpdateManagementElementMessage.TYPE_REMOVE_BINDING) {
					myRM.removeManagementElement(m.getDestination());
				}
			}
		}
	}

	class HandoverRange implements Runnable {

		IntervalBroadcastInfo info;

		DKSRef recepient;

		ArrayList<DelegationRequestMessage> items;

		int CHUNK_SIZE = 10;
		
		HandoverRange(DKSRef newPred, ArrayList<DelegationRequestMessage> items) {
			this.recepient = newPred;
			this.items = items;
		}

		// This is for taking care of an fail-restore request
		HandoverRange(IntervalBroadcastInfo info) {
			this.info = info;
			this.recepient = info.getInitiator();
		}

		public void run() {

			/*#%*/ String logMessage = "";

			if (info != null) {

				IntervalsList intervalsList = (IntervalsList) info.getInterval();
				Object[] targetBounds = (Object[]) info.getMessage();
					
				myRM.triggerSymmetricSequenceTransfer(
								intervalsList,
								recepient,
								(BigInteger)targetBounds[0],
								(BigInteger)targetBounds[1]
				);


			}/*#%*/  else {
			/*#%*/ 	logMessage = "Predecessor update. New predecessor is " + recepient + "\n";
				// the items are alrady handed over in the constructor

			/*#%*/ } // endif pred change
			
			/*#%*/ log.debug(logMessage);
			/*#%*/ logMessage = "";

			
			NicheId idOfNewNode = myRM.getContainterId(recepient.getId().toString());

			if (items != null && 0 < items.size()) {

				/*#%*/ logMessage += "Sending over: "
				/*#%*/ 				+ items.toArray().length
				/*#%*/ 				+ " items as "
				/*#%*/ 				+ items.toArray().length / CHUNK_SIZE
				/*#%*/ 				+ " chunk-messages";
					
					int startIndex = 0;
					List chunk;
					DelegationRequestMessage message, message1, message2;

					
					do {
						if (items.toArray().length < startIndex
									+ CHUNK_SIZE) {
							
							chunk = items.subList(
											startIndex,
											items.toArray().length
										); 
							} else {
								chunk = items.subList(
											startIndex,
											startIndex + CHUNK_SIZE
										);
							}

							message = new DelegationRequestMessage(
									idOfNewNode,
									DelegationRequestMessage.TYPE_BULK,
									new ArrayList(chunk)
							);
			
							trigger(new SendRequestEvent(
									recepient,
									recepient.getId(),
									message,
									null,
									null,
									(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_NODE)
									)
							);

							startIndex += CHUNK_SIZE;
						
//							try {
//								Thread.sleep(1000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}

						} while (startIndex < items.toArray().length);
							
			}/*#%*/  else { // endif items != null
			/*#%*/ 	logMessage += "No items to hand over";
			/*#%*/ }
			
			/*#%*/ log.debug(logMessage);
		}
		
	}

	class GetComponentInformation implements Runnable {

		Factory deployer;

		NicheManagementInterface myNicheManagementInterface;

		DelegationRequestMessage message;

		public GetComponentInformation(Factory deployer,
				DelegationRequestMessage message,
				NicheManagementInterface nicheManagementInterface) {
			this.deployer = deployer;
			this.message = message;
			this.myNicheManagementInterface = nicheManagementInterface;
			// this.env = env;
		}

		public void run() {

			// PLAY HERE!!!
			synchronized ("kalle") {

				ManagementDeployParameters params = (ManagementDeployParameters) message
						.getManagementDeployParameters();

				/*#%*/ myNicheManagementInterface.getNicheAsynchronousSupport().log(
				/*#%*/ 		"Synchronized GetComponentInformation with deploy content: "
				/*#%*/ 				+ Arrays.deepToString(params.getDeploy()
				/*#%*/ 						.toArray()));

				org.objectweb.fractal.api.Component managedResources = myNicheManagementInterface
						.getResourceManager()
						.getManagedResourcesResourcesComponent();

				// try {
				// TypeFactory typeFactory =
				// Fractal.getTypeFactory(managedResources);
				// } catch (NoSuchInterfaceException e3) {
				// // TODO Auto-generated catch block
				// e3.printStackTrace();
				// }
				// GenericFactory genericFactory;
				// try {
				// genericFactory = Fractal.getGenericFactory(managedResources);
				// } catch (NoSuchInterfaceException e3) {
				// // TODO Auto-generated catch block
				// e3.printStackTrace();
				// }
				//				
				// ComponentType componentType;

				//
				ContentController cc = null;
				try {
					cc = Fractal.getContentController(managedResources);
				} catch (NoSuchInterfaceException e2) {
					e2.printStackTrace();
				}

				// org.objectweb.fractal.api.Component
				ComponentType newComponent = null;

				ArrayList<String[]> deploys = params.getDeploy();
				ArrayList<Map> contexts = params.getContext();

				for (int i = 0; i < deploys.size(); i++) {
					String[] deploy = deploys.get(i);

					Map context = contexts.get(i);
					// //////////////////////////////////////////////////////////////////
					// //// create the new component
					// //////////////////////////////////////////////////////////////////

					// myNicheManagementInterface.getNicheAsynchronousSupport().log("creating
					// comp: " + deploy[0] + " "+ deploy[1] );

					try {
						newComponent = (ComponentType) deployer
								.newComponentType(deploy[0], context);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}

	}

	/**
	 * @param delegateHandler
	 * @param methodName
	 */
	// public void registerDelegateHandler(Object delegateHandler, String
	// methodName) { this.delegateHandler = delegateHandler;
	// this.delegateMethodName = methodName; }
}
