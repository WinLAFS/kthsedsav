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

import java.io.Serializable;

import dks.messages.Message;
import dks.niche.ids.ManagementElementId;
import dks.niche.ids.NicheId;
import dks.niche.messages.DelegationRequestMessage;

/**
 * The <code>ManagementElementInteface</code> class
 *
 * @author Joel
 * @version $Id: ManagementElementInteface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface ManagementElementInterface extends IdentifierInterface, ReliableInterface {

	public final int COPY_FLAG = 1;
	public final int MOVE_FLAG = 10;
	
	public final int NEW = 0;
	public final int QUEUED = 2;
	public final int RECREATED_ON_MOVE = 4;
	public final int RECREATED_ON_FAIL = 8;	
	
	public void connect(NicheId id, int replicaNumber, NicheManagementInterface myHost, NicheNotifyInterface callBackHandler);
	public void init(Serializable[] parameters);
	public void reinit(Serializable[] parameters);

	//public void eventHandler(Object event);
	public void eventHandler(Serializable event, int flag);
	
	public void messageHandler(Message message);
	
	public DelegationRequestMessage transfer(int mode);
	
	public void setReplicaNumber(int replicaNumber);
	public int getReplicaNumber();
			
}
