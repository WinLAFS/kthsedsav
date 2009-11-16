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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.sound.midi.SysexMessage;

import dks.addr.DKSRef;
import dks.niche.exceptions.DestinationUnreachableException;
import dks.niche.exceptions.OperationTimedOutException;
import dks.niche.ids.BindId;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.GetReferenceMessage;
import dks.niche.messages.SendThroughBindingMessage;

/**
 * The <code>bindId</code> class
 * 
 * @author Joel
 * @version $Id: bindId.java 294 2006-05-05 17:14:14Z joel $
 */
public class ClientSideBindStub implements Serializable, IdentifierInterface, MessageManagerInterface {

	/**
	 * @serialVersionUID -
	 */
	
	//This is general bookkeeping stuff:
	
	private static final long serialVersionUID = 2227322099838937626L;

	//private final static int MAX_CONCURRENT_OPERATIONS = 10;

	final static int RETRY_DELAY = 5000;
	
	NicheAsynchronousInterface niche;
	
	//protected int replicaNumber;
	//protected int type;
	
	//Below are fields which are valid at the bindId-host-node:
	
	BindId bindId;
	String myTypeString;
	boolean oneToOne;

	//Below are fields which are ONLY when the bindId is used cached by a SNRElement:
	
	//protected NicheId myDestinationUserId;
	final int MAX_CONCURRENT_OPERATIONS = 10;

	transient HashMap<String, IdentifierInterface> cache;

	transient Object[] cacheArray;

	transient Random myRandom;

	protected int lastSeenOperationId = 0;

	transient private Object synchronizedObject = new Object();

	transient private Object waitForResults = new Object();

	private Object waitForSynchronousReturnValue = "hejhej";

	private DKSRef dksRef;
	
	/*
	 * 
	 * Here starts "run" business!!
	 * 
	 */
	
	boolean running = false;
	boolean sendFailure = false; 

	ArrayList<BindSendJob> waitingMessages;
	HashMap<Integer, BindSendJob> storedJobs = new HashMap<Integer, BindSendJob>(NicheSendClass.MESSAGE_QUEUE / 4);
	//ComponentId receiver = null;
	
	//NicheNotifyInterface returnValueReceiver;
	
	private final int FETCH_ONLY_ONE =
		System.getProperty("niche.bindings.fetchOnlyOne") instanceof String ?
				Integer.parseInt(System.getProperty("niche.bindings.fetchOnlyOne"))
			:
				0;

	private final int CAP = 
		System.getProperty("niche.bindings.cap") instanceof String ?
				Integer.parseInt(System.getProperty("niche.bindings.cap"))
			:
				Integer.MAX_VALUE;

	public static final	int	OPERATION_TIMEOUT =
		System.getProperty("niche.operationTimeout") instanceof String ?
			Integer.parseInt(System.getProperty("niche.operationTimeout"))
		:
			100000 //close to inf
	;	

	final static int BIND_RETRY_DELAY = 1000;
	

	// EMPTY CONSTRUCTOR NEEDED FOR DYNAMIC CREATION
	public ClientSideBindStub() {
		myRandom = new Random();
		waitingMessages = new ArrayList<BindSendJob>();
	}	
	public ClientSideBindStub(NicheAsynchronousInterface niche, BindId bindId) {
		this.niche = niche;
		this.bindId = bindId;
		this.myRandom = new Random();
		waitingMessages = new ArrayList<BindSendJob>();
		
		/*#%*/ niche.log("ClientBindStub says: Created stub with id " + bindId.getId() + (CAP < Integer.MAX_VALUE ? " and a use-cap of " + CAP : ""));
		switch (getBasicType()) {

			case JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE:
			case JadeBindInterface.ONE_TO_ONE:
				oneToOne = true;
				myTypeString = "one-to-one";								
				break;
	
			case JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE:				
			case JadeBindInterface.ONE_TO_ANY:
	
				myTypeString = "one-to-any";
				break;
				
			case JadeBindInterface.ONE_TO_MANY:
				
				myTypeString = "one-to-many";
				break;
				
			default: 
				System.err.println("Unsupported bind type, please fix");
			/*#%*/ niche.log("Unsupported bind type, please fix");
				break;
		} // End switch
		
		this.dksRef = niche.getResourceManager().getDKSRef();
	}	

		

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return bindId.getType();
	}

	public int getBasicType() {
		return bindId.getType() & JadeBindInterface.BASIC_TYPES;
	}


	/**
	 * @param id
	 */
