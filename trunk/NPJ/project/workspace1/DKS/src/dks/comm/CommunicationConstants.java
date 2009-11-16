/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm;

/**
 * The <code>CommunicationConstants</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: CommunicationConstants.java 491 2007-12-11 12:01:50Z roberto $
 */
public interface CommunicationConstants {

	/* Messages types */
	public final static byte MSG_ACK = (byte) 0;

	public final static byte PING = (byte) 1;

	public final static byte PONG = (byte) 2;

	public final static byte MSG_HELLO = (byte) 3;

	public final static byte MSG_KEEP_CONNECTION = (byte) 4;

	public final static byte MSG_CLOSE = (byte) 5;

	public final static byte MSG_CLOSE_ACK = (byte) 6;

	public final static byte MSG_CONTENT = (byte) 7;

	public final static byte MSG_WEB = (byte) 'G';

	/* receiver states */
	public final static int RCV_HEADER = 0;

	public final static int RCV_PAYLOAD = 1;

	public final static int RCV_WEB_REQUEST = 2;

	/* transmitter states */
	public final static int XMIT_HEADER = 2;

	public final static int XMIT_PAYLOAD = 3;

	/* How many nested calls in the transmitter/receiver */
	public final static int MAX_NESTED_IO_OPERATIONS = 2;
	/*
	 * Rationale of the PAYLOAD_SIZE of the ByteBuffers: Ethernet MTU = 1500
	 * bytes - 40 bytes (IPv6 Header - without options) - 20 bytes (TCP Header)
	 * 28 bytes = 1440
	 */
	public final static int PAYLOAD_SIZE = 1440;

	/*
	 * 1 byte: message type 4 bytes: payload length 4 bytes: message ID
	 */
	public final static int HEADER_SIZE = 9;

	/* The time after which the first connection is considered timed out */
	public final static long INITAL_CONNECTION_TIMEOUT = 5000;

	public final static boolean PERMANENT = true;

	public final static boolean TEMPORARY = false;

	public final static int KEEP_CONNECTION_PERMANENT = 0;

	public final static int KEEP_CONNECTION_TEMPORARY = 1;

	// Connection garbage connection timer
	public final static long GARBAGE_COLLECTION_TIMER = 30000;

	public final static long GARBAGE_COLLECTION_CONNECTION_AGE = 2000;
	
	public final static long GARBAGE_COLLECTION_BUFFERS = 5000;
}
