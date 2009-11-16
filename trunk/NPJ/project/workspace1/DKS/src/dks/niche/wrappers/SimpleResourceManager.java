/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.adl.FactoryFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;
import org.objectweb.jasmine.jade.util.Serialization;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.bcast.IntervalBroadcastInfo;
import dks.bcast.events.PseudoReliableIntervalBroadcastStartEvent;
import dks.niche.fractal.FractalInterfaceNames;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.ids.ResourceId;
import dks.niche.ids.SNR;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.utils.IntervalsList;
import dks.utils.RingIntervals;
import dks.utils.SimpleIntervalException;
import dks.utils.SimpleInterval.Bounds;
import dks.web.jetty.JettyServer;

/**
 * The <code>SimpleResourceManager</code> class
 * 
 * @author Joel
 * @version $Id: SimpleResourceManager.java 294 2006-05-05 17:14:14Z joel $
 */
public class SimpleResourceManager {

	// private static String [] initialStorageComponentNodes = {"190000",
	// "190050", "420000"}; //, "830000"}; //, "190050"};
	private String[] stableNodes; // = {"5000", "190000", "420000", "830000"};

	// private static String leavingNode = "190050";
	// private static String frontEndNode = "420050";

	// private static String [] replacementNodes = {"830050"};

	public static String MAIN_COMPONENT = "storage";

	public static String COMPOSITE = "composite";

	public static String FRONTEND = "frontend";

	public final String RANDOM_ID_STRING = "0";

	public final int RANDOM_PORT = 0;

	public final char CAPACITY_DELIMITER = '=';

	//public final char PRIVAT_ID_DELIMITER = ':';

	// public HashMap<String, Boolean> myLocations;

	// public HashMap<String, HashMap<String, ManagementElementInterface>>
	// myManagementElements;
	private SortedList managementElements;
	private SubscriptionList sensorSubscriptons;

	// Mapping NicheId.toString() to the replica number!
	// OBS only the lowest replica number will be present,
	// since if anyone should receive messages, it's the
	// one with the lowest number...
	//TODO: this is of course only temp. has to be hashmap of hashmap
	//or something like that
	//private HashMap<String, Integer> managementElementIndexes;

	// SortedList myPrimeManagementElements;
	// HashMap<String location, HashMap<String privateId,
	// ManagementElementInterface theMEI>>

	public ArrayList<Object> myAllocatedResources;

	private HashMap<String, Object> componentBindReceiversGlobalToLocal;

	private HashMap<Object, IdentifierInterface> componentBindReceiversLocalToGlobal;

	private HashMap<Object, ComponentId> componentsLocalToGlobal;

	private HashMap<String, Object> componentsGlobalToLocal;
	
	private HashMap<String, NicheActuatorInterface> nicheActuatorInterfaces;

	private HashMap<String, String> componentsGlobalIdToADLName;

	private HashMap<String, NicheId> componentsADLNameToGlobalId;

	private HashMap<String, Object> receiverSideBindingsGlobalToLocal; //String = DEFINE HERE:
	//Now just
	//ComponentId+InterfaceName,
	//but should really be
	//GroupId+ComponentId+InterfaceName

	private HashMap<Object, ClientSideBindStub> senderSideBindingsLocalToGlobal;

	private HashMap<String, BigInteger> dcmsCache;

	private HashMap<String, ComponentType> componentNameToType;
	
	private HashMap<String, SNR> snrTemplates;

	// private HashMap<String, ArrayList<NicheId>> collocatedElements;
	// private HashMap<String, NicheId> isCollocatedWith;

	private Component nicheComponent;

	private Component managedResourcesComponent;

	// For tests:
	JettyServer jettyServer;

	static Properties testProperties;

	// int SMALL_STORAGE = 5000;
	// int ORDINARY_STORAGE = 500500;

	// int BIG_STORAGE = 1700;

	// int STORAGE_COMPONENT_SIZE = 500000;
	// int FRONTEND_SIZE = 10;
	// int COMPOSITE_SIZE = 10;

	int myFreeStorage = -1;

	int myDynamicFreeStorage = -1;

	int functionalComponentThreshold = 1100;

	int sizeForManagement = 10;

	DKSParameters dksParameters;

	BigInteger myRingId;
	BigInteger predecessorId;

	long mySeed;
	
	BigInteger[] symmetricDelta;

	DKSRef myDKSRef;

	String webCacheAddress;

	int myPort;

	long littleN;

	int uniqueCounter = 0;

	Random myRandom;

	String myPostition = "pos";

	String lines = "lines";

	String nodes = "stableNodes";

	boolean jadeMode = false;

	boolean started = false;

	private int nodeSelector = 0;

	private boolean dynamic;

	NicheAsynchronousInterface logger;

	private Factory adlDeployer;

	private GenericFactory javaDeployer;

	private ContentController contentController;

	ComponentRegistry componentRegistry;
	
	private String[] knownComponentTypes = {
			"dks.niche.fractal.ManagementElement"
			/* 	
			"dks.niche.fractal.watcher.Watcher",
			"dks.niche.fractal.aggregator.Aggregator",
			"dks.niche.fractal.manager.Manager",
			 */
		};

	/**
	 * @replicationDegree - the system wide replication factor
	 */
	public int replicationDegree;

	ArrayList<Runnable> supportThreads;
	
	int currentLookupAndSendOperation = 5;
	