//	public void setId(NicheId id) {
//		this.myId = id;
//
//	}


//	public HashMap getCache() {
//		return cache;
//	}
//
//	public void setCache(HashMap c) {
//		this.cache = c;
//	}

//	public IdentifierInterface getAny(int fetchOnlyOneFlag) {
//		// if(cache == null) {
//		// TODO: determine retry-policy. now (potentially) looping forever!
//		fillCache(bindId.getType(), fetchOnlyOneFlag);
//		cacheArray = cache.values().toArray();
//		while (cacheArray.length < 1) {
//			niche.log("Bind says: no receiver found, sleeping / looping");
//			try {
//				Thread.sleep(RETRY_DELAY);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			fillCache(bindId.getType(), fetchOnlyOneFlag);
//			cacheArray = cache.values().toArray();
//		}
//		return (IdentifierInterface) cacheArray[((int) (myRandom.nextFloat() * cacheArray.length))];
//	}
//
//	public Object[] getAll() {
//		// if(cache == null) {
//		fillCache(bindId.getType(), 0);
//		cacheArray = cache.values().toArray();
//		return cacheArray;
//	}

	// public SNRElement get(IdentifierInterface receiver) {
	// //if(cache == null) {
	// //TODO: policy for this:
	// if(receiver instanceof ComponentId) {
	// return ((ComponentId)receiver);
	// }
	// fillCache(0);
	// return (SNRElement)cache.get(receiver.toString());
	// }

	private synchronized void fillCache(int operationId) { //int mask, int anyFlag) {

		int flag = GetReferenceMessage.GET_ALL;
//		if( (mask & JadeBindInterface.ONE_TO_ANY & anyFlag) != 0) {
//			flag = GetReferenceMessage.GET_ANY;
//		}
		synchronizedObject = new Object();
		NicheId receiverId = bindId.getReceiver().getId();
		do {
			waitForResults = waitForSynchronousReturnValue;

			// TODO: counterpart is now treated statically and not resolved from
			// the remote bindId-site: it should only be done this way if the binding is static
			
			niche.requestFromManagement(
					receiverId,
					new GetReferenceMessage(receiverId, flag),
					new NicheNotify(this, operationId)
			);

			myWait(operationId);

		} while (waitForResults instanceof String);

		
		if(waitForResults instanceof HashMap) {
			cache = (HashMap<String, IdentifierInterface>) waitForResults;
			
		} else { //we assume X
			cache.put(receiverId.toString(), (IdentifierInterface)waitForResults);
		}

				
//		if ((mask & JadeBindInterface.NO_SEND_TO_SENDER) != 0) {
//			if (cache.remove(bindId.getSender().getId().toString()) != null) {
//				niche.log("BindStub for " + bindId.getId()
//						+ " says: removing reference to self");
//			}
//		}

	}


	public void notify(int statusOrOperationId, Object result) {

		switch(statusOrOperationId) {
			case MessageManagerInterface.CHANNEL_ERROR:

				sendFailure = true;
				int failedJobId = ((SendThroughBindingMessage)result).getOperationId();
												
				if(getBasicType() == JadeBindInterface.ONE_TO_ONE
						||
				   getBasicType() == JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE) {
					//time to generate an exception
					storedJobs.get(failedJobId).exceptionsAndReplyHandler.notify(new DestinationUnreachableException());					
					return;
				}
				
				if(getBasicType() == JadeBindInterface.ONE_TO_MANY) {
					//Do nothing, don't treat it as an failure, but
					//accept that the failed member will not get the invocation
					sendFailure = false;
					return;
				}
				
				synchronized (waitingMessages) {
					waitingMessages.add(storedJobs.get(failedJobId));
				}	
				
			break;
			case MessageManagerInterface.SUCCESSFULLY_SENT:
				
				int jobId = ((SendThroughBindingMessage)result).getOperationId();				
				/*#%*/ niche.log("Bind says: Notifying on send-success: " + jobId);
				storedJobs.get(jobId).exceptionsAndReplyHandler.notify(true);
				
				
			break;
			default:
				synchronized (synchronizedObject) {
					waitForResults = result;
					synchronizedObject.notify();
				}
				break;
		}//end switch
	}

	public NicheId getId() {
		return bindId.getId();
	}



//	protected void prepareWait() {
//		operationId = (operationId + 1) % MAX_CONCURRENT_OPERATIONS;
//		waitForResults = waitForSynchronousReturnValue;
//	}

