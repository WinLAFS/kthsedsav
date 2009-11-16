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

import java.io.Serializable;

/**
 * The <code>TestTest</code> class
 *
 * @author Joel
 * @version $Id: TestTest.java 294 2006-05-05 17:14:14Z joel $
 */
public class TestTest implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -7720669552774149828L;
	int i = 37;
	public TestTest() {
		i = 99;
	}
}
