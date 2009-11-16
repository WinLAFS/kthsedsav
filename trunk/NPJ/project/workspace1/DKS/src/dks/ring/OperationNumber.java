/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * The <code>OperationNumber</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: OperationNumber.java 479 2007-12-07 10:14:04Z roberto $
 */
public class OperationNumber implements Serializable {

	private static final long serialVersionUID = 7933283412590795175L;

	private BigInteger identifier;

	private long counter;

	/**
	 * Constructs an operation's identifier from the identifier of the peer and
	 * a long value(from an external operation counter)
	 * 
	 * @param identifier
	 * @param counter
	 */
	public OperationNumber(BigInteger identifier, long counter) {
		super();
		this.identifier = identifier;
		this.counter = counter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof OperationNumber) {
			OperationNumber opNum = (OperationNumber) arg0;
			if (opNum.identifier.equals(this.identifier)
					&& opNum.counter == this.counter)
				return true;
			else
				return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((identifier == null) ? 0 : identifier.hashCode());
		result = PRIME * result + (int)counter;
		return result;
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return identifier.toString() + counter;
	}

	/**
	 * Checks if this {@link OperationNumber} is previous to the
	 * {@link OperationNumber} passed
	 * 
	 * @param opNum
	 *            The current {@link OperationNumber}
	 * @return true if previous, false otherwise
	 */

	public boolean isEqualOrPreviousOpOf(OperationNumber opNum) {
		if ((this.equals(opNum))
				|| (opNum.identifier.equals(identifier) && opNum.counter > this.counter))
			return true;
		else
			return false;
	}

	/**
	 * @return Returns the counter.
	 */
	public long getCounter() {
		return counter;
	}

	/**
	 * @return Returns the identifier.
	 */
	public BigInteger getIdentifier() {
		return identifier;
	}

}