//	protected NicheId requestId(boolean unique, boolean mustBeStable) {
//		NicheId newId;
//		boolean isStable = false;
//
//		do {
//			if (mustBeStable) { // for now. later: description.keepAlive()) {
//				if (unique) {
//					newId = niche.getResourceManager().getUncheckedStableId();
//				} else {
//					newId = niche.getResourceManager()
//							.getUncheckedStableLocation();
//				}
//			} else {
//				if (unique) {
//					newId = niche.getResourceManager().getUncheckedUniqueId();
//				} else {
//					newId = niche.getResourceManager().getUncheckedLocation();
//				}
//			}
//
//			BigInteger ringDestination = new BigInteger(newId.getLocation());
//
//			SendRequestEvent sre;
//
//			sre = new SendRequestEvent(ringDestination, newId,
//					SendRequestEvent.REQUEST_MESSAGE);
//			//TODO: should be REQUEST_ID
//
//			NicheId checkedId;
//			synchronized (this) {
//				prepareWait();
//				sre.setInitiator(new NicheNotify(this, operationId));
//				niche.trigger(sre);
//				myWait(operationId);
//				checkedId = (NicheId) waitForResults;
//			}
//			if (mustBeStable && !newId.isCollocated(checkedId)) {
//				isStable = false;
//				niche
//						.log("Bind says: chosen stable node not present, retrying a different one");
//			} else {
//				isStable = true;
//				newId = checkedId;
//			}
//		} while (!isStable);
//
//		return newId;
//
//	}

	protected void myWait(int operationId) {

		long startMillis = System.currentTimeMillis(), waitMillis = 0;

		synchronized (synchronizedObject) {
			/*#%*/ niche.log("Bind says: Entering critical section: " + operationId
			/*#%*/ 		+ " mySyncObj: " + synchronizedObject);
			
			while (waitForResults.equals(waitForSynchronousReturnValue)
					&&
					waitMillis < OPERATION_TIMEOUT
					) {
				
				try {
					synchronizedObject.wait(OPERATION_TIMEOUT);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				waitMillis = System.currentTimeMillis() - startMillis;
			} // 
							
			/*#%*/ if(waitForResults.equals(waitForSynchronousReturnValue)) {			
			/*#%*/ niche.log("Bind says: Operation " + operationId + " timed out");
			/*#%*/ System.err.println("Bind says RETRYING " + operationId);
			/*#%*/ }
			
			/*#%*/ niche.log("Bind says: Exiting critical section: " + operationId);
		}
		// }
	}

	public DelegationRequestMessage transfer(int mode) {
		System.err.println("Right now stubs cannot move...");
		return new DelegationRequestMessage(
				//FIXME
			);
	}


//	public boolean isReliable() {
//		return 0 <= replicaNumber;
//	}

	public Object getSenderSideInterfaceDescription() {
		return bindId.getSenderSideInterfaceDescription();
	}

	public Object getReceiverSideInterfaceDescription() {
		return bindId.getReceiverSideInterfaceDescription();
	}

	public void setReceiverSideInterfaceDescription(
			Object receiverSideInterfaceDescription) {
		bindId.setReceiverSideInterfaceDescription(receiverSideInterfaceDescription);
	}

	public void setSenderSideInterfaceDescription(
			Object senderSideInterfaceDescription) {
		bindId.setSenderSideInterfaceDescription(senderSideInterfaceDescription);
	}

//	public void setReplicaNumber(int replicaNumber) {
//		this.replicaNumber = replicaNumber;
//	}
//	public int getReplicaNumber() {
//		return this.replicaNumber;
//	}

	/*
	 * 
	 * Here starts "run" business!!
	 * 
	 */
	
	public void prepareSending(Object message, int operationId, NicheNotifyInterface replyHandler, ComponentId shortcut) {
		
		if(shortcut != null) {
			
			DKSRef temp = shortcut.getResourceRef().getDKSRef();
			
			/*#%*/ niche.log("ClientBindStub says: Op " + operationId + " on bindId " + bindId.getId()
			/*#%*/ 		+ " is shortcutted used to send a message to "
			/*#%*/ 		+ shortcut.getId() + " hosted by "
			/*#%*/ 		+ temp
			/*#%*/ );

			storedJobs.put(operationId, new BindSendJob(message, operationId, replyHandler));
			
			niche.sendToNode(
					temp,
					shortcut.getId(),
					new SendThroughBindingMessage(
							shortcut.getId(),
							bindId,
							message,
							operationId
					),
					replyHandler,
					this
			);
			
		} else {
			
			/*#%*/ niche.log("ClientBindStub says: Op " + operationId + " on bindId " + bindId.getId()
			/*#%*/ 		+ " is put in the queue"
			/*#%*/ );

			lastSeenOperationId = operationId;
			synchronized (waitingMessages) {
				waitingMessages.add(new BindSendJob(message, operationId, replyHandler));
			}

			
		}
	}
		
//	public void setOperationId(int operationId) {
//		this.operationId = operationId;
//	}
	
	public void run() {
		
		if(sendFailure) {
			
			sendFailure = false;
			running = false;
			
			/*#%*/ niche.log("ClientBindStub says: message on "+ bindId.getId() +" could NOT be delivered to a proper receiver, sleep " + BIND_RETRY_DELAY + " & retry");
				try {
					Thread.sleep(BIND_RETRY_DELAY);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*#%*/ niche.log("ClientBindStub says: Time to retry sending message on "+ bindId.getId() +"!");	
		} 
		
		if (!running) {
			
			if(waitingMessages.size() < 1) {
				/*#%*/ niche.log("ClientBindStub says: "+ bindId.getId() +" was activated, but had no messages to process"); 

			} else {		
				
				running = true;
				
				if(getBasicType() == JadeBindInterface.ONE_TO_ONE
						||
				   getBasicType() == JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE) {
					
					//do nothing, now. later we should check whether the single receiver is
					//movable or not
					cacheArray = new Object[] { "dummy" };
					
				} else {
					
					/*#%*/ niche.log(
					/*#%*/ 	"ClientBindStub says: "
					/*#%*/ 		+ myTypeString
					/*#%*/ 		+ " message on "
					/*#%*/ 		+ bindId.getId()
					/*#%*/ 		+ " is about to be processed - fetching references from "
					/*#%*/ 		+ bindId.getReceiver().getId().toString()
					/*#%*/ );
					
					fillCache(lastSeenOperationId);
					cacheArray = cache.values().toArray();
					
					while (cacheArray.length < 1) {
						/*#%*/ niche.log("ClientBindStub says: "+ bindId.getId() +" had no receivers, sleep and retry");
						try {
							Thread.sleep(RETRY_DELAY);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						fillCache(lastSeenOperationId);
						cacheArray = cache.values().toArray();
					}
					//after reaching this, we know we have receivers to send to! so process all messages in the queue
				}
				// if we wouldn't use while here, messages might be given to the stub between we do
				// "waitingMessages.toArray()" and we are done processing them
				while(0 < waitingMessages.size()) {
					
					Object[]readyMessages;
					synchronized (waitingMessages) {
						readyMessages = waitingMessages.toArray();
						waitingMessages.clear();
					}
					
					/*#%*/ String logMessage =
					/*#%*/ 	"ClientBindStub says: "
					/*#%*/ 	+ bindId.getId()
					/*#%*/ 	+ " had "
					/*#%*/ 	+ cacheArray.length
					/*#%*/ 	+ " receivers, use them to send " +
					/*#%*/ 	+ readyMessages.length
					/*#%*/ 	+ " "
					/*#%*/ 	+ myTypeString
					/*#%*/ 	+ " messages ";				
					
					ComponentId destination;
					BindSendJob sendJob;
					
					//Store, to be able to resend
					for (Object job : readyMessages) {						
						sendJob = (BindSendJob)job;						
						storedJobs.put(sendJob.operationId, sendJob);						
					}
					
					switch (getBasicType()) {
		
					case JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE:
					case JadeBindInterface.ONE_TO_ONE:
	
						for (Object job : readyMessages) {
							
							sendJob = (BindSendJob)job;
							
							destination = (ComponentId)bindId.getReceiver();
							
							/*#%*/ logMessage += "\nto " + destination.getId() + " hosted by " + destination.getResourceRef().getDKSRef();
						
							niche.sendToNode(
									destination.getResourceRef().getDKSRef(),
									destination.getId(),
									new SendThroughBindingMessage(
											destination.getId(),
											bindId,
											sendJob.message,
											sendJob.operationId
									),
									((BindSendJob)job).exceptionsAndReplyHandler,
									this
								);
							                                               
						}
						
						break;
			
					case JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE:				
					case JadeBindInterface.ONE_TO_ANY:
			
						
						for (Object job : readyMessages) {
							
							sendJob = (BindSendJob)job;
							
							destination = pickDestination();
							
							/*#%*/ logMessage += "\nto " + destination.getId() + " hosted by " + destination.getResourceRef().getDKSRef();
							
							niche.sendToNode(
									destination.getResourceRef().getDKSRef(),
									destination.getId(),
									new SendThroughBindingMessage(
											destination.getId(),
											bindId,
											sendJob.message,
											sendJob.operationId
									),
									((BindSendJob)job).exceptionsAndReplyHandler,
									this
								);
							                                               
						}
						
						break;
						
					case JadeBindInterface.ONE_TO_MANY:
						
						for (Object job : readyMessages) {
							
							sendJob = (BindSendJob)job;
							
							if (cacheArray.length < JadeBindInterface.DIRECT_SEND_THRESHOLD) {						
							
								for (int i = 0; i < cacheArray.length; i++) {
									
									destination = (ComponentId)cacheArray[i];
									/*#%*/ logMessage += "\nto " + destination.getId() + " hosted by " + destination.getResourceRef().getDKSRef();
									
									niche.sendToNode(
											destination.getResourceRef().getDKSRef(),
											destination.getId(),
											new SendThroughBindingMessage(
													destination.getId(),
													bindId,
													sendJob.message,
													sendJob.operationId
											),
											((BindSendJob)job).exceptionsAndReplyHandler,
											this
									);
		
								}
							} else {
								// Now the same, should be done constructing a broadcast
								// call!
								for (int i = 0; i < cacheArray.length; i++) {
									
									destination = (ComponentId)cacheArray[i];
									/*#%*/ logMessage += "\nto " + destination.getId() + " hosted by " + destination.getResourceRef().getDKSRef();
									
									niche.sendToNode(
											destination.getResourceRef().getDKSRef(),
											destination.getId(),
											new SendThroughBindingMessage(
													destination.getId(),
													bindId,
													sendJob.message,
													sendJob.operationId
											),
											((BindSendJob)job).exceptionsAndReplyHandler,
											this

									);
		
								}
							}//end if-else
						} //end for-job-loop
						
						break;
						
					default: 
						System.err.println("Unsupported bind type, please fix");
					/*#%*/ niche.log("Unsupported bind type, please fix");
						break;
				} // End switch
		
					/*#%*/ niche.log(logMessage);
			} //end while
			
			running = false;
			} //end else
			
		} //end if running			
		/*#%*/  else {
		/*#%*/ 	niche.log("ClientBindStub says: "+ bindId.getId() +" was activated, but is already running");
		/*#%*/ }
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheNotifyInterface#notify(java.lang.Object)
	 */
	public void notify(Object message) {
		
		sendFailure = (Boolean)message;
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.IdentifierInterface#getId()
	 */
	public String getMessageManagerId() {
		return bindId.getId().toString();
	}
	
	public boolean invokeOnIdError() {
		return true;
	}

	public boolean invokeOnNodeError() {
		return true;
	}
	
	public boolean invokeOnSendSuccess() {
		//if it is a one way binding it will block until the message is successfully sent
		//if it is a two way binding it will block until the return value arrives
		return (bindId.getType() & JadeBindInterface.WITH_RETURN_VALUE) == 0;
	}
	
	private ComponentId pickDestination() {
		int cap = cacheArray.length < CAP ? cacheArray.length : CAP; 
		return (ComponentId) cacheArray[((int) (myRandom.nextFloat() * cap))];
	}
//	private void iterate() {
//		//int operationId = -1;
//		DKSRef temp;
//
//		switch (getBasicType()) {
//
//		case JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE:
//		case JadeBindInterface.ONE_TO_ONE:
//
//			if(bindId.getReceiver() instanceof ComponentId && ((ComponentId)bindId.getReceiver()).getResourceRef() != null) {
//				
//					temp = ((ComponentId)bindId.getReceiver()).getResourceRef().getDKSRef();
//					
//					niche.log("ClientBindStub says: Op " + operationId + " bindId " + bindId.getId()
//							+ " is used to send a one-to-one message to "
//							+ bindId.getReceiver().getId() + " hosted by "
//							+ temp);
//
//	
//					
//				} else { //fetch from id
//
//					niche.log("ClientBindStub says: Op "
//							+ operationId +
//							" References for bindId "
//							+ bindId.getId()
//							+ " are being fetched from " 
//							+ bindId.getReceiver().getId().toString()
//							+ " before sending");
//
//
//					IdentifierInterface destination = getAny(FETCH_ONLY_ONE);
//					temp = destination.getId().getDKSRef();
//					
//					niche.log("ClientBindStub says: Op " + operationId + " bindId " + bindId.getId()
//							+ " is used to send a message to "
//							+ destination.getId() + " hosted by "
//							+ temp
//					);						
//				}
//				
//				niche.sendToNode(
//						temp,
//						bindId.getReceiver().getId(),
//						new SendThroughBindingMessage(
//								bindId.getReceiver().getId(),
//								bindId,
//								message,
//								operationId
//						),
//						returnValueReceiver,
//						this
//				);
//
//				
//			break;
//
//		case JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE:				
//		case JadeBindInterface.ONE_TO_ANY:
//
//			ComponentId destination;
//
//				niche.log("ClientBindStub says: Op "
//						+ operationId +
//						" References for bindId "
//						+ bindId.getId()
//						+ " are being fetched from " 
//						+ bindId.getReceiver().getId().toString()
//						+ " before one-to-any sending");
//
//
//				destination = (ComponentId) getAny(FETCH_ONLY_ONE);
//	
//				niche.log("ClientBindStub says: Op " + operationId + " bindId " + bindId.getId()
//						+ " is used to send a one-to-any message to "
//						+ destination.getId() + " hosted by "
//						+ destination.getResourceRef().getDKSRef());
//
//
//			niche.sendToNode(
//						destination.getResourceRef().getDKSRef(),
//						destination.getId(),
//						new SendThroughBindingMessage(
//								destination.getId(),
//								bindId,
//								message,
//								operationId
//						),
//						returnValueReceiver,
//						this
//					);
//			
//			break;
//
//		case JadeBindInterface.ONE_TO_MANY:
//
//				Object[] destinations = null;
//				destinations = getAll();
//
//				if (destinations.length < JadeBindInterface.DIRECT_SEND_THRESHOLD) {
//					for (int i = 0; i < destinations.length; i++) {
//						niche.log("ClientBindStub says: Op " + operationId + " bindId "
//								+ bindId.getId()
//								+ " is used to send a message to "
//								+ ((ComponentId) destinations[i]).getId()
//								+ " hosted by "
//								+ ((ComponentId) destinations[i])
//										.getResourceRef().getDKSRef());
//
//						niche.sendToNode(
//								((ComponentId) destinations[i])
//										.getResourceRef().getDKSRef(),
//										((ComponentId) destinations[i]).getId(),
//								new SendThroughBindingMessage(
//										((ComponentId) destinations[i]).getId(),
//										bindId,
//										message,
//										operationId
//										)
//								);
//
//					}
//				} else {
//					// Now the same, should be done constructing a broadcast
//					// call!
//					for (int i = 0; i < destinations.length; i++) {
//						niche.log("ClientBindStub says: Op " + operationId + " bindId "
//								+ bindId.getId()
//								+ " is used to send a message to "
//								+ ((ComponentId) destinations[i]).getId()
//								+ " hosted by "
//								+ ((ComponentId) destinations[i])
//										.getResourceRef().getDKSRef());
//
//						niche.sendToNode(
//								((ComponentId) destinations[i])
//										.getResourceRef().getDKSRef(),
//										((ComponentId) destinations[i]).getId(),
//								new SendThroughBindingMessage(
//										((ComponentId) destinations[i]).getId(),
//										bindId,
//										message,
//										operationId
//										)
//								);
//
//					}
//				}
//			
//			break;
//			default: 
//				System.err.println("Unsupported bind type, please fix");
//				niche.log("Unsupported bind type, please fix");
//				break;
//		} // End switch
//
//	}
	
	class BindSendJob {
		Object message;
		NicheNotifyInterface exceptionsAndReplyHandler;
		int operationId;
		BindSendJob(Object message, int operationId, NicheNotifyInterface exceptionsAndReplyHandler) {
			this.message = message;
			this.operationId = operationId;
			this.exceptionsAndReplyHandler = exceptionsAndReplyHandler;
		}
		
	}

/* (non-Javadoc)
 * @see dks.niche.interfaces.MessageManagerInterface#dropMessage()
 */
@Override
public boolean dropMessage() {
	return false;
}
/* (non-Javadoc)
 * @see dks.niche.interfaces.MessageManagerInterface#invokeOnChannelError()
 */
@Override
public boolean invokeOnChannelError() {
	return true;
}
	


}
