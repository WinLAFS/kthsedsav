/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.tests;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The <code>LargeObject</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: LargeObject.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class LargeObject implements Serializable {

/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 7963445935835182587L;

	String msg;
	
	static ArrayList<Long> veryLargeData;
	
	{
		veryLargeData = new ArrayList<Long>(50000);	//50000 ~= 700KB, 100000 ~= 1.4 MB, 1000000 ~= 14MB 
		
		for(long i = 0; i<50000 ; i++)
			veryLargeData.add(i);
	}
	/**
	 * 
	 */
	public LargeObject(String msg) {
		this.msg = msg;
	}
	@Override
	public String toString() {
		
		return msg;
	}
	
	

}
