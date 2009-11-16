/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring.tests;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSNode;
import dks.boot.DKSPropertyLoader;

/**
 * The <code>ProfileTest</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ProfileTest.java 294 2006-05-05 17:14:14Z jars $
 */
public class ProfileTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		InputStreamReader cin = new InputStreamReader(System.in);

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		DKSPropertyLoader propertyLoader = new DKSPropertyLoader();

		DKSParameters dksParameters = (propertyLoader).getDKSParameters();
		InetAddress ip = null;
		try {
			ip = propertyLoader.getIP();
		} catch (UnknownHostException e) {
			// TODO Handle the exception
		}
		// ip=AddressResolution.getNonLoopbackInet4Address();
		int port = propertyLoader.getPort();

		// byte[] hash = AddressHashing.hash(ip, port);

		BigInteger bigInt2 = new BigInteger("1023");

		DKSRef ref2 = new DKSRef(ip, port, bigInt2.abs());

		DKSNode node1 = new DKSNode(ref2, dksParameters, propertyLoader
				.getWebcacheAddress());

		node1.getDksImplementation().create();
	}

}
