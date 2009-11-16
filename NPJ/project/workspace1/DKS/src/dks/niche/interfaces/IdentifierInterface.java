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

import dks.addr.DKSRef;
import dks.niche.ids.NicheId;

/**
 * The <code>IdentifierInterface</code> class
 *
 * @author Joel
 * @version $Id: IdentifierInterface.java 294 2006-05-05 17:14:14Z joel $
 */
public interface IdentifierInterface {

	//Ok, so this will infact be the least common denominator between SNRs and MEs, so it not only has
	// to provide getId, but also get(current...)DKSRef
	public NicheId getId();
}
