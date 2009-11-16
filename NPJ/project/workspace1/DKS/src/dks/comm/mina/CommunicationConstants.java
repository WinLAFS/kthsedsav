/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina;

/**
 * The <code>CommunicationConstants</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: CommunicationConstants.java 294 2006-05-05 17:14:14Z roberto $
 */
public interface CommunicationConstants {

	public final static String CONNECTION_ENDPOINT = "comm_endpoint";

	public final static String PERMANENT_COUNTER = "permanent_counter";

	public final static String NEWLY_CREATED = "newly-created";

	public final static String TRANSPORT_PROTOCOL = "protocol";

	public final static String MESSAGES_SENT = "sent";

	public final static String MESSAGES_RECEIVED = "received";

	public final static boolean PERMANENT = true;
	
	public final static boolean TEMPORARY = true;

	public final static boolean MINA_LOGGING_ENABLED =
		System.getProperty("dks.comm.minaLogging") instanceof String ?
				0 < Integer.parseInt(System.getProperty("dks.comm.minaLogging")) ?
						true
					:
						false
			:
				false;

	public final static boolean MINA_SERIALIZATION_LOGGING_ENABLED =
		System.getProperty("dks.comm.minaSerializationLogging") instanceof String ?
				0 < Integer.parseInt(System.getProperty("dks.comm.minaSerializationLogging")) ?
						true
					:
						false
			:
				false;

	public final static long SESSION_GARBAGE_COLLECTION_TIMER=5000;
	
	//public final static int SEND_TIMEOUT=1000;
	

}
