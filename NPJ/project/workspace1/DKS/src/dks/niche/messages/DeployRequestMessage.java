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

import java.io.Serializable;

import dks.addr.DKSRef;
import dks.messages.Message;
import dks.niche.wrappers.BulkSendContent;

/**
 * The <code>DeployRequestMessage</code> class
 *
 * @author Joel
 * @version $Id: DeployRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class DeployRequestMessage extends Message implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -3970177202724844491L;
	BulkSendContent info;
	DKSRef dsource;
	Object managementDeployParam;
	int size;
	
	/**
	 * @param info
	 */
	public DeployRequestMessage(BulkSendContent info, DKSRef source, int size) {
		this.info = info;
		this.dsource = source;
		this.size = size;
	}
	
	public DeployRequestMessage(Object info, DKSRef source) {
		this.managementDeployParam = info;
		this.dsource = source;
	}

	public BulkSendContent getContent() {
		return info;
	}

	public void setContent(BulkSendContent info) {
		this.info = info;
	}

	public DKSRef getDeploySource() {
		return dsource;
	}
	
	public int getSize() {
		return size;
	}
	
	public Object getManagementDeployParam() {
		return managementDeployParam;
	}
	
}