	/**
	 * @param givenId
	 * @param givenPort
	 * @param dksParameters
	 */
	public SimpleResourceManager(NicheAsynchronousInterface logger,
			String givenId, int givenPort, DKSParameters dksParameters, 
			int replicationDegree) {

		

		this.dksParameters = dksParameters;
		littleN = Long.parseLong("" + dksParameters.N); // "*" doesn't work with
		// BigInteger and double
		this.replicationDegree = replicationDegree;
		this.symmetricDelta = new BigInteger[replicationDegree];
		symmetricDelta[0] = BigInteger.ZERO;
		if (1 < replicationDegree) {
			symmetricDelta[1] = dksParameters.N.divide(new BigInteger(""
					+ replicationDegree));
			for (int i = 2; i < replicationDegree; i++) {
				symmetricDelta[i] = symmetricDelta[i - 1]
						.add(symmetricDelta[1]);
			}
		}

		if (givenId.equals(RANDOM_ID_STRING)) {
			this.myRingId = getBootstrapNodeIdAndSetFreeSpace();
		} else {
			this.myRingId = new BigInteger(givenId);
		}

		this.mySeed = Long.parseLong(myRingId.toString());
		myRandom = new Random(mySeed -1);
		
		this.predecessorId = myRingId;
		
		if (givenPort == RANDOM_PORT) {
			this.myPort = getBootstrapPort();
		} else {
			this.myPort = givenPort;
		}

		stableNodes = loadStableNodes();

		// myLocations = new HashMap();
		new ArrayList<SortedList>();

		
		managementElements = new SortedList(
				logger,
				myRingId, 
				dksParameters
		);
		
		sensorSubscriptons = new SubscriptionList(
				logger,
				myRingId, 
				dksParameters
		);
		

		myAllocatedResources = new ArrayList<Object>();
		componentBindReceiversLocalToGlobal = new HashMap<Object, IdentifierInterface>();
		componentBindReceiversGlobalToLocal = new HashMap<String, Object>();
		componentsLocalToGlobal = new HashMap<Object, ComponentId>();
		componentsGlobalToLocal = new HashMap<String, Object>();
		componentsGlobalIdToADLName = new HashMap<String, String>();
		componentsADLNameToGlobalId = new HashMap<String, NicheId>();
		receiverSideBindingsGlobalToLocal = new HashMap<String, Object>();
		senderSideBindingsLocalToGlobal = new HashMap<Object, ClientSideBindStub>();
		nicheActuatorInterfaces = new HashMap<String, NicheActuatorInterface>();
		dcmsCache = new HashMap<String, BigInteger>();
		componentNameToType = new HashMap<String, ComponentType>();
		snrTemplates = new HashMap<String, SNR>();
		supportThreads = new ArrayList<Runnable>();
		// collocatedElements = new HashMap<String, ArrayList<NicheId>>();

		this.logger = logger;

	}

	public void setDKSRef(DKSRef dksRef) {
		this.myDKSRef = dksRef;
	}
	
	public void setComponentRegistry(ComponentRegistry componentRegistry) {
		this.componentRegistry = componentRegistry; 
	}

	public void setWebCacheAddress(String webCacheAddress) {
		this.webCacheAddress = webCacheAddress;
	}

	
	public String getWebCacheAddress() {
		return this.webCacheAddress;
	}

//	public ResourceRef getResourceRef(IdentifierInterface owner) {
//		return new ResourceRef(myDKSRef, 0, owner.getId().getOwner());
//	}

	public NodeRef getNodeRef() {
		return new NodeRef(myDKSRef);
	}

	public DKSRef getDKSRef() {
		return myDKSRef;
	}

	public void setJettyServer(JettyServer jettyServer) {
		this.jettyServer = jettyServer;
	}

	public void setTestProperties(Properties testProperties) {
		this.testProperties = testProperties;
	}

	public Properties getTestProperties() {
		return this.testProperties;
	}

	// seems to not work :(
	// public void addServlet(NicheServlet servlet) {
	// jettyServer.pausAndAddServlet(servlet, servlet.getContext());
	//		            
	// }
	// DISCOVER

	public int checkFreeSpace(String req) {

		int d = req.indexOf(':');
		if (d > 0) {
			// This means we are in the dynamic discovery mode
			dynamic = true;
			req = req.substring(d + 1, req.length());
			if (myFreeStorage < 0) {
				myFreeStorage = myDynamicFreeStorage;
			}
		}

		/*#%*/ logger.log("RM says: my free storage is: " + myFreeStorage
		/*#%*/ 		+ " and the req is " + req);
		// System.out.println("RM at " + myRingId + " says: my free storage is:
		// "+myFreeStorage+ " and the req is "+req);

		int reqInt = Integer.parseInt(req);
		if (reqInt <= myFreeStorage) {
			if (myFreeStorage - sizeForManagement < reqInt) {
				return myFreeStorage;
			}
			if (functionalComponentThreshold < reqInt) {
				return myFreeStorage - sizeForManagement;
			}
			if (reqInt < functionalComponentThreshold) {
				return reqInt;
			}
			return myFreeStorage;
		}

		return -1;

	}

	// ALLOCATE

	public void addAllocatedResource(Object object) {
		myAllocatedResources.add(object);

	}

	public void freeAllocatedResource(Object object) {
		myAllocatedResources.remove(object);

	}

	// public int decreaseFreeSpace(int s) {
	//		
	// myFreeStorage -= s;
	// return myFreeStorage;
	//
	// }

