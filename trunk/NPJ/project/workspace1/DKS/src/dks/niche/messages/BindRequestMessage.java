/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.messages;

import java.util.ArrayList;

import dks.messages.Message;
import dks.niche.ids.BindElement;
import dks.niche.ids.BindId;
import dks.niche.ids.NicheId;
import dks.niche.ids.SNRElement;
import dks.niche.interfaces.IdentifierInterface;
import dks.niche.interfaces.NicheNotifyInterface;

/**
 * The <code>BindMessage</code> class
 *
 * @author Joel
 * @version $Id: BindMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class BindRequestMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 3273505555798389079L;

	int operationId;
	//BindElement bindId;
	String handlerId;
	
	BindId bindId;
	IdentifierInterface finalDestination;
	ArrayList<BindRequestMessage> bindings;
	
	int sender;
	NicheNotifyInterface initiator;
	
	//Do remember the empty constructor, or dynamic creation wont work
	public BindRequestMessage() {
		
	}
	/**
	 * @param operationId
	 * @param description
	 * @param eventName
	 */
//	public BindRequestMessage(int operationId, BindElement bindId, Object description, String handlerId) {
//		this(operationId, bindId, description, handlerId, -1);
//	}
	
//	public BindRequestMessage(int operationId, BindElement bindId, Object description, String handlerId, int receiver) {
//		this.operationId = operationId;
//		//this.source = source;
//		//this.bindId = bindId;
//		//this.description = description;
//		this.handlerId = handlerId;
//		//this.receiver = receiver;
//	}

	//new ones:
	
	public BindRequestMessage(IdentifierInterface destination, Object bindings, int sender) {
		//this.source = source;
		this.finalDestination = destination;
		if(bindings instanceof BindId) {
			this.bindId = (BindId) bindings;
		} else {
			this.bindings = (ArrayList<BindRequestMessage>)bindings;
		}
		this.sender = sender;
	}
	
	public BindRequestMessage(IdentifierInterface destination, Object bindings, int sender, NicheNotifyInterface initiator) {
		//this.source = source;
		this.finalDestination = destination;
		if(bindings instanceof BindId) {
			this.bindId = (BindId) bindings;
		} else {
			this.bindings = (ArrayList<BindRequestMessage>)bindings;
		}
		this.sender = sender;
		this.initiator = initiator;
		
	}
	
//	public BindRequestMessage(IdentifierInterface destination, NicheId bindId, Object interfaceDescription, int type, SNRElement counterpart, ArrayList cache) {
//		//this.source = source;
//		this.destination = destination;
//		this.bindId = bindId;
//		this.description = interfaceDescription;
//		this.type = type;
//		this.counterpart = counterpart;
//		this.cache = cache;
//	}

	
//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler   ) { //,	List<DirectByteBuffer> buffers)  {
//		super.initMarshall(buffers, messageType, marshaler);
//		
//		marshaler.addInt(buffers, operationId);		
//		try {
//			marshaler.addObject(buffers, source);
//			marshaler.addObject(buffers, bindId);
//			marshaler.addObject(buffers, description);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		marshaler.addString(buffers, eventName);
//		marshaler.addInt(buffers, receiver);
//			
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		
//		operationId = marshaler.remInt(buffers);
//		try {
//			source = (DKSRef)marshaler.remObject(buffers);
//			bindId = (BindElement)marshaler.remObject(buffers);
//			description = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		eventName = marshaler.remString(buffers);
//		receiver = marshaler.remInt(buffers);
//	}
//	
//	/* (non-Javadoc)
//	 * @see dks.messages.Message#getMessageType()
//	 */
//	@Override
//	public int getMessageType() {
//		return messageType;
//	}
//
//	public static int getStaticMessageType() {
//		return messageType;
//	}


//	/**
//	 * @return Returns the description.
//	 */
//	public Object getDescription() {
//		return description;
//	}
//
//
//	/**
//	 * @param description The description to set.
//	 */
//	public void setDescription(Object description) {
//		this.description = description;
//	}


	/**
	 * @return Returns the eventName.
	 */
	public String getHandlerId() {
		return handlerId;
	}


	/**
	 * @param eventName The eventName to set.
	 */
	public void setHandlerId(String handlerId) {
		this.handlerId = handlerId;
	}


	/**
	 * @return Returns the operationId.
	 */
	public int getOperationId() {
		return operationId;
	}



	/**
	 * @param operationId The operationId to set.
	 */
	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}


	/**
	 * @return Returns the receiver.
	 */
	public boolean isSender() {
		return 0 < sender;
	}
//
//	/**
//	 * @param receiver The receiver to set.
//	 */
//	public void setReceiver(int receiver) {
//		this.receiver = receiver;
//	}
	
	
	
//	public ComponentId getComponentId(BigInteger i) {
//		if (0 <= receiver) {
//			return bindId.getReceiverComponentId(i);
//		}
//		return bindId.getSenderComponentId();
//	}
//	
	public BindId getBindInfo() {
		return bindId;
	}
	
	public ArrayList<BindRequestMessage> getBindings() {
		return bindings;
	}
	public IdentifierInterface getDestination() {
		return finalDestination;
	
	
	}
	
	public NicheId getDestinationId() {
		return finalDestination.getId();
	}
	
	public int getType() {
		return bindId.getType();
	}
	
	
	public NicheNotifyInterface getInitiator() {
		return initiator;
	}
	public boolean isBulk(){
		return bindings != null; 
	}
	
}
