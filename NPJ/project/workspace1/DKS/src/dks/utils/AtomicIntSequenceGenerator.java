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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The <code>SequenceGenerator</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: AtomicIntSequenceGenerator.java 109 2006-11-16 14:23:25Z cosmin $
 */
public class AtomicIntSequenceGenerator {

	private AtomicInteger sequenceNumber;

	/**
	 * @param sequenceNumber
	 */
	public AtomicIntSequenceGenerator(int sequenceNumber) {
		super();
		this.sequenceNumber = new AtomicInteger(sequenceNumber);
	}

	public int getNextSequenceNumber() {
		sequenceNumber.addAndGet(1);
		if (sequenceNumber.get()< 0) {
			sequenceNumber.set(0);
		}
		return sequenceNumber.get();
	}
}
