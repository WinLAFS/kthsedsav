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
 * The <code>JadeDirectTest</code> class
 *
 * @author Joel
 * @version $Id: JadeDirectTest.java 294 2006-05-05 17:14:14Z joel $
 */
public class JadeDirectTest {

	public Object resourceEnquiry(Object o) {
		return o + " "+ o;
	}
	public Object deploy(Object o) {
		return o + " " + o;
	}
}
