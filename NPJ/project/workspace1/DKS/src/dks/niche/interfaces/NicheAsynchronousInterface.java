/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.interfaces;

//import org.apache.mina.common.WriteFuture;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.arch.Event;
import dks.messages.Message;
import dks.niche.fractal.interfaces.EventHandlerInterface;
import dks.niche.ids.ComponentId;
import dks.niche.ids.NicheId;
import dks.niche.ids.SNRElement;
import dks.niche.wrappers.NodeRef;
import dks.niche.wrappers.SimpleResourceManager;

/**
 * The <code>NicheAsynchronousInterface</code> class
 * 
 * This is a system developer interface class! of interest for those who want to
 * extend/modify the system itself 
 *
 * @author Joel
 * @version $Id: NicheAsynchronousInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface NicheAsynchronousInterface extends LoggerInterface{

	
	//Alles:
	
	public void trigger(Event e);
	
	public void publicExecute(Runnable task);
	//Discover
	
	//public void discover(Object requirements, NicheNotifyInterface initiator);
	
	//Allocate
	
	//public void allocate(Object receiver, Object description, NicheNotifyInterface initiator);
	
	//Deploy
	
	//public void deploy(Object receiver, Object whatToDeploy, NicheNotifyInterface initiator);
	
	//Message communication
	
	public void requestFromManagement(NicheId destination, Serializable requestMessage, NicheNotifyInterface initiator);
	
	//public void requestId(NicheId target, NicheNotifyInterface initiator);

	public void sendToNode(DKSRef node, Message message);
	public void sendToManagement(DKSRef node, NicheId destinationId, Message message);
	public void sendToNode(DKSRef node, NicheId destinationId, Message message);
	public void sendToNode(DKSRef destination, NicheId destinationId, Message requestMessage, NicheNotifyInterface initiator, MessageManagerInterface sendManager);
	//public void sendToNode(DKSRef node, Message message);
	//
	
	//public void sendToManagement(NicheId id, Message message);
	public void sendToManagement(NicheId id, Message message, boolean replicate);
	

	//Message communication - Components
	
//	public void send(Object localBindId, Object message);
//	
//	public void send(Object localBindId, Object message, ComponentId destination);
	
	public void sendWithReply(Object localBindId, Serializable message, ComponentId destination, NicheNotifyInterface replyReceiver);
	public void sendWithSendAck(Object localBindId, Serializable message, ComponentId destination, NicheNotifyInterface sendAckReceiver);
	
	//Event communication
	
	//public void trigger(Event e);

	//Local getters
	/**
	 * @param id
	 * @return
	 */
	//public NicheId getUniqueCollocatedId(NicheId id);
	
	//NicheId locationId can be null!
	public NicheId getNicheId(NicheId locationId, String owner, int type, boolean reliable);
	/**
	 * @return
	 */
	//public NicheId getUncheckedUniqueId();

	//public NicheId getLocalId();
	
	/**
	 * @param nodeOfRef
	 * @return
	 */
	//public NicheId getCloseNodeId(DKSRef nodeOfRef, NicheId creatingEntity, int type, boolean reliable);
//	public NicheId getCloseNodeId(NicheId idOfRef);
//	public NicheId getNextNodeId();
//	public NicheId getSameNodeId(DKSRef nodeOfRef);

	/**
	 * @return
	 */
	public SimpleResourceManager getResourceManager();

	
	//Timers
	
	/**
	 * @param me
	 * @param name
	 * @param period
	 */
	public long registerTimer(EventHandlerInterface me, Class name, int period);

	/**
	 * @param timerId
	 */
	public void cancelTimer(long timerId);
	
	//Logs
	
	/*#%*/ 	public void log(String message);

	//public NodeRef getMyNodeRef();
	
}
