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

import dks.messages.MessageTypeTable;
import dks.niche.messages.*;

/**
 * The <code>NicheMessageTable</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @author joel
 * @version $Id: NicheMessageTable.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheMessageTable extends MessageTypeTable {

	
	public final static int INTERVAL_STARTING = 8000;


	/*
	 * Here you can define the types of messages that your component will use.
	 * The message types intervals are assigned in blocks of 1000. Please ask
	 * for obtaining one of them or check in the WIKI if you can use already
	 * assigned identifiers.
	 * 
	 */

	 
	public static final int MSG_TYPE_BIND_REQUEST_MESSAGE =  1;
	public static final int MSG_TYPE_BIND_RESPONSE_MESSAGE =  2;
	public static final int MSG_TYPE_SEND_THROUGH_BINDING_MESSAGE =  3;
	

	/*
	 *	Here goes Niche-messages
	 *
	 */
	
	public static final int MSG_TYPE_SEND_TO_ID_MESSAGE = 5;
	
	public static final int MSG_TYPE_SEND_TO_ID_RESPONSE_MESSAGE = 6;
	
	public static final int MSG_TYPE_DELEGATION_REQUEST_MESSAGE = 7;
	
		
	public static final int MSG_TYPE_INSTANTIATE_SNR_REQUEST_MESSAGE = 9;
	
	public static final int MSG_TYPE_UPDATE_SNR_REQUEST_MESSAGE = 10;
	
	public static final int MSG_TYPE_DELIVER_EVENT_MESSAGE = 11;
	
	public static final int MSG_TYPE_UPDATE_MANAGEMENT_ELEMENT_MESSAGE = 12;
	
	public static Class[] messageTypes = new Class[15]; //[MessageTypeTable.class.getFields().length - 1];
	
	static {
		/*
		messageTypes[MSG_TYPE_DEPLOY_REQUEST_MESSAGE] = DeployRequestMessage.class;
		messageTypes[MSG_TYPE_DISCOVER_REQUEST_MESSAGE] = DiscoverRequestMessage.class;

		messageTypes[MSG_TYPE_DEPLOY_RESPONS_MESSAGE] = DeployResponseMessage.class;
		messageTypes[MSG_TYPE_DISCOVER_RESPONS_MESSAGE] = DiscoverResponsMessage.class;
		*/
		messageTypes[MSG_TYPE_BIND_REQUEST_MESSAGE] = BindRequestMessage.class;
		messageTypes[MSG_TYPE_BIND_RESPONSE_MESSAGE] = BindResponseMessage.class;
		
		messageTypes[MSG_TYPE_SEND_THROUGH_BINDING_MESSAGE] = SendThroughBindingMessage.class;
		
		//Here goes Niche-messages
		messageTypes[MSG_TYPE_SEND_TO_ID_MESSAGE] = SendToIdMessage.class; 
		messageTypes[MSG_TYPE_SEND_TO_ID_RESPONSE_MESSAGE] = SendToIdResponseMessage.class;
		messageTypes[MSG_TYPE_DELEGATION_REQUEST_MESSAGE] = DelegationRequestMessage.class;
	
		messageTypes[MSG_TYPE_INSTANTIATE_SNR_REQUEST_MESSAGE] = InstantiateSNRRequestMessage.class;
		messageTypes[MSG_TYPE_UPDATE_SNR_REQUEST_MESSAGE] = UpdateSNRRequestMessage.class;
		messageTypes[MSG_TYPE_DELIVER_EVENT_MESSAGE] = DeliverEventMessage.class;
		messageTypes[MSG_TYPE_UPDATE_MANAGEMENT_ELEMENT_MESSAGE] = UpdateManagementElementMessage.class;
		
	}

	public static Class getMessageTypeClass(int messageType) {
		return messageTypes[messageType % INTERVAL_STARTING];
		//return messageTypes[messageType]; ?? hmm
	}

}


