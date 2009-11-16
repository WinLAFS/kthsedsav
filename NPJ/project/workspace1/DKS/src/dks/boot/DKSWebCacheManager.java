/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;

/**
 * The <code>WebCacheManager</code> class Class used for publishing what's
 * needed in the public WebCache
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSWebCacheManager.java 191 2007-02-09 17:55:28Z Roberto $
 */
public class DKSWebCacheManager {

	private String publishAddres;

	/*#%*/ private static Logger log = Logger.getLogger(DKSWebCacheManager.class);

	/**
	 * Initialize the WebCacheManager
	 * 
	 * @param publishAddres
	 *            The public address of the WebCache
	 */
	public DKSWebCacheManager(String publishAddres) {
		this.publishAddres = publishAddres + "webcache.php?";
	}

	public void publishDKSParameters() {
		DKSParameters dksParameters = ComponentRegistry.getDksParameters();
		String command = "addDKSParameters=" + "K:" + dksParameters.K + "L:"
				+ dksParameters.L;
		/*#%*/ 	if (
				updateWebCache(command)
		/*#%*/ 		) {
				;
		/*#%*/ 	log.info("DKS Parameters published");
		/*#%*/ } else {
		/*#%*/ 	log.info("DKS Parameters cannot be published");
		/*#%*/ }
	}

	public void publishDKSRef(DKSRef dksRef) {
		String command = "addDKSRef=" + dksRef;
		/*#%*/ if (
				updateWebCache(command)
		/*#%*/ 		) {
				;
		/*#%*/ log.info("DKS Reference " + dksRef + " published");
		/*#%*/ } else {
		/*#%*/ 	log.info("DKS Reference " + dksRef + " cannot be published");
		/*#%*/ }

	}

	public void reset() {
		String command = "reset=reset";
		/*#%*/ if (
				updateWebCache(command)
		/*#%*/ 		) {
			;
		/*#%*/ 	log.info("Webcache reset");
		/*#%*/ } else {
		/*#%*/ 	log.info("Webcache cannot be reset");
		/*#%*/ }
	}

	private boolean updateWebCache(String command) {
		URL url = null;
		URLConnection con = null;
		BufferedReader in;

		try {
			// System.out.println("Pub " + publishAddres);
			url = new URL(publishAddres + command);
			con = url.openConnection();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.equalsIgnoreCase("UPDATE-OK"))
					return true;
			}
			in.close();
		} catch (MalformedURLException e) {
			/*#%*/ log.error("The address for publishing does not exist ");
			// + url.toString());
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			/*#%*/ log.error("The address for publishing does not exist");
			return false;
		}

		return false;
	}

	public String getFirstDKSRef() {
		URL url = null;
		URLConnection con = null;
		BufferedReader in;
		try {
			/*#%*/ log.debug("Contacting web cache");
			url = new URL(publishAddres);
			con = url.openConnection();
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				return line;
			}
			in.close();
		} catch (MalformedURLException e) {
			/*#%*/ log.error("The DKSRef cannot be retrieved " + url.toString());
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			/*#%*/ log.error("The address for retrieving the DKSRef doesn't exist");
			return null;
		}
		return null;
	}
}
