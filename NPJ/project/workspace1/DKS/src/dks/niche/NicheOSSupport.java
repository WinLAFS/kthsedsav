/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche;

import java.io.Serializable;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.Event;
import dks.boot.DKSPropertyLoader;
import dks.boot.DKSWebCacheManager;
import dks.messages.Message;
import dks.niche.components.NicheOverlayServiceComponent;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.hiddenEvents.DeliverToManagementEvent;
import dks.niche.hiddenEvents.DeliverToNodeEvent;
import dks.niche.hiddenEvents.SendRequestEvent;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheActuatorInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheComponentSupportInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.ReplicableMessageInterface;
import dks.niche.messages.AllocateRequestMessage;
import dks.niche.messages.AllocateResponseMessage;
import dks.niche.messages.BindRequestMessage;
import dks.niche.messages.BindResponseMessage;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.DeployRequestMessage;
import dks.niche.messages.DeployResponseMessage;
import dks.niche.messages.RespondThroughBindingMessage;
import dks.niche.messages.SendThroughBindingMessage;
import dks.niche.messages.StartComponentMessage;
import dks.niche.wrappers.NicheOSSupportFork;
import dks.niche.wrappers.SimpleResourceManager;
import dks.test.niche.NicheServlet;

/**
 * The <code>NicheOSSupport</code> class
 * 
 * @author Joel
 * @version $Id: NicheOSSupport.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheOSSupport implements NicheManagementInterface,
		NicheAsynchronousInterface {

	public static final int MAX_CONCURRENT_OPERATIONS = 20;

	public static final int COLLOCATION_DESIRED = 1;

	public static final int COLLOCATION_REQUIRED = 2;

	public static final int BOOT = 1;

	public static final int JOINING = 0;

	// public static final int KEYSPACE =
	// registry.getRingMaintainerComponent().getDksParameters().N;

	//public static final int SENDER = 15555; // only for testing...

	static final String APPLICATION_TEST_SERVLET = System
			.getProperty("niche.test.servlet") instanceof String ? System
			.getProperty("niche.test.servlet") : null;

	static final int REPLICATION_DEGREE = System
			.getProperty("niche.replicationDegree") instanceof String ? Integer
			.parseInt(System.getProperty("niche.replicationDegree")) : 1;

	static final Properties testProperties = new Properties();

	private static final String THIS_PROGRAM_HAS_NO_OWNER = "NoName";

	DKSPropertyLoader propertyLoader;

	DKSParameters dksParameters;

	DKSRef dksRef = null; // for the joining node to find the ring

	DKSRef myDKSRef = null; // for the joining node to find the ring

	SimpleResourceManager myRM;

	// protected ComponentRegistry registry;
	//
	// protected Scheduler scheduler;
	//
	// NicheManagementContainerComponent myContainer;

	NicheNode node;

	BigInteger myRingId;

	int port;

	String bootNodeIp = null;

	String joinNodeIp = null;

	NicheActuatorInterface adlCruncherSupport;

	public NicheOSSupport(String givenId, int givenPort, int mode) {

		propertyLoader = new DKSPropertyLoader();

		dksParameters = (propertyLoader).getDKSParameters();

		DKSRef ref = null;
		InetAddress ip = null;

		// public SimpleResourceManager(NicheAsynchronousInterface logger,
		// String givenId, int givenPort, DKSParameters dksParameters,
		// int replicationDegree) {

		myRM = new SimpleResourceManager(this, givenId, givenPort,
				dksParameters, REPLICATION_DEGREE);

		this.myRingId = myRM.getId();
		System.out
				.println("System is starting up. The node has been asigned the id "
						+ myRingId);

		this.port = myRM.getPort();

		System.setProperty("niche.log.file", "./logs/niche-"
				+ myRingId.toString() + ".log4j");

		String propfile = System.getProperty("org.apache.log4j.config.xml");

		if(propfile==null) {
			PropertyConfigurator.configure(System.getProperty("org.apache.log4j.config.file"));
		} else {
			System.out.println("Configuring log4j using: " + propfile);
			DOMConfigurator.configure(propfile);
		}
		String webCacheAddress = propertyLoader.getWebcacheAddress();

		if (mode == BOOT) {

			try {
				ip = propertyLoader.getIP();// InetAddress.getByName(bootNodeIp);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ref = new DKSRef(ip, port, myRingId.abs());

		} else { // ==join

			try {
				ip = propertyLoader.getIP();// InetAddress.getByName(joinNodeIp);

				DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
						webCacheAddress);

				ref = new DKSRef(ip, port, myRingId.abs());

				/*
				 * this is needed for finding existing node(s)
				 */
				String rawDKSRef = dksCacheManager.getFirstDKSRef();
				dksRef = new DKSRef(rawDKSRef);

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		} // endifelse

		// /* Starting the ComponentRegistry */
		// registry = ComponentRegistry.init(dksParameters);
		//
		// /* Creating Scheduler */
		// scheduler = new Scheduler(registry);
		//
		// /* Creating the TimerComponent */
		// new TimerComponent(registry, scheduler);
		ArrayList<NicheServlet> testServlets = new ArrayList<NicheServlet>();

		// This is very ugly, plz break it out of here, as soon as someone
		// shows how to get the servlets running when added after the first
		// server start
		if (APPLICATION_TEST_SERVLET != null) {

			Class testServletClass;
			NicheServlet newServlet = null;

			try {
				testServletClass = Class.forName(APPLICATION_TEST_SERVLET);
				newServlet = (NicheServlet) testServletClass.newInstance();

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			newServlet.setProperties(testProperties);

			testServlets.add(newServlet);
			myRM.setTestProperties(testProperties);
		}
		
		System.out.println("propertyLoader.getWebcacheAddress(): " + propertyLoader.getWebcacheAddress());
		node = new NicheNode(
						this,
						ref,
						dksParameters,
						propertyLoader.getWebcacheAddress(),
						myRM,
						testServlets
		);

		// TESTING
		// FIXME
		// registerMEContainer();

		myDKSRef = node.
					getComponentRegistry().
						getRingMaintainerComponent().
							getMyDKSRef();
		
		myRM.setDKSRef(myDKSRef);
		myRM.setWebCacheAddress(webCacheAddress);
		myRM.setComponentRegistry(node.getComponentRegistry());
		node.serviceComponent.setResourceManager(myRM);

		myRM.startSupportThreads();
		
		// TODO: work in progress:
		// node.serviceComponent.publicTrigger(new
		// ResourceJoinEvent(myRM.getNodeRef()))

		// Keyspace =
		// node.getComponentRegistry().getRingMaintainerComponent().getDksParameters().N;
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF

	public void registerReceiver(Object receiverObject, String handlerMethod) {
		node.serviceComponent.registerReceiver(receiverObject, handlerMethod);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerResourceEnquiryHandler(Object resourceEnquiryHandler) {
		node.serviceComponent
				.registerResourceEnquiryHandler(resourceEnquiryHandler);
	}

	public void registerAllocationHandler(Object allocationHandler) {
		node.serviceComponent.registerAllocationHandler(allocationHandler);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerDeploymentHandler(Object deploymentHandler) {
		node.serviceComponent.registerDeploymentHandler(deploymentHandler);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerResourceEnquiryHandler(
			Object resourceEnquiryHandlerObject,
			String resourceEnquiryHandlerMethod) {
		node.serviceComponent.registerResourceEnquiryHandler(
				resourceEnquiryHandlerObject, resourceEnquiryHandlerMethod);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerDeploymentHandler(Object deploymentHandlerObject,
			String deploymentHandlerMethod) {
		node.serviceComponent.registerDeploymentHandler(
				deploymentHandlerObject, deploymentHandlerMethod);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerBindHandler(Object bindHandler) {
		node.serviceComponent.registerBindHandler(bindHandler);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerBindHandler(Object bindHandler, String methodName) {
		node.serviceComponent.registerBindHandler(bindHandler, methodName);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerDeliverHandler(Object deliverHandler) {
		node.serviceComponent.registerDeliverHandler(deliverHandler);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerDeliverHandler(Object deliverHandler, String methodName) {
		node.serviceComponent
				.registerDeliverHandler(deliverHandler, methodName);
	}

	//
	// //MANAGEMENT&RECEIVER-REGISTRATION STUFF
	// public void registerMEReceiver(Object deliverHandler, String methodName)
	// {
	// node.communicationComponent.registerReceiver(deliverHandler, methodName);
	// }
	// //MANAGEMENT&RECEIVER-REGISTRATION STUFF
	// public void registerManagementEventReceiver(Object deliverHandler, String
	// methodName) {
	// node.communicationComponent.registerManagementEventReceiver(deliverHandler,
	// methodName);
	// }
	// //MANAGEMENT&RECEIVER-REGISTRATION STUFF
	// public void registerRequestReceiver(Object deliverHandler, String
	// methodName) {
	// node.communicationComponent.registerReceiver(deliverHandler, methodName);
	//		
	// }
	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void setResourceManager(SimpleResourceManager rm) {
		node.serviceComponent.setResourceManager(rm);
	}

	// MANAGEMENT&RECEIVER-REGISTRATION STUFF
	public void registerManagementElement(int replicaNumber, int flag,
			ManagementElementInterface mei) {
		node.container.registerManagementElement(
				replicaNumber,
				flag,
				mei.getId(),
				mei
		);
	}

	//

	// MANAGEMENT STUFF

	// MANAGEMENT STUFF - BOOT

	public void boot() {
		System.out.println("Starting Niche ...");
		System.out.println("First node. Creating a ring...");
		node.getDksImplementation().create();
		System.out.println("Done creating");

	}

	// MANAGEMENT STUFF - JOIN

	public void join() {

		// System.out.println("Joining ring using node " + dksRef + "...");
		node.getDksImplementation().join(dksRef);
		System.out.println("Done joining");
	}

	// MANAGEMENT STUFF - LEAVE
	/**
	 * Initializes a graceful leave, which allow ongoing operations to complete,
	 * and moves DHT-data from the machine to the network
	 * 
	 */
	public void leave() {

		System.out
				.println("Leaving the ring, handing over responsibilities. Please hold");
		node.getDksImplementation().leave();
		System.out.println("Done leaving");
		// node.getComponentRegistry().getRingMaintainerComponent().
	}

	// MANAGEMENT STUFF - DESTROY

	public void destroy() {
		// TODO Auto-generated method stub

	}

	// ALLES

	public void trigger(Event e) {
		node.serviceComponent.publicTrigger(e);
	}

	// ACTUATION STUFF

	// ACTUATION STUFF - DISCOVER

	// COMMUNICATION STUFF

	public void requestFromManagement(NicheId destination,
			Serializable requestMessage, NicheNotifyInterface initiator) {
		// TODO farligt farligt, no type checking
		// if(myRM.hasLocation(destination)) {
		// //deliver locally
		// //SHOULD NOT BLOCK!!!
		// node.serviceComponent.publicTrigger(new
		// RequestFromManagementEvent(requestMessage, initiator));
		// }
		// else {
		// node.serviceComponent.publicTrigger();
		// }

		BigInteger ringDestination = myRM.dcmsCacheGet(destination);
		SendRequestEvent sre;

		int type = SendRequestEvent.REQUEST_MESSAGE | SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_ID; 
		
		if (ringDestination != null) {

			sre = new SendRequestEvent(
					ringDestination,
					requestMessage,
					type
			);

		} else {

			sre = new SendRequestEvent(
					new BigInteger(destination.getLocation()),
					requestMessage,
					type
				);
		}

		sre.setInitiator(initiator);
		node.serviceComponent.publicTrigger(sre);

	}

	// COMMUNICATION STUFF

	public void sendToManagement(DKSRef destination, NicheId destinationId,
			Message message) {
		if (myDKSRef.equals(destination)) {
			// deliver locally
			// SHOULD NOT BLOCK!!!
			node.serviceComponent.publicTrigger(new DeliverToManagementEvent(
					message));
		} else {

			node.serviceComponent.publicTrigger(
					new SendRequestEvent(
							destination,
							new BigInteger(destinationId.getLocation()),
							message, 
							null,
							null,
							(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_NODE) 
							)
					);
		}
	}

	// COMMUNICATION STUFF

	public void sendToManagement(NicheId destination, Message sourceMessage,
			boolean useReplication) {

		BigInteger replicaZeroDestination = new BigInteger(destination
				.getLocation());

		/*#%*/ String logMessage = "I'm sending a "
		/*#%*/ 					+ sourceMessage.getClass().getSimpleName()
		/*#%*/ 					+ " to the destination with id "
		/*#%*/ 					+ destination.toString();
		
		if (!useReplication) {
			
			/*#%*/ logMessage += " - one copy only, no replication";
			
			Message message = sourceMessage instanceof ReplicableMessageInterface ?
						((ReplicableMessageInterface) sourceMessage).getReplicaCopy(0, replicaZeroDestination) :
						sourceMessage;

						//if using sourceMessage, the destinationRingId will be null
			node.serviceComponent.publicTrigger(
					new SendRequestEvent(
							replicaZeroDestination,
							message,
							(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_ID)
					)
			);

		} else if (!(sourceMessage instanceof ReplicableMessageInterface)){
			
			/*#%*/ logMessage += " - one copy only, no replication"; 
				
			/*#%*/ if(sourceMessage instanceof BindRequestMessage) {
			/*#%*/ 		logMessage += " - BindRequestMessages are not replicated";
			/*#%*/ } else {
			/*#%*/ 		logMessage += ", although there should have been replication used!!";
			/*#%*/		System.err.println(logMessage);
			/*#%*/ }
			
			
			Message message = sourceMessage;

						//if using sourceMessage, the destinationRingId will be null
			node.serviceComponent.publicTrigger(
					new SendRequestEvent(
							replicaZeroDestination,
							message,
							(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_ID)
					)
			);

			
		} else {

			BigInteger destinationRingId;
			/*#%*/ logMessage += " - " + REPLICATION_DEGREE + " copies, going to\n";
			for (int replicaNumber = 0; replicaNumber < REPLICATION_DEGREE; replicaNumber++) {

				// if you send the same msg to all nodes, u will
				// have the case where u send one (or more) copies
				// to yourself. If u then set the replicaNumber
				// "upon arrival" of the new DelegationMessage
				// this can happen before it is actually sent
				// to the other replicas in the group...
				// how to circumvent without doing manual
				// clone?
				
				destinationRingId = myRM.getSymmetricId(
										replicaZeroDestination,
										replicaNumber
									);
				
				/*#%*/ logMessage += "Replica " + replicaNumber + " going to node responsible for " + destinationRingId + "\n";
				
				node.serviceComponent.publicTrigger(
						new SendRequestEvent(
								destinationRingId,
								((ReplicableMessageInterface) sourceMessage).getReplicaCopy(replicaNumber, destinationRingId),
								(SendRequestEvent.SEND_TO_MANAGEMENT | SendRequestEvent.SEND_TO_ID)
							)
					);
				
			}
		}
		/*#%*/ node.container.log(logMessage);
	}

	// COMMUNICATION STUFF

	public void sendToNode(DKSRef destination, Message message) {
		sendToNode(destination, destination.getId(), message);
	}

	public void sendToNode(DKSRef destination, NicheId destinationId,
			Message message) {
		sendToNode(destination, new BigInteger(destinationId.getLocation()),
				message);
	}

	private void sendToNode(DKSRef destination, BigInteger destinationId,
			Message message) {
		message.setSource(myDKSRef); // okok, might be a little ugly but
		// source has to be present

		if (myDKSRef.equals(destination)) {
			// deliver locally
			// SHOULD NOT BLOCK!!!
			node.serviceComponent
					.publicTrigger(new DeliverToNodeEvent(message));
		} else {
			node.serviceComponent.publicTrigger(new SendRequestEvent(
					destination, destinationId, message, null, null,
					SendRequestEvent.SEND_TO_NODE));
		}
	}

	public void sendToNode(DKSRef destination,
							NicheId destinationId,
							Message message,
							NicheNotifyInterface returnValueReceiver,
							MessageManagerInterface messageManager) {
		
		message.setSource(myDKSRef); // okok, might be a little ugly but
		// source has to be present

		int type = 0;
		
		if (
				(message instanceof AllocateRequestMessage)
				||			
				(message instanceof AllocateResponseMessage) 
				||
				(message instanceof DeployRequestMessage)
				||
				(message instanceof DeployResponseMessage)
				||
				(message instanceof BindRequestMessage)
				||
				(message instanceof BindResponseMessage)
				||
				(message instanceof SendThroughBindingMessage)
				||
				(message instanceof RespondThroughBindingMessage)
				||
				(message instanceof StartComponentMessage)
		) {
			type = type | SendRequestEvent.SEND_TO_NODE;
			
		} else	if (
				(message instanceof DelegationRequestMessage)
				
		) {
			type = type | SendRequestEvent.SEND_TO_MANAGEMENT;
		} else {
			String errMsg = "OBS: Trying to send a message of type " + message.getClass().getSimpleName() + " please handle!";
			System.err.println(errMsg);
			
		}

		
		if(returnValueReceiver != null) {
			type = type | SendRequestEvent.REQUEST_MESSAGE;  
		}
		
		node.serviceComponent.publicTrigger(
				new SendRequestEvent(
								destination,
								new BigInteger(destinationId.getLocation()),
								message,
								returnValueReceiver,
								messageManager,
								type
								)
				);
		
	}

	// COMMUNICATION STUFF

//	public void send(Object localBindId, Object message) {
//		node.serviceComponent.send(localBindId, message);
//	}
//
//	public void send(Object localBindId, Object message, ComponentId destination) {
//		node.serviceComponent.send(localBindId, message, destination);
//	}

	public void sendWithReply(Object localBindId, Serializable message,
			ComponentId destination, NicheNotifyInterface replyReceiver) {
		node.serviceComponent.send(localBindId, message, destination,
				replyReceiver);
	}
	
	public void sendWithSendAck(Object localBindId, Serializable message,
			ComponentId destination, NicheNotifyInterface replyReceiver) {
		node.serviceComponent.send(localBindId, message, destination,
				replyReceiver);
	}

	// TIMER STUFF

	public long registerTimer(EventHandlerInterface receiver,
			Class eventClass, int timeout) {
		return node.container.registerTimer(receiver, eventClass, null, timeout);
	}
	public long registerTimer(EventHandlerInterface receiver,
			Class<Event> eventClass, Object attachment, int timeout) {
		return node.container.registerTimer(receiver, eventClass, attachment, timeout);
	}

	public void cancelTimer(long timerId) {
		node.container.cancelTimer(timerId);
	}

	// LOG STUFF

	/*#%*/ 	public void log(String message) {
	/*#%*/ 		node.container.log(message);
	/*#%*/ 	}

	// EXECUTE 
	
	public void publicExecute(Runnable task) {
		node.container.publicExecute(task);
	}
	// GETTERS

	// DYNAMIC GETTERS

//	public NicheActuatorInterface getNicheActuator() {
//		return new NicheOSSupportFork(
//				(NicheAsynchronousInterface) this,
//				null,
//				false);
//	}

	public NicheActuatorInterface getNicheActuator(NicheId id) {
		return new NicheOSSupportFork(
				this,
				id,
				null,
				false);
	}
	
//	public NicheActuatorInterface getNicheActuator(NicheId id, TriggerInterface proxy) {
//		return new NicheOSSupportFork(
//				(NicheAsynchronousInterface) this,
//				id,
//				false);
//	}

	public NicheAsynchronousInterface getNicheAsynchronousSupport() {
		return this;
	}

	public NicheActuatorInterface getJadeSupport() {
		return getJadeSupport(THIS_PROGRAM_HAS_NO_OWNER);
	}
	public NicheActuatorInterface getJadeSupport(String owner) {
		if (adlCruncherSupport == null) {
			adlCruncherSupport =
				new NicheOSSupportFork(
					this,
					null,
					owner,
					true //blocking? non-blocking?
				);
			}
		return adlCruncherSupport;
	}

//	public NicheComponentSupportInterface getComponentSupport() {
//		return new NicheOSSupportFork(this, null, THIS_PROGRAM_HAS_NO_OWNER, true); // FIXME
//	}
	public NicheActuatorInterface getComponentSupport(Object component) {
		
		//ComponentId cid = myRM.getComponentId(component);
		NicheActuatorInterface nicheActuatorInterface = myRM.getNicheActuatorInterface(component);
		if(null == nicheActuatorInterface) {
			System.err.println("You must use a valid component while requesting an component-support instance");
			return null;
		}
		
		return nicheActuatorInterface;
		
//		return new NicheOSSupportFork(
//				this,
//				cid.getId(),
//				null,
//				true
//		); // FIXME
	}

	/**
	 * @return
	 */

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheAsynchronousInterface#getNicheId(dks.niche.ids.NicheId, java.lang.String, int, boolean)
	 */
	@Override
	public NicheId getNicheId(NicheId locationId, String owner, int type,
			boolean reliable) {
		
		return myRM.getNicheId(locationId, owner, type, reliable);
	}

//	public NicheId getCloseNodeId(DKSRef nodeOfRef, NicheId creatingEntity, int type, boolean reliable) {
//		return myRM.getNicheId(locationId, owner, type, reliable)
//	}
//	public NicheId getCloseNodeId(DKSRef nodeOfRef) {
//		return myRM.getSuccessorNodeId(nodeOfRef);
//	}
//	public NicheId getCloseNodeId(NicheId nicheId) {
//		return myRM.getSuccessorNodeId(nicheId);
//	}
//	public NicheId getNextNodeId() {
//		return myRM.getSuccessorNodeId();
//	}
//
//	public NicheId getSameNodeId(DKSRef nodeOfRef) {
//		return myRM.getSameNodeId(nodeOfRef);
//	}
//
//	public NicheId getUniqueCollocatedId(NicheId location) {
//		return myRM.getUniqueCollocatedId(location);
//	}
//
//	public NicheId getLocalId() {
//		return myRM.getLocalId();
//	}
//
//	public NicheId getUncheckedUniqueId() {
//		return myRM.getUncheckedUniqueId();
//	}

//	public NodeRef getMyNodeRef() {
//		return myRM.getNodeRef();
//	}
	// public void requestId(NicheNotifyInterface initiator) {
	//
	//
	// }
	//	
	// public void requestStableId(NicheNotifyInterface initiator) {
	//		
	// //OBS! no checking here, the lookup will return whoever is currently
	// resp. for the ringId, regardless of whether the stable node is present or
	// not!
	//		
	// NicheId newId = myRM.getStableNodeId();
	// BigInteger ringDestination = new BigInteger(newId.getLocation());
	//		
	// SendRequestEvent sre;
	//
	// sre = new SendRequestEvent(ringDestination, newId,
	// SendRequestEvent.REQUEST_ID);
	//
	// sre.setInitiator(initiator);
	// node.serviceComponent.publicTrigger(sre);
	//		
	// }

	// STATIC GETTERS

	public SimpleResourceManager getResourceManager() {
		return myRM;
	}

	public NicheOverlayServiceComponent getDirectOverlayAccess() {
		return node.serviceComponent;
	}

	
	// PRIVATE??
	// private ComponentRegistry getComponentRegistry() {
	// return registry;
	// }
	//
	// private Scheduler getScheduler() {
	// return scheduler;
	// }
	//	
	// private NicheNode getNode() {
	// return node;
	// }

	/*
	 * Testing
	 */
	// public void setCurrentNodes(DKSRef[] currentNodes) {
	// node.container.setCurrentNodes(currentNodes);
	// //myNicheOSSIC.setCurrentNodes(currentNodes);
	//		
	// }
}
