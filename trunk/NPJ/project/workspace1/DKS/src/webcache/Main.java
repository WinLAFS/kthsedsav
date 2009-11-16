/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package webcache;

import java.net.MalformedURLException;
import java.net.UnknownHostException;

import dks.addr.DKSRef;
import dks.web.jetty.JettyServer;

/**
 * The <code>Main</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: Main.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class Main {

	/**
	 * @param args
	 * @throws UnknownHostException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, UnknownHostException {
		JettyServer jettyServer = new JettyServer(new DKSRef("dks://127.0.0.1:2000/0")); // only the port is important
		jettyServer.addServlet(new WebCacheServlet(), "/webcache");
		try {
			jettyServer.start();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
