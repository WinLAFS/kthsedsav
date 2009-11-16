/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.ids;

import java.io.Serializable;
import java.util.HashMap;

import dks.niche.interfaces.IdentifierInterface;
import dks.niche.wrappers.ResourceRef;


/**
 * The <code>IdentifierInterface</code> class
 *
 * @author Joel
 * @version $Id: IdentifierInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface SNR extends IdentifierInterface, Serializable {

	public ResourceRef getResourceRef();
	public void addServerBinding(String interfaceName, int type);
	public HashMap<String, BindId> getPredefinedReceiverBindings();
}
