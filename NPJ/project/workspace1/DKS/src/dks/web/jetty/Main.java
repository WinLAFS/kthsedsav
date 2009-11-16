/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.web.jetty;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.PropertyConfigurator;

import dks.DKSParameters;
import dks.addr.DKSRef;

/**
 * The <code>Main</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: Main.java 294 2006-05-05 17:14:14Z roberto $
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		DKSRef ref=null;
		try {
			ref = new DKSRef(InetAddress.getByName("127.0.0.1"), 12345,
					BigInteger.ONE);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JettyServer jettyServer = new JettyServer(ref);

		jettyServer.addServlet(new DKSInfoServlet(ref,new DKSParameters(4,3),null), "/info");
		
		try {
			jettyServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
