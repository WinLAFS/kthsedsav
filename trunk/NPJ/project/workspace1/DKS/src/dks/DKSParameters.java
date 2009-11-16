/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks;

import java.math.BigInteger;

/**
 * The <code>DKSParameters</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSParameters.java 320 2007-06-28 15:11:51Z roberto $
 */
public class DKSParameters {

	/**
	 * @K - the arity of the system
	 */
	public int K; 
	
	/**
	 * @L - the number of levels
	 */
	public int L; 
	
	/**
	 * @N - the size of the identifier space (N = K ^ L). 
	 */
	public BigInteger N;

	/**
	 * @param k
	 * @param l
	 */
	public DKSParameters(int k, int l) {
		super();
		K = k;
		L = l;
		N = BigInteger.valueOf(K).pow(L);
	}
}
