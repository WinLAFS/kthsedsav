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

/**
 * The <code>SequenceGenerator</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: LongSequenceGenerator.java 214 2007-02-27 11:39:04Z Roberto $
 */
public class LongSequenceGenerator {

	private long sequenceNumber;

	/**
	 * @param sequenceNumber
	 */
	public LongSequenceGenerator(long sequenceNumber) {
		super();
		this.sequenceNumber = sequenceNumber;
	}

	synchronized public long getNextSequenceNumber() {
		sequenceNumber++;
		if (sequenceNumber < 0) {
			sequenceNumber = 0;
		}
		return sequenceNumber;
	}
}
