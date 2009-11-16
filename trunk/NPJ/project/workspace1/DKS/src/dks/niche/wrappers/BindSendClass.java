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

import dks.addr.DKSRef;
import dks.niche.ids.BindElement;
import dks.niche.ids.ComponentId;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.JadeBindInterface;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.messages.SendThroughBindingMessage;

/**
 * The <code>BindSendClass</code> class
 *
 * @author Joel
 * @version $Id: BindSendClass.java 294 2006-05-05 17:14:14Z joel $
 */
public class BindSendClass { // implements MessageManagerInterface {

			
//		final static int BIND_RETRY_DELAY = 1000;
//		
//		int operationId;
//		String id;
//		
//		BindElement bindId;
//
//		Object message;
//		
//		boolean sendFailure = false; 
//
//		ComponentId receiver = null;
//
//		NicheAsynchronousInterface myCommunicator;
//		
//		NicheNotifyInterface returnValueReceiver;
//		
//		private final int FETCH_ONLY_ONE =
//			System.getProperty("niche.bindings.fetchOnlyOne") instanceof String ?
//					Integer.parseInt(System.getProperty("niche.bindings.fetchOnlyOne"))
//					:
//					0;
//
//
//		public BindSendClass(int operationId, NicheAsynchronousInterface communicator, BindElement bindId, Object message, ComponentId receiver,
//				NicheNotifyInterface returnValueReceiver) {
//		
//			this.operationId = operationId;
//			this.myCommunicator = communicator;
//			this.bindId = bindId;
//			this.id = bindId.getId().toString() + operationId; 
//			this.message = message;
//			this.receiver = receiver;
//			this.returnValueReceiver = returnValueReceiver;
//		}
//
//		public void run() {
//			
//			if(sendFailure) {
//
//				myCommunicator.log("BindSendClass says: message on "+ bindId.getId() +" could NOT be delivered to a proper receiver, sleep " + BIND_RETRY_DELAY + " & retry");
//					try {
//						Thread.sleep(BIND_RETRY_DELAY);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					//TODO: think of the semantics of this:
//					receiver = null;
//					// if we had picked a specific node to deliver a respons to, this
//					// might be incorrect / inconsistent 
//					iterate();
//			} else {
//				myCommunicator.log("BindSendClass says: message on "+ bindId.getId() +" is about to be processed");
//				iterate();
//			}
//		}
//
//		/* (non-Javadoc)
//		 * @see dks.niche.interfaces.NicheNotifyInterface#notify(java.lang.Object)
//		 */
//		public void notify(Object message) {
//			
//			sendFailure = (Boolean)message;
//			
//		}
//
//		/* (non-Javadoc)
//		 * @see dks.niche.interfaces.IdentifierInterface#getId()
//		 */
//		public String getId() {
//			return id;
//		}
//		
//		public boolean invokeOnIdError() {
//			return true;
//		}
//
//		public boolean invokeOnNodeError() {
//			return true;
//		}
//		
//		private void iterate() {
//			//int operationId = -1;
//			DKSRef temp;
//
//			switch (bindId.getBasicType()) {
//
//			case JadeBindInterface.ONE_TO_ONE_WITH_RETURN_VALUE:
//			case JadeBindInterface.ONE_TO_ONE:
//
//				if(receiver == null) {
//				
//					if(bindId.getReceiver() instanceof ComponentId && ((ComponentId)bindId.getReceiver()).getResourceRef() != null) {
//					
//						temp = ((ComponentId)bindId.getReceiver()).getResourceRef().getDKSRef();
//						
//						myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement " + bindId.getId()
//								+ " is used to send a one-to-one message to "
//								+ bindId.getReceiver().getId() + " hosted by "
//								+ temp);
//	
//		
//						
//					} else { //fetch from id
//
//						myCommunicator.log("BindSendClass says: Op "
//								+ operationId +
//								" References for BindElement "
//								+ bindId.getId()
//								+ " are being fetched from " 
//								+ bindId.getReceiver().getId().toString()
//								+ " before sending");
//
//
//						IdentifierInterface destination = (IdentifierInterface) bindId.getAny(FETCH_ONLY_ONE);
//						temp = destination.getDKSRef();
//						
//						myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement " + bindId.getId()
//								+ " is used to send a message to "
//								+ destination.getId() + " hosted by "
//								+ temp
//						);						
//					}
//					
//					myCommunicator.sendToNode(
//							temp,
//							bindId.getReceiver().getId(),
//							new SendThroughBindingMessage(
//									bindId.getReceiver().getId(),
//									bindId,
//									message,
//									operationId
//							),
//							returnValueReceiver,
//							this
//					);
//
//					
//				} else {
//					
//					temp = receiver.getResourceRef().getDKSRef();
//				
//					myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement " + bindId.getId()
//							+ " is shortcutted used to send a message to "
//							+ receiver.getId() + " hosted by "
//							+ temp);
//
//					myCommunicator.sendToNode(
//							temp,
//							receiver.getId(),
//							new SendThroughBindingMessage(
//									receiver.getId(),
//									bindId,
//									message,
//									operationId
//							),
//							returnValueReceiver,
//							this
//					);
//
//				}
//					
//				break;
//
//			case JadeBindInterface.ONE_TO_ANY_WITH_RETURN_VALUE:				
//			case JadeBindInterface.ONE_TO_ANY:
//
//				ComponentId destination;
//
//				if (receiver == null) {
//
//					myCommunicator.log("BindSendClass says: Op "
//							+ operationId +
//							" References for BindElement "
//							+ bindId.getId()
//							+ " are being fetched from " 
//							+ bindId.getReceiver().getId().toString()
//							+ " before one-to-any sending");
//
//
//					destination = (ComponentId) bindId.getAny(FETCH_ONLY_ONE);
//		
//					myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement " + bindId.getId()
//							+ " is used to send a one-to-any message to "
//							+ destination.getId() + " hosted by "
//							+ destination.getResourceRef().getDKSRef());
//
//				} else {
//
//					destination = receiver; // (ComponentId)
//			
//					myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement " + bindId.getId()
//							+ " is shortcutted to go directly to " + destination.getId());
//
//				}
//
//				myCommunicator.sendToNode(
//							destination.getResourceRef().getDKSRef(),
//							destination.getId(),
//							new SendThroughBindingMessage(
//									destination.getId(),
//									bindId,
//									message,
//									operationId
//							),
//							returnValueReceiver,
//							this
//						);
//				
//				break;
//
//			case JadeBindInterface.ONE_TO_MANY:
//
//				if (receiver == null) {
//					Object[] destinations = null;
//					destinations = bindId.getAll();
//
//					if (destinations.length < JadeBindInterface.DIRECT_SEND_THRESHOLD) {
//						for (int i = 0; i < destinations.length; i++) {
//							myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement "
//									+ bindId.getId()
//									+ " is used to send a message to "
//									+ ((ComponentId) destinations[i]).getId()
//									+ " hosted by "
//									+ ((ComponentId) destinations[i])
//											.getResourceRef().getDKSRef());
//
//							myCommunicator.sendToNode(
//									((ComponentId) destinations[i])
//											.getResourceRef().getDKSRef(),
//											((ComponentId) destinations[i]).getId(),
//									new SendThroughBindingMessage(
//											((ComponentId) destinations[i]).getId(),
//											bindId,
//											message,
//											operationId
//											)
//									);
//
//						}
//					} else {
//						// Now the same, should be done constructing a broadcast
//						// call!
//						for (int i = 0; i < destinations.length; i++) {
//							myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement "
//									+ bindId.getId()
//									+ " is used to send a message to "
//									+ ((ComponentId) destinations[i]).getId()
//									+ " hosted by "
//									+ ((ComponentId) destinations[i])
//											.getResourceRef().getDKSRef());
//
//							myCommunicator.sendToNode(
//									((ComponentId) destinations[i])
//											.getResourceRef().getDKSRef(),
//											((ComponentId) destinations[i]).getId(),
//									new SendThroughBindingMessage(
//											((ComponentId) destinations[i]).getId(),
//											bindId,
//											message,
//											operationId
//											)
//									);
//
//						}
//					}
//				} else {
//					//destination = (ComponentId) bindId.get(receiver);
//					
//					destination = receiver; // (ComponentId)
//
//					myCommunicator.log("BindSendClass says: Op " + operationId + " BindElement " + bindId.getId()
//							+ " is shortcutted to go directly to " + destination.getId());
//
//					myCommunicator.sendToNode(
//							destination.getResourceRef().getDKSRef(),
//							destination.getId(),
//							new SendThroughBindingMessage(
//									destination.getId(),
//									bindId,
//									message,
//									operationId
//							)
//					);
//
//				}
//				break;
//				default: 
//					System.err.println("Unsupported bind type, please fix");
//					myCommunicator.log("Unsupported bind type, please fix");
//					break;
//			} // End switch
//
//		}
	}


