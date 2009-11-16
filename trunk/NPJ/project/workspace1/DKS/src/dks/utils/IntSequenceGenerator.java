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
 * @version $Id: IntSequenceGenerator.java 105 2006-11-16 11:44:28Z cosmin $
 */
public class IntSequenceGenerator {

	private int sequenceNumber;

	/**
	 * @param sequenceNumber
	 */
	public IntSequenceGenerator(int sequenceNumber) {
		super();
		this.sequenceNumber = sequenceNumber;
	}

	public int getNextSequenceNumber() {
		sequenceNumber++;
		if (sequenceNumber < 0) {
			sequenceNumber = 0;
		}
		return sequenceNumber;
	}
}
