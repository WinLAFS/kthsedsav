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

import java.math.BigInteger;

import dks.messages.Message;
import dks.niche.ids.NicheId;

/**
 * The <code>SensorInterface</code> class
 *
 * @author Joel
 * @version $Id: .java 294 2006-05-05 17:14:14Z joel $
 */
public interface ReplicableMessageInterface {

	public NicheId getDestination();
	public Message setReplicaInformation(int replicaNumber, BigInteger destinationRingId); 
	public Message getReplicaCopy(int replicaNumber, BigInteger destinationRingId);
	public BigInteger getDestinationRingId();
	public int getReplicaNumber();
	
}
