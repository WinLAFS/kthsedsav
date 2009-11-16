/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.NicheId;
import dks.niche.ids.SNR;
import dks.niche.ids.SNRElement;
import dks.niche.interfaces.IdentifierInterface;

/**
 * The <code>MemberAddedEvent</code> class
 *
 * @author Joel
 * @version $Id: MemberAddedEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class MemberAddedEvent extends Event implements ConfigurationEvent, Serializable {

	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5304319624236572231L;
	SNR snr;
	NicheId brokerId;
	
	public MemberAddedEvent () {
		
	}

	public MemberAddedEvent(SNR snr) {
	
		this.snr = snr;
	}

	public MemberAddedEvent(IdentifierInterface snr) {
	
		this.snr = (SNR)snr;
	}

	
	/**
	 * @return
	 */
	public SNR getSNR() {
		return snr;
	}
	public void setSNR(SNR snr) {
		this.snr = snr;
	}

	/* (non-Javadoc)
	 * @see dks.niche.events.ConfigurationEvent#getBroker()
	 */
	@Override
	public NicheId getBroker() {
		return brokerId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.events.ConfigurationEvent#getSource()
	 */
	@Override
	public String getSource() {
		// 
		return brokerId.toString();
	}

	/* (non-Javadoc)
	 * @see dks.niche.events.ConfigurationEvent#setBroker(dks.niche.ids.NicheId)
	 */
	@Override
	public ConfigurationEvent setBroker(NicheId id) {
		this.brokerId = id;
		return this;
	}
	

}
