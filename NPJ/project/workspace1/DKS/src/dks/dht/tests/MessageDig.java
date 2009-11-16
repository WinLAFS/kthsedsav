/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The <code>MessageDigest</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: MessageDigest.java 294 2006-05-05 17:14:14Z roberto $
 */
public class MessageDig {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Object key = "katt";
		BigInteger id = null;
		//calculate the ID of this key in the IDSpace;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(stream).writeObject(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MessageDigest digest=null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		id = new BigInteger(digest.digest(stream.toByteArray())).mod(new BigInteger("1024"));
	
		System.out.println(id);
	}

}
