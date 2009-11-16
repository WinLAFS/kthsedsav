/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.niche.ids.BindElement;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.interfaces.NicheNotifyInterface;
import dks.niche.interfaces.OperationManagerInterface;

/**
 * The <code>NicheNotify</code> class
 *
 * @author Joel
 * @version $Id: NicheNotify.java 294 2006-05-05 17:14:14Z joel $
 */
public class NicheNotify implements NicheNotifyInterface {

	OperationManagerInterface fork;
	ClientSideBindStub bind;
	int operationId;
	
	
	/**
	 * @param fork
	 * @param operationId
	 */
	public NicheNotify(OperationManagerInterface fork, int operationId) {
		this.fork = fork;
		this.operationId = operationId;
	}
	public NicheNotify(ClientSideBindStub bind, int operationId) {
		this.bind = bind;
		this.operationId = operationId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheNotifyInterface#getOperationId()
	 */
	public int getOperationId() {
		return operationId;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.NicheNotifyInterface#notify(int, java.lang.Object)
	 */
	public void notify(Serializable result) {
		if(fork != null) {
			fork.notify(operationId, result);
		} else {
			bind.notify(MessageManagerInterface.RETURN_VALUE, result);
		}
		
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.IdentifierInterface#getId()
	 */
	public NicheId getId() {
		if(fork != null) {
			return fork.getId();
		} 
		return bind.getId();
	}
	

}
