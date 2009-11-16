/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.web.events;

import dks.arch.Event;

/**
 * The <code>WebRequestEvent</code> class
 *
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: WebRequestEvent.java 126 2006-12-04 16:51:58Z Roberto $
 */
public class WebRequestEvent extends Event {
	
	private String webRequest;

	/**
	 * Event issued when a HTTP GET request is received by the peer
	 */
	public WebRequestEvent(String webRequest) {
		this.webRequest = webRequest;
	}
	
	/**
	 * @return Returns the webRequest.
	 */
	public String getWebRequest() {
		return webRequest;
	}

}
