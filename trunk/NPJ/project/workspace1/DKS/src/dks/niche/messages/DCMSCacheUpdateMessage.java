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

import java.math.BigInteger;

import dks.messages.Message;
import dks.niche.ids.NicheId;

/**
 * The <code>DCMSCacheUpdateMessage</code> class
 *
 * @author Joel
 * @version $Id: DCMSCacheUpdateMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class DCMSCacheUpdateMessage extends Message {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 7881228020056500094L;
	private NicheId dcmsId; 
	private BigInteger ringId;
	
	public DCMSCacheUpdateMessage() {
		
	}
	
	public DCMSCacheUpdateMessage(NicheId dcmsId, BigInteger ringId) {
		this.dcmsId = dcmsId;
		this.ringId = ringId;
	}

	public NicheId getDCMSId() {
		return dcmsId;
	}

	public void setDCMSId(NicheId dcmsId) {
		this.dcmsId = dcmsId;
	}

	public BigInteger getRingId() {
		return ringId;
	}

	public void setRingId(BigInteger ringId) {
		this.ringId = ringId;
	}
	
	
}
