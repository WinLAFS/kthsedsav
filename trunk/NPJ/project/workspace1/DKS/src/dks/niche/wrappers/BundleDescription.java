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
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

/**
 * The <code>BundleDescription</code> class
 *
 * @author Joel
 * @version $Id: BundleDescription.java 294 2006-05-05 17:14:14Z joel $
 */
public class BundleDescription implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 1300377500512405666L;
	HashMap resourceDescriptions;
	Vector types; //possibly not needed, keep it 4 now.
	
	public BundleDescription() {
		
		resourceDescriptions = new HashMap();
		types = new Vector();
		
	}
	public void describeResource(Object type, Object value) {
		types.add(type);
		resourceDescriptions.put(type, value);
	}
	
	public Object getResourceDescription(Object type) {
		return resourceDescriptions.get(type);
	}
	
	public Set getResourceDescriptions() {
		return resourceDescriptions.entrySet();
	}
	
	
}
