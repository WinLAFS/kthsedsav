/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.tests;



/**
 * The <code>JadeInterfaceTest</code> class
 *
 * @author Joel
 * @version $Id: JadeInterfaceTest.java 294 2006-05-05 17:14:14Z joel $
 */
public class JadeInterfaceTest  {

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.JadeResourceEnquiryInterface#resourceEnquiry(java.lang.String)
	 */
	public Object resourceEnquiry(Object o) {
		return o + " "+ o;
	}

	/* (non-Javadoc)
	 * @see dks.niche.interfaces.JadeDeploymentInterface#deploy(java.lang.String)
	 */
	public Object deploy(Object o) {
		// TODO Auto-generated method stub
		return o + " " + o;
	}

}
