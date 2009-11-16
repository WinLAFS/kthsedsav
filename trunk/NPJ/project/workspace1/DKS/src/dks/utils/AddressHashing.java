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

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The <code>AddressHashing</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: AddressHashing.java 122 2006-11-21 18:46:23Z Roberto $
 */
public class AddressHashing {

	/**
	 * 
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	public static byte[] hash(InetAddress ip, int port) {
		MessageDigest md=null;
		try {
			md = MessageDigest.getInstance("SHA");
			md.update(ip.getAddress());
			md.update((new Integer(port).toString().getBytes()));
		} catch (NoSuchAlgorithmException e) {
			// TODO Handle the exception
		}
		return md.digest();
	}

}
