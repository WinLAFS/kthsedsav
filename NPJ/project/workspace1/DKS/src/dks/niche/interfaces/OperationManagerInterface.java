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


/**
 * The <code>NicheNotifyInterface</code> class
 *
 * @author Joel
 * @version $Id: OperationManagerInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface OperationManagerInterface extends IdentifierInterface {

	public void notify(int operationId, Object message);
	//public int getOperationId();
	//public void notify(Object message);
}
