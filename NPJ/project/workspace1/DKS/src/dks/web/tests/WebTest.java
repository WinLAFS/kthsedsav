/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.web.tests;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.boot.DKSNode;
import dks.boot.DKSPropertyLoader;

/**
 * The <code>WebTest</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: WebTest.java 154 2007-01-24 14:43:31Z Roberto $
 */
public class WebTest {

	/**
	 * 
	 */
	public WebTest() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		int port = propertyLoader.getPort();

		// byte[] hash = AddressHashing.hash(ip, port);

		BigInteger bigInt2 = new BigInteger("1233");

		DKSRef ref2 = new DKSRef(ip, port, bigInt2.abs());

		DKSNode node1 = new DKSNode(ref2, dksParameters, propertyLoader
				.getWebcacheAddress());

		node1.getDksImplementation().create();

	}

}
