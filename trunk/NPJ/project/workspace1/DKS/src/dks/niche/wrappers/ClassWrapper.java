/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

/**
 * The <code>ClassWrapper</code> class
 *
 * @author Joel H
 * @version $Id: ClassWrapper.java 294 2006-05-05 17:14:14Z joel $
 */
public class ClassWrapper {

	Object o;
	String m;
	/**
	 * @param o
	 * @param m
	 */
	public ClassWrapper(Object o, String m) {
		super();
		this.o = o;
		this.m = m;
	}
	/**
	 * @return Returns the m.
	 */
	public String getMethod() {
		return m;
	}
	/**
	 * @param m The m to set.
	 */
	public void setMethod(String m) {
		this.m = m;
	}
	/**
	 * @return Returns the o.
	 */
	public Object getObject() {
		return o;
	}
	/**
	 * @param o The o to set.
	 */
	public void setObject(Object o) {
		this.o = o;
	}
	
}