	public int decreaseFreeSpace(ResourceId rid) {

		myFreeStorage -= rid.getTotalStorage(); // TODO: should be
		// getAllocatedStorage();
		// System.out.println("ResourceManager at " + myRingId + " says: I've
		// decreased my free space by " + rid.getTotalStorage() + " to " +
		// myFreeStorage +" (compare with "+rid.getAllocatedStorage()+ ")");
		/*#%*/ logger.log("ResourceManager says: I've decreased my free space by "
		/*#%*/ 		+ rid.getTotalStorage() + " to " + myFreeStorage
		/*#%*/ 		+ " (compare with " + rid.getAllocatedStorage() + ")");
		return myFreeStorage;

	}

	// public int decreaseFreeSpace(Object req) {
	//		
	// String cName;
	// if (jadeMode) {
	// DeploymentParams params=null;
	// try {
	// params=(DeploymentParams) Serialization.deserialize((String)req);
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// e.printStackTrace();
	// }
	// cName = params.name;
	// }
	// else {
	// cName = (String)req;
	// }
	//		
	// if(cName.startsWith(MAIN_COMPONENT)) {
	//
	// myFreeStorage -= STORAGE_COMPONENT_SIZE;
	// return STORAGE_COMPONENT_SIZE;
	//			
	// }
	//		
	// if(cName.startsWith(FRONTEND)) {
	// myFreeStorage -= FRONTEND_SIZE;
	// return FRONTEND_SIZE;
	// }
	//		
	// if(cName.startsWith(COMPOSITE)) {
	// myFreeStorage -= COMPOSITE_SIZE;
	// return COMPOSITE_SIZE;
	// }
	// return 0;
	//
	// }

