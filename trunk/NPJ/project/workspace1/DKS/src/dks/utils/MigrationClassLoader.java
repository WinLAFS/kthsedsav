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
 * The <code>MigrationClassLoader</code> class
 *
 * @author Joel
 * @version $Id: MigrationClassLoader.java 294 2006-05-05 17:14:14Z joel $
 */
public class MigrationClassLoader extends ClassLoader {

	public MigrationClassLoader() {
		super(MigrationClassLoader.class.getClassLoader());
	}
	public Object getClass(String name, byte []b) {
    	return defineClass(name, b, 0, b.length);
    }


}

