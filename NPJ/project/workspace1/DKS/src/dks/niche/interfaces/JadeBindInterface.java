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

/**
 * The <code>JadeBindInterface</code> class
 *
 * @author Joel
 * @version $Id: JadeBindInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface JadeBindInterface {

	public static final int ONE_TO_ONE = 1;
	public static final int ONE_TO_ANY = 16;
	public static final int ONE_TO_MANY = 64;
	
	public static final int ONE_TO_ONE_STATIC = 3;
	
	public static final int NO_SEND_TO_SENDER = 512;
	
	public static final int WITH_RETURN_VALUE = 8;
	
	public static final int ONE_TO_ONE_WITH_RETURN_VALUE = ONE_TO_ONE | WITH_RETURN_VALUE;
	//public static final int ONE_TO_MANY_WITH_RETURN_VALUE = 12;
	public static final int ONE_TO_ANY_WITH_RETURN_VALUE = ONE_TO_ANY | WITH_RETURN_VALUE;;
	
	public static final int BASIC_TYPES = ONE_TO_ONE | ONE_TO_ANY | ONE_TO_MANY;
	
	public static final int DIRECT_SEND_THRESHOLD = 256; //TODO: decide value for this
	
	public void send(Object localBindId, Object message);
}
