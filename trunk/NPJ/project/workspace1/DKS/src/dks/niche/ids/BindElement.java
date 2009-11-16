/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.ids;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.ManagementElementInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheManagementInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.ReliableInterface;
import dks.niche.messages.BindRequestMessage;
import dks.niche.messages.DelegationRequestMessage;
import dks.niche.messages.UpdateManagementElementMessage;
import dks.niche.messages.UpdateSNRRequestMessage;
import dks.niche.wrappers.NicheNotify;
import dks.niche.wrappers.ResourceRef;

/**
 * The <code>BindElement</code> class
 * 
 * @author Joel
 * @version $Id: BindElement.java 294 2006-05-05 17:14:14Z joel $
 */
public class BindElement implements Serializable, IdentifierInterface,
		ManagementElementInterface {

	/**
	 * @serialVersionUID -
	 */
	
	//This is general bookkeeping stuff:
	
	private static final long serialVersionUID = 2227322099838937626L;


	transient NicheAsynchronousInterface niche;
	
	//protected NicheId myId;
	protected int replicaNumber;

	BindId bindId;
	
	transient NicheNotifyInterface initiator;

	// EMPTY CONSTRUCTOR NEEDED FOR DYNAMIC CREATION
	public BindElement() {

	}

	public void connect(NicheId id, int replicaNumber,
			NicheManagementInterface host,
			NicheNotifyInterface initiator) {
		
		this.replicaNumber = replicaNumber;
		this.niche = host.getNicheAsynchronousSupport();
		this.initiator = initiator;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dks.niche.interfaces.ManagementElementInterface#init(java.lang.Serializable[])
	 */
	public void init(Serializable[] parameters) {
		
		this.bindId = (BindId) parameters[0];

		boolean senderGroup = (bindId.getSender() instanceof GroupId);
		boolean receiverGroup = (bindId.getReceiver() instanceof GroupId);
		
		
		// So: send first to the receiver side, so the probability of the
		// receiver being ready when the sender is, is higher

		String logMessage = "BindElement-init " + bindId.getId() + " says: ";
		
		if (replicaNumber < 1) {
			
			if (!receiverGroup) {
				logMessage = sendToIdOrNode(bindId.getReceiver(), logMessage, "receiver", 0, null);
				// Should be id!
				// CurrentNode = dht.lookup(receiver.getId());


			} 
			
			//The else section below is disabled, now we require receiver interfaces to be pre-declared for 
			//each group being created
			
//			else {
//				
//				logMessage += " group receiver is "
//					+ receiver.getId().toString();
//				
//				niche.sendToManagement(
//						receiver.getId(),
//						new UpdateSNRRequestMessage(
//								receiver.getId(),
//								myId,
//								new Serializable[] {
//									type,
//									receiverSideInterfaceDescription,
//									false,
//									sender
//								}
//								),
//						receiver.isReliable()
//						); 
//								// sender even less											
//								// needed on
//								// receiver side
//			}

			if (!senderGroup) {
				
				logMessage = sendToIdOrNode(bindId.getSender(), logMessage, "sender", 1, initiator);
				
			} else {
				// send to SNRElement-host of sender-side
				/*#%*/ logMessage += " senders are in the group " + bindId.getSender().getId();
				
				niche.sendToManagement(
						bindId.getSender().getId(),
						new UpdateSNRRequestMessage(
								bindId.getSender().getId(),
								UpdateSNRRequestMessage.TYPE_ADD_CLIENT_BINDING,
								bindId								
							),
							true //groups are reliable. period.
						); // receiver is a cash-test
			}
		} /*#%*/ else {
		/*#%*/ logMessage += "'Do nuffin', I'm a replica: " + replicaNumber;
		/*#%*/ }
		//System.err.println(logMessage);
		/*#%*/ niche.log(logMessage);

	}

	public void reinit(Serializable[] parameters) {
		
		//don't do much
		this.bindId = (BindId) parameters[0];

	}

	private String sendToIdOrNode(IdentifierInterface element, String logMessage, String logFlag, int flag, NicheNotifyInterface initiator) {
		//Check if we have a shortcut to the node hosting the component
		//(this will NOT be the case if it is a ME)
		boolean sendToId = false;
		NicheId id = element.getId();
		
		if(element instanceof SNR) {
			
			ResourceRef rr = ((SNR)element).getResourceRef();
			
			if(rr != null) {
				DKSRef nodeOfRef =	rr.getDKSRef();
				logMessage +=
					" single "
					+ logFlag
					+ " is "
					+ id
					+ " on "
					+ nodeOfRef;
	
				niche.sendToNode(				
					nodeOfRef,
					id,
					new BindRequestMessage(
							id,
							bindId,
							flag,
							initiator
						)
				);

			} else {
				sendToId = true;
			}
		} else {
			sendToId = true;
		}
		
		if(sendToId) { //send to id
			//(this will be the case if it is a ME)
			logMessage +=
				" single "
				+ logFlag
				+ " is "
				+ id;

			niche.sendToManagement(				
				id,
				new BindRequestMessage(
						id,
						bindId,
						flag,
						initiator
				),
				id.isReliable()
			);
			
		}
		return logMessage;

	}


	public NicheId getId() {
		return bindId.getId();
	}

	public DelegationRequestMessage transfer(int mode) {
		return new DelegationRequestMessage(
				bindId.getId(),
				DelegationRequestMessage.TYPE_BINDING,
				BindElement.class.getName(),
				new Serializable[] {
					bindId
				}
			);
	}


	public void messageHandler(Message message) {
		if (message instanceof UpdateManagementElementMessage) {
			UpdateManagementElementMessage m = (UpdateManagementElementMessage) message;
			if (m.getType() == UpdateManagementElementMessage.TYPE_REMOVE_BINDING) {
				// TODO, FIXME
			}
		}

	}
	
	public void eventHandler(Serializable event, int flag) {
		// TODO Auto-generated method stub

	}
	public void eventHandler(Object event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#getReplicaNumber()
	 */
	
	public int getReplicaNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ManagementElementInterface#setReplicaNumber(int)
	 */
	
	public void setReplicaNumber(int replicaNumber) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.ReliableInterface#isReliable()
	 */
	
	public boolean isReliable() {
		// TODO Auto-generated method stub
		return false;
	}

}