	public String checkIfStorage(Object req) {

		if (dynamic) {
			String cName;
			DeploymentParams params = null;
			if (jadeMode) {
				// DeploymentParams params=null;
				try {
					params = (DeploymentParams) Serialization
							.deserialize((String) req);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				cName = params.name;
			} else {
				cName = (String) req;
			}

			if (cName.startsWith(MAIN_COMPONENT)) {

				return params.name;

			}
			return null;
		}
		return null;
	}

	public void setFreeSpace(int s) {

		myFreeStorage = s;

	}

	public String checkPreferences(String pref) {

		int d = pref.indexOf(':');
		if (d > 0) {
			// This means we have a prefix to remove
			return pref.substring(d + 1, pref.length());

		}
		return pref;
	}

	// LOCATIONS

	// private void addLocation(NicheId id) {
	// myLocations.put(id.getLocation(), true);
	// }

	// public boolean removeLocation(NicheId id) {
	// return myLocations.remove(id.getLocation());
	// }
	public boolean hasLocation(NicheId id) {
		// logger.log("My positions: " + myManagementElements.printPositions());
		return managementElements.containsKey(id.getLocation());
	}

	public Object[] getSubscriptionAndMessage(IdentifierInterface source,
			IdentifierInterface sink, String eventName) {
		return getSubscriptionAndMessage(source, sink, eventName, null);
	}
	
	public Serializable[] getSubscriptionAndMessage(IdentifierInterface source,
			IdentifierInterface sink, String eventName, Object tag) {
		
//		if(eventName.equals(ComponentFailEvent.class.getName())
//			||
//		   eventName.equals(ResourceLeaveEvent.class.getName())
//		   ||
//		   eventName.equals(CreateGroupEvent.class.getName())
//			) {
//			
//			SensorSubscription ss = new SensorSubscription(
//					source,
//					source,
//					sink,
//					eventName,
//					null);
//			
//			return new Object [] {
//				
//					ss,
//					new DelegationRequestMessage(				
//						sink.getId(),
//						DelegationRequestMessage.TYPE_SENSOR,
//						new Object[] {ss}
//						)
//				};
//		}
		
		Subscription subscription = new Subscription(source, sink, eventName, tag);
		
		return new Serializable[] {
			
				subscription,
				new UpdateManagementElementMessage(
					source.getId(),
					UpdateManagementElementMessage.TYPE_ADD_SINK,
					subscription 
				)
		};
	}
	public synchronized PseudoReliableIntervalBroadcastStartEvent updatePredecessor(
			DKSRef newPredecessor) {
		
		PseudoReliableIntervalBroadcastStartEvent event = new PseudoReliableIntervalBroadcastStartEvent();
		
		BigInteger oldPredecessorId = predecessorId;
		BigInteger newPredecessorId = newPredecessor.getId();
		predecessorId = newPredecessorId;
		
		//FIXME
		//checking of the interval endpoints?
		managementElements.updatePredecessorAndTriggerTransfer(newPredecessor, newPredecessorId);
		ArrayList<DelegationRequestMessage> itemsToSend = sensorSubscriptons.updatePredecessor(newPredecessorId);
		//itemsToSend.addAll(moreItemsToSend);		
		
		if(RingIntervals.belongsTo(newPredecessorId, oldPredecessorId, myRingId, dksParameters.N, RingIntervals.Bounds.OPEN_CLOSED)) { 

//			ArrayList attachment = new ArrayList<ArrayList<DelegationRequestMessage>>();
//			attachment.add(itemsToSend);
			
			event.setAttachment(itemsToSend);
			
		} else if (newPredecessor == null
					||
					newPredecessor.equals(myDKSRef)
					||
					newPredecessor.equals(oldPredecessorId)
				) {
			/*#%*/ logger.log(
			/*#%*/ 		"Do nothing, 'new' predecessor is "
			/*#%*/ 		+ (
			/*#%*/ 			newPredecessor == null ?
			/*#%*/ 				"null"
			/*#%*/ 			 :
			/*#%*/ 				newPredecessor.equals(myDKSRef) ?
			/*#%*/ 					"myself"
			/*#%*/ 				 :
			/*#%*/ 					 "same as the old!"
			/*#%*/ 		)
			/*#%*/ 		+" do nuffin"
			/*#%*/ );
		} else {
			/* 
			Responsibilities have grown <=> Old pred is gone,
			add request to contact symmetric neighbours
			for the range between the old pred and the new
			*/
			
			IntervalsList missingRange = null;
			
			try {
				missingRange = new IntervalsList(newPredecessorId, oldPredecessorId, Bounds.OPEN_CLOSED, dksParameters.N);
			} catch (SimpleIntervalException e1) {
				// should not happen after null-check has been included
				e1.printStackTrace();
			}
			
			BigInteger targetIntervalStart, intervalStart, intervalEnd;
			
			targetIntervalStart = newPredecessorId.add(BigInteger.ONE);

			//TODO:
			//Here is the point where it is hardcoded that the request only goes to the first
			//replica: this could be replaced with something more advanced.
			//(and would need to for individual MEs implementing stricter consistency
			//guarantees)
			intervalStart = getSymmetricId(targetIntervalStart, 1);
			intervalEnd = getSymmetricId(oldPredecessorId, 1);
				
			
			
			/*#%*/ String logMessage =
			/*#%*/ 					
			/*#%*/ 		"The resource manager is requesting elements from nodes responsible for the range " 
			/*#%*/ 		+ intervalStart
			/*#%*/ 		+ " to "
			/*#%*/ 		+ intervalEnd
			/*#%*/ 		+ " corresponding to symmetric replica 1 of the missing range\n"
			/*#%*/ 		+ newPredecessorId
			/*#%*/ 		+ " - "
			/*#%*/ 		+ oldPredecessorId;
			
			/*#%*/ System.out.println(logMessage);
			/*#%*/ logger.log(logMessage);			
	
			IntervalsList requestRange = null, tempRange = null;
			try {			
				requestRange = new IntervalsList(intervalStart, intervalEnd, Bounds.OPEN_CLOSED, dksParameters.N);
				//OPEN_CLOSED : ]interval]
				
			} catch (SimpleIntervalException e) {
				// should not happen after null-check has been included
				e.printStackTrace();
				
				/*#%*/ logMessage =
									
				/*#%*/ 		"ERROR in flen, no range!";
				
				/*#%*/ System.out.println(logMessage);
				/*#%*/ logger.log(logMessage);					
			}
			
			
			//Fortunately, if they are overlapping, the overlap will be inside the requested range anyhow... 
			
			IntervalBroadcastInfo info = new IntervalBroadcastInfo();			
			info.setInterval(requestRange);
			info.setMessage(new Object[]{targetIntervalStart, oldPredecessorId});
			
			info.setIdRangeCast(true); //to id-ranges, not to nodes!
			
				
			
			info.setAggregate(false);
			// we do not want the recepients to send their
			// data back with the request, they have to do that
			//using direct send...

			//we want the standard delivery event to be triggered upon returning,
			//since we want the
			//NicheCommunicatingComponent.receiveBroadcastResultHandler
			//to parse the result...

			event.setInfo(info);

			
		}
		
		
		return event;
	}

	// public ArrayList<DelegationRequestMessage> getHandoverSequence(BigInteger
	// newPredecessorId) {
	// return myManagementElements.getAndRemoveSequence(newPredecessorId);
	// }

	// public ArrayList<DelegationRequestMessage>
	// getSymmetricSequence(BigInteger intervalStart, BigInteger intervalEnd) {
	// return myManagementElements.getSequence(intervalStart, intervalEnd);
	// }

	public void triggerSymmetricSequenceTransfer(
			IntervalsList requestedRange,
			DKSRef believedReceiver,
			BigInteger targetIntervalStart,
			BigInteger targetIntervalEnd) {
			
		managementElements.triggerSequenceTransfer(
				requestedRange,
				believedReceiver,
				targetIntervalStart,
				targetIntervalEnd,
				this
		);
		//Here no subscriptions are being sent - 
		//the rational is that groups redo subscriptions
		//for failures etc when they re-init
		//TODO: still needs to be verified
	}

	// public Object[] getLocations() {

	// return myManagementElements.keySet().toArray();
	// }

	// public Object[] getManagementElements(String location) {
	// return myManagementElements.get(location).values().toArray();
	// }

	public ManagementElementInterface removeManagementElement(
			NicheId elementToRemove) {
		return managementElements.remove(elementToRemove);
	}

	// public Object[] removeManagementElements(String location) {
	// //myLocations.remove(location);
	// return myManagementElements.remove(location).values().toArray();
	// }
	// public ArrayList<String> getIds(String location) {
	// return myLocations.values().toArray();
	// }

	// FUNCTIONAL COMPONENTS & MANAGERS

	public void addComponentBindReceiver(IdentifierInterface cid,
			Object component, Object componentDescription) {
		// System.out.println("RM at "+myRingId +" says: storing "+cid.getId());
		componentBindReceiversGlobalToLocal.put(cid.getId().toString(),
				component);
		componentBindReceiversLocalToGlobal.put(component, (ComponentId) cid);
		componentsGlobalIdToADLName.put(cid.getId().toString(),
				getComponentName(componentDescription));

	}

	public synchronized boolean addManagementBindReceiver(
			IdentifierInterface id, Object component,
			Object componentDescription, String name) {
		if (!name.endsWith(FractalInterfaceNames.PROXY_SUFFIX)) {
			//TODO - string naming is baad
			// we want to store the user provided
			// component
			/*#%*/ logger.log("RM says: storing ME " + name + " " + id.getId());
			componentBindReceiversGlobalToLocal.put(
					id.getId().toString(),
					component
			);
			
			componentBindReceiversLocalToGlobal.put(
					component,
					id
			);
			componentsADLNameToGlobalId.put(name, id.getId());
			return true;
		}
		return false;

	}

	public void addComponent(ComponentId cid, Object component) {
		componentsLocalToGlobal.put(component, cid);
		componentsGlobalToLocal.put(cid.getId().toString(), component);
	}

	public synchronized Component localADLDeploy(String name, Map context) {

		Component component = null;
		try {
			component = (org.objectweb.fractal.api.Component) adlDeployer
					.newComponent(name, context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return component;

	}

	public synchronized Component localJavaDeploy(String contentDesc, String type,
			ComponentType componentType) {

		Component component = null;
		if (componentType == null) {
			componentType = getComponentType(contentDesc);
		}
		try {
			component = (org.objectweb.fractal.api.Component) javaDeployer
					.newFcInstance(componentType, type, contentDesc);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return component;
	}

	private void preloadComponentTypes() {

		for (int i = 0; i < knownComponentTypes.length; i++) {
			getComponentType(knownComponentTypes[i]);
		}
	}

	public ComponentType getComponentType(String adlName) {

		ComponentType componentType = componentNameToType.get(adlName);

		if (componentType != null) {

			return componentType;
		}
		// else

		try {
			componentType = (ComponentType) adlDeployer.newComponentType(
					adlName, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*#%*/ logger.log("RM says: new component type for " + adlName
		/*#%*/ 		+ " generated: " + componentType.getClass().getSimpleName());

		componentNameToType.put(adlName, componentType);

		return componentType;
	}

	public ContentController getContentController() {
		return contentController;
	}

	// Adds the ME 
	public void addManagementElement(int replicaNumber, NicheId nicheId, ManagementElementInterface mei) {
		managementElements.put(getSymmetricId(new BigInteger(nicheId.getLocation()), replicaNumber), nicheId, mei);
	}
	
	// Adds a subscription 
	public void addSensorSubscription(int replicaNumber, NicheId nicheId, DelegationRequestMessage m) {
		sensorSubscriptons.put(
				getSymmetricId(
					new BigInteger(
						nicheId.getLocation()
					),
					replicaNumber
				),
				nicheId, 
				m
		);
	}

	
	public void addManagementElement(BigInteger index, NicheId nicheId, ManagementElementInterface mei) {
		managementElements.put(index, nicheId, mei);
	}

	
	public ManagementElementInterface getManagementElement(NicheId nicheId) {
		return managementElements.get(new BigInteger(nicheId.getLocation()), nicheId);
	}

	
	public ManagementElementInterface getManagementElement(BigInteger ringId, NicheId nicheId) {
		return managementElements.get(ringId, nicheId);
	}

	// ID RANGE/S

	public boolean belongsToMe(BigInteger id) {
		return managementElements.belongsToMe(id);
	}

	// ID RANGE/S

//	public int belongsToMeSymmetrically(NicheId nicheId) {
//		//this is dangerous - in a small system these ranges will be over-
//		//lapping, so infact in some cases belongsToMeSymmetrically
//		//should return [i, AND i+n] ...
//		BigInteger id = new BigInteger(nicheId.getLocation());
//		for (int i = 0; i < replicationDegree; i++) {
//			if (allManagementElements.get(i).belongsToMe(getSymmetricId(id, i))) {
//				System.out.println("Id " + nicheId.toString() +" gave " + i);
//				return i;
//			}
//		}
//		System.out.println("Id " + nicheId.toString() +" gave " + -1);
//		return -1;
//	}

	// public void addBinding(BindElement b, Object ref) {
	// bindingsGlobalToLocal.put(b.getId().toString(), ref);
	// bindingsLocalToGlobal.put(ref, b);
	// }

	// BINDINGS
	public void addSenderSideBinding(ClientSideBindStub b, Object ref) {
		senderSideBindingsLocalToGlobal.put(ref, b);
	}
	public ClientSideBindStub getSenderSideBinding(Object ref) {
		return senderSideBindingsLocalToGlobal.get(ref);
	}

	public String addReceiverSideBinding(NicheId finalReceiver, BindId b, Object ref) {
		String bindReceiverDescription = getBindReceiverDescription(finalReceiver, b);
		receiverSideBindingsGlobalToLocal.put(bindReceiverDescription, ref);
		return bindReceiverDescription;
	}
	public Object getLocalReceiverSideBindReference(NicheId finalReceiver, BindId b) {
		return receiverSideBindingsGlobalToLocal.get(getBindReceiverDescription(finalReceiver, b));
	}

//	public Object getBindReference(NicheId b) {
//		return bindingsGlobalToLocal.get(b.toString());
//	}

	public ComponentId getComponentId(Object component) {
		return (ComponentId)(componentBindReceiversLocalToGlobal.get(component));
	}

	public NicheActuatorInterface getNicheActuatorInterface(Object component) {
		IdentifierInterface identifierInterface = componentBindReceiversLocalToGlobal.get(component);
		
		if(null == identifierInterface) {
			return null;
		}
		String idAsString = identifierInterface.getId().toString();
		if(nicheActuatorInterfaces.containsKey(idAsString)) {
			return nicheActuatorInterfaces.get(idAsString);
		}
		
		NicheActuatorInterface nicheActuatorInterface =
			new NicheOSSupportFork(
					logger,
					identifierInterface.getId(),
					null,
					true
		); 
		nicheActuatorInterfaces.put(idAsString, nicheActuatorInterface);
		return nicheActuatorInterface;
		
	}
	
	public NicheId getComponentId(String componentName) {
		return (componentsADLNameToGlobalId.get(componentName));
	}

	public Object getComponent(ComponentId componentId) {
		return (componentsGlobalToLocal.get(componentId.getId().toString()));
	}

	public Object getComponentBindReceiver(NicheId id) {
		/*#%*/ logger.log("RM says: retrieving " + id);
		return componentBindReceiversGlobalToLocal.get(id.toString());
	}

	public int getTotalStorage(Object component) {
		// Testing
		IdentifierInterface identifierInterface = (componentBindReceiversLocalToGlobal.get(component));
		
		if(identifierInterface instanceof ComponentId) {
			int temp = 
				((ComponentId)identifierInterface).getResourceRef().getAllocatedStorage();
			if (temp > 0) {
				return temp;
			}
			return ((ComponentId)identifierInterface).getResourceRef().getTotalStorage();
			// TODO: should "always" be allocated storage
		}
		return 0;
	}

	private String[] loadStableNodes() {
		boolean exists = (new File(nodes)).exists();

		if (exists) {
			ArrayList<String> tempArray = new ArrayList<String>();

			try {

				BufferedReader idReader = new BufferedReader(new FileReader(
						nodes));

				String lastReadLine = idReader.readLine();
				while (lastReadLine != null) {
					tempArray.add(lastReadLine);
					lastReadLine = idReader.readLine();
				}
				idReader.close();

			} catch (Exception ioe) {
				// ignored: ioe.printStackTrace();
				tempArray = null;
			}

			if (tempArray == null) {
				/*#%*/ logger.log("File error, no stable nodes");
				return null;
			}
			String[] nodes = new String[tempArray.size()];
			for (int i = 0; i < nodes.length; i++) {
				nodes[i] = tempArray.get(i);
			}
			return nodes;

		}
		return null; // file did not exist

	}

	private BigInteger getBootstrapNodeIdAndSetFreeSpace() {

		boolean exists = (new File(myPostition)).exists();
		int position = 0;

		if (exists) {
			// File exists
			BufferedReader positionReader;
			try {
				positionReader = new BufferedReader(new FileReader(myPostition));
				position = Integer.parseInt(positionReader.readLine());
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			position = 0;
		}

		String idSourceLine = null;
		BigInteger randomId;

		try {

			PrintWriter positionWriter = new PrintWriter(new FileWriter(
					myPostition));
			positionWriter.print("" + (position + 1));
			positionWriter.close();

			BufferedReader idReader = new BufferedReader(new FileReader(lines));

			for (int i = 0; i <= position; i++) {
				idSourceLine = idReader.readLine();
			}
			idReader.close();

		} catch (Exception ioe) {
			// ignored: ioe.printStackTrace();
			idSourceLine = "";
		}

		String idSource = "";
		int pos = idSourceLine.indexOf(CAPACITY_DELIMITER);
		if (pos > 0) {
			idSource = idSourceLine.substring(0, pos);
			String space = idSourceLine.substring(pos + 1, idSourceLine
					.length());

			if (Character.isDigit(space.charAt(0))) {
				myFreeStorage = Integer.parseInt(space);
			} else {
				myDynamicFreeStorage = Integer.parseInt(space.substring(1,
						space.length()));
			}

		} else {
			idSource = idSourceLine;
		}

		if (idSource.length() < 1) {
			randomId = getRandomNodeId();
			System.out.println("No predefined id found, returning random id: "
					+ randomId + " and setting storage capacity to "
					+ myFreeStorage);
			return randomId;
		}
		// System.out.println("Returning id: "+idSource);
		// There was a file, which is only used in Jade-mode: therefore:
		jadeMode = true;
		randomId = new BigInteger(idSource);

		if (randomId.equals(new BigInteger("0"))) {
			randomId = getRandomNodeId();
			System.out.println("No predefined id found, returning random id: "
					+ randomId + " and setting storage capacity to "
					+ myFreeStorage);

		}
		return randomId;

	}

	private BigInteger getRandomNodeId() {
		return new BigInteger("" + (long) (littleN * myRandom.nextDouble()));
	}

	private String getRandomNodeIdString() {
		return "" + (long) (littleN * myRandom.nextDouble());
	}

	private int getBootstrapPort() {

		return 10000 + myRandom.nextInt(30000);
	}
	
	public String getBindReceiverDescription(NicheId finalReceiver, BindId bindId) {
		return finalReceiver.toString() + bindId.getReceiverSideInterfaceDescription();  
	}

	/*
	 * Please tell, _where_ should the naming service be, and how should it be
	 * accessed??
	 * 
	 * 
	 */

	public NicheId getNicheId(String location, String owner, int type, boolean reliable) {
		uniqueCounter++;
		return new NicheId(
				location,
				owner,
				myRingId.toString(),
				""+uniqueCounter,
				type,
				reliable
		);
	}
	
//	public NicheId getUncheckedUniqueId(NicheId ownerId) {
//		
//		return new
//	}

	public NicheId getNicheId(IdentifierInterface locationId, String owner, int type, boolean reliable) {
		
		String location = locationId != null ? locationId.getId().getLocation() :  getUncheckedStableLocation();
		return getNicheId(
				location,
				owner,
				type,
				reliable
		);
	}

	public NicheId getUniqueCollocatedId(NicheId location, int type) {
		return getNicheId(location.getLocation(), location.getOwner(), type, location.isReliable());
	}
	
	public NicheId getContainterId(String location) {
		return new NicheId().setLocation(location);
	}

//	public NicheId getUniqueCollocatedId(BigInteger location) {
//		uniqueCounter++;
//		return new NicheId(location.toString(), myRingId.toString()
//				+ PRIVAT_ID_DELIMITER + uniqueCounter);
//	}

//	public NicheId getCollocatedRandomId(BigInteger location) {
//		return new NicheId(location.toString(), myRingId.toString()
//				+ PRIVAT_ID_DELIMITER + myRandom.nextInt());
//	}

//	public NicheId getLocalId() {
//		uniqueCounter++;
//		return new NicheId(myRingId.toString(), myRingId.toString()
//				+ PRIVAT_ID_DELIMITER + uniqueCounter);
//	}

//	public NicheId getUncheckedLocation() {
//		return new NicheId(getRandomNodeIdString(), "");
//	}

	private String getUncheckedStableLocation() {
		if (stableNodes != null) {
			nodeSelector = (nodeSelector + 1) % stableNodes.length;
			return stableNodes[nodeSelector];
		}
		return getRandomNodeIdString();
	}

//	public NicheId getUncheckedStableId() {
//		if (stableNodes != null) {
//			nodeSelector = (nodeSelector + 1) % stableNodes.length;
//			uniqueCounter++;
//			return new NicheId(stableNodes[nodeSelector], myRingId.toString()
//					+ PRIVAT_ID_DELIMITER + uniqueCounter); // (int)(myRandom.nextFloat()*stableNodes.length)
//		} else {
//			return new NicheId(getRandomNodeIdString(), myRingId.toString()
//					+ PRIVAT_ID_DELIMITER + uniqueCounter);
//		}
//	}

//	public NicheId getSuccessorNodeId(DKSRef ref) {
//		//uniqueCounter++;
//		//Testing to create non unique sensor subscriptions for less overhead...
//		return getSuccessorNodeId(ref.getId());
//		
//	}
	
//	public NicheId getSuccessorNodeId(NicheId nicheId) {
//		//uniqueCounter++;
//		return getSuccessorNodeId(new BigInteger(nicheId.getLocation()));
//	}
	
	public NicheId getSuccessorNodeContainerId(String id) {
		//uniqueCounter++;
		if(id == null) {
			return getContainterId(
					componentRegistry.getRingMaintainerComponent().getRingState().successor.getId().toString()
			);
		}
		return getContainterId(new BigInteger(id).add(BigInteger.ONE).toString());
	}
	
//	private NicheId getSuccessorNodeId(BigInteger ringId) {
//		return new NicheId("" + (ringId.add(new BigInteger("1"))), "");
//	}
//
//	public NicheId getSameNodeId(DKSRef ref) {
//		uniqueCounter++;
//		return new NicheId("" + ref.getId(), myRingId.toString()
//				+ PRIVAT_ID_DELIMITER + uniqueCounter);
//	}

	/**
	 * @return
	 */
	public BigInteger getId() {
		return myRingId;
	}

	/**
	 * @return
	 */
	public int getPort() {
		return myPort;
	}

	public void registerNicheComponent(Component nicheComponent) {
		this.nicheComponent = nicheComponent;
	}

	public Component getNicheComponent() {
		return nicheComponent;
	}

	public void registerManagedResourcesComponent(
			Component managedResourcesComponent) {
		this.managedResourcesComponent = managedResourcesComponent;
		// NOW we can init the fractal factories
		try {
			adlDeployer = FactoryFactory
					.getFactory(FactoryFactory.FRACTAL_BACKEND);
			// with just getFactory() it seems you get a raw-component-factory,
			// they are now longer wrapped
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			javaDeployer = Fractal.getGenericFactory(Fractal
					.getBootstrapComponent()); // managedResources
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			contentController = Fractal
					.getContentController(managedResourcesComponent);
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		// and preload the known component types
		preloadComponentTypes();
		started = true;

	}

	public Component getManagedResourcesResourcesComponent() {
		return managedResourcesComponent;
	}

	public void startLocalFractalComponent(ComponentId cid) {
		startLocalFractalComponent(componentsGlobalIdToADLName.get(cid.getId()
				.toString()));
	}

	public void startLocalFractalComponent(String name) {
		/*#%*/ logger.log("RM says: Trying to start the component: " + name);

		Component theComponent = null;
		try {
			ContentController cc = Fractal
					.getContentController(managedResourcesComponent);
			Component subComponents[] = cc.getFcSubComponents();

			for (int i = 0; i < subComponents.length; i++) {
				if (Fractal.getNameController(subComponents[i]).getFcName()
						.equals(name)) {
					theComponent = subComponents[i];
					break;
				}
			}
		} catch (NoSuchInterfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (theComponent == null) {
			/*#%*/ logger
			/*#%*/ 		.log("RM says: The local component " + name
			/*#%*/ 				+ " is not found");
		} else {
			try {
				LifeCycleController lcc = Fractal
						.getLifeCycleController(theComponent);
				lcc.startFc();
				/*#%*/ 	logger.log("RM says: The component " + name + " is started!");
			} catch (NoSuchInterfaceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalLifeCycleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
	
	public void addSupportThread(Runnable task) {
		supportThreads.add(task);
	}

	public void startSupportThreads() {
		for (Runnable runnable : supportThreads) {
			logger.publicExecute(runnable);
		}
	}
	/**
	 * @param componentDescription
	 * @return
	 */
	public String getComponentName(Object componentDescription) {
		if (jadeMode) {
			DeploymentParams params = null;
			try {
				params = (DeploymentParams) Serialization
						.deserialize((String) componentDescription);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return params.name;
		}

		return (String) componentDescription;

	}
	
	public DeploymentParams getDeploymentParams(Object componentDescription) {
		
		DeploymentParams params = null;
			try {
				params = (DeploymentParams) Serialization
						.deserialize((String) componentDescription);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return params;
	}

	public BigInteger getSymmetricId(BigInteger p, int replicaNumber) {
		if (replicaNumber < 1) {
			return p;
		}
		return p.add(symmetricDelta[replicaNumber]).mod(dksParameters.N);
	}


	public Object[] getReplicaTransferInfo(BigInteger replicaZeroIndex, BigInteger targetIntervalStart, BigInteger targetIntervalEnd) {

		BigInteger destinationRingId = null; 
		int replicaNumber;
		
		for (replicaNumber = 0; replicaNumber < replicationDegree; replicaNumber++) {
			destinationRingId = getSymmetricId(replicaZeroIndex, replicaNumber);
			if (RingIntervals.belongsTo(destinationRingId, targetIntervalStart, targetIntervalEnd, dksParameters.N, RingIntervals.Bounds.OPEN_CLOSED)) {
				break;
			}
		}
		return new Object[]{destinationRingId, replicaNumber };

	}
	
	public ArrayList<String> loadSettings(String fileName) {

		boolean exists = (new File(myPostition)).exists();
		if (!exists) {
			return null;
		}

		BufferedReader settingsReader;
		ArrayList<String> settings = new ArrayList();
		try {

			settingsReader = new BufferedReader(new FileReader(fileName));
			String lastSetting = settingsReader.readLine();
			while (lastSetting != null) {
				// System.out.println("last line was "+lastSetting);
				settings.add(lastSetting);
				lastSetting = settingsReader.readLine();
			}

		} catch (Exception ioe) {
			return null;
		}
		return settings;
	}

	public String[] wrapInterfaceDescriptions(String senderInterface, String receiverInterface, int type) {
		
		String wrappedSenderInterface, wrappedReceiverInterface;
		
		if (senderInterface
				.startsWith(FractalInterfaceNames.DISTRIBUTED_CLIENT_INTERFACE_PREFIX) || 
				senderInterface
				.startsWith(FractalInterfaceNames.DISTRIBUTED_CLIENT_REPLY_INTERFACE_PREFIX)) {
			wrappedSenderInterface = senderInterface;
		} else {
			
			if( (type & JadeBindInterface.WITH_RETURN_VALUE) !=  0) {
				
				wrappedSenderInterface = FractalInterfaceNames.DISTRIBUTED_CLIENT_REPLY_INTERFACE_PREFIX
					+ senderInterface;

			} else {
				wrappedSenderInterface = FractalInterfaceNames.DISTRIBUTED_CLIENT_INTERFACE_PREFIX
					+ senderInterface;
			}
		}
		if (receiverInterface
				.startsWith(FractalInterfaceNames.DISTRIBUTED_SERVER_INTERFACE_PREFIX)) {
			wrappedReceiverInterface = receiverInterface;
		} else {
			wrappedReceiverInterface = FractalInterfaceNames.DISTRIBUTED_SERVER_INTERFACE_PREFIX
					+ receiverInterface;
		}
		return new String[]{wrappedSenderInterface, wrappedReceiverInterface};

	}
	public boolean registerSNRTemplate(String templateName, SNR template) {
		if(snrTemplates.containsKey(templateName)) {
			return false;
		}
		snrTemplates.put(templateName, template);
		return true;
	}
	public SNR getSNRTemplate(String templateName) {
		return snrTemplates.get(templateName);
	}
	// public void registerCollocation(NicheId existingId, NicheId newId) {
	// NicheId bootstrap = isCollocatedWith.get(existingId.toString());
	// ArrayList ta;
	// if(bootstrap != null) {
	// ta = collocatedElements.get(bootstrap.toString());
	// ta.add(newId);
	// } else {
	// ta = new ArrayList<NicheId>();
	// ta.add(newId);
	// collocatedElements.put(existingId.toString(), ta);
	// isCollocatedWith.put(newId.toString(), existingId);
	// }
	// }

	/**
	 * @param destination
	 * @return
	 */
	public BigInteger dcmsCacheGet(NicheId destination) {
		return dcmsCache.get(destination.toString());
	}

	/**
	 * @param destination
	 * @return
	 */
	public BigInteger dcmsCachePut(NicheId destination, BigInteger ringId) {
		return dcmsCache.put(destination.toString(), ringId);
	}

	public boolean isStarted() {
		return started;
	}

	public Object getDeploymentLock() {
		return javaDeployer;
	}
	
	public int getReplicationFactor() {
		return replicationDegree;
	}
	
	public long getRandomSeed(Object type) {
		return mySeed++;
	}
	
	
	public synchronized int getNextLookupAndSendOperationIndex() {

		int thisOperation = currentLookupAndSendOperation;
		currentLookupAndSendOperation = ((currentLookupAndSendOperation + 1)
				% NicheSendClass.MESSAGE_QUEUE) + 1; //OBS important,
		//should not be 0 in order not to be confused with
		//non-supervised operations
		return thisOperation;

	}
	
//	public int getRandom(int range) {
//		return myRandom.nextInt(range);
//	}
	
}
