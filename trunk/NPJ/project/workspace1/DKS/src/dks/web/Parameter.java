/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.web;

/**
 * The <code>Parameter</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: Parameter.java 294 2006-05-05 17:14:14Z roberto $
 */
public class Parameter {
	
	private String key;
	
	private String value;
	

	public Parameter(String key,String value) {
		this.key=key;
		this.value=value;
	}

	/**
	 * @return Returns the key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return Returns the value.
	 */
	public String getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return key+"="+value;
	}

	
	
}
