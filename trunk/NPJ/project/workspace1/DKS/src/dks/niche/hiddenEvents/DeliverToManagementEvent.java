/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.hiddenEvents;

import java.io.Serializable;


/**
 * The <code>DeliverToManagementEvent</code> class
 *
 * @author Joel
 * @version $Id: DeliverToManagementEvent.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeliverToManagementEvent extends ManagementEvent implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	int operationId;
	private static final long serialVersionUID = -3747777265668822978L;

	public DeliverToManagementEvent(Serializable o) {
		super(o);
	}
	
	public void setOperationId(int operationId) {
		this.operationId = operationId;
	}
	
	public int getOperationId() {
		return operationId;
	}
}
