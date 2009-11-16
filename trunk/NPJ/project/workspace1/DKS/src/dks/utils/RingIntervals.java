/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.utils;

import java.math.BigInteger;

/**
 * The <code>RingIntervals</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingIntervals.java 119 2006-11-20 11:42:15Z cosmin $
 */
public class RingIntervals {

	public static enum Bounds {
		OPEN_OPEN, OPEN_CLOSED, CLOSED_OPEN, CLOSED_CLOSED
	};

	public static boolean belongsTo(BigInteger id, BigInteger from,
			BigInteger to, BigInteger N, Bounds bounds) {

		BigInteger X = from;
		BigInteger Y = to;
		BigInteger NX = BigInteger.ZERO;
		BigInteger NY = Y.subtract(X).mod(N);
		BigInteger NId = id.subtract(X).mod(N);

		if (bounds == Bounds.CLOSED_OPEN) {
			if (X.equals(Y)) {
				return (true);
			} else {
				return ((NId.compareTo(NX) >= 0) && (NId.compareTo(NY) < 0));
			}
		} else if (bounds == Bounds.OPEN_CLOSED) {
			if (X.equals(Y)) {
				return (true);
			} else {
				return ((NId.compareTo(NX) > 0) && (NId.compareTo(NY) <= 0));
			}
		} else if (bounds == Bounds.CLOSED_CLOSED) {
			if ((X.equals(Y)) && (id.equals(X))) {
				return (true);
			} else {
				return ((NId.compareTo(NX) >= 0) && (NId.compareTo(NY) <= 0));
			}
		} else if (bounds == Bounds.OPEN_OPEN) {
			if ((X.equals(Y)) && (!id.equals(X))) {
				return (true);
			} else {
				return ((NId.compareTo(NX) > 0) && (NId.compareTo(NY) < 0));
			}
		}
		return false;
	}
}
