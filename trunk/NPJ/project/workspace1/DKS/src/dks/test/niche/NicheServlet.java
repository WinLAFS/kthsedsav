/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.test.niche;

import java.util.Properties;

import javax.servlet.http.HttpServlet;

/**
 * The <code>NicheServlet</code> class
 *
 * @version $Id: NicheServlet.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class NicheServlet extends HttpServlet {
	protected String context;
	protected Properties props;

	public NicheServlet() {
	}
	
	public NicheServlet(String context) {
		this.context = context;
	}
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
	
	public void setProperties(Properties props) {
		this.props = props;
	}
	
	

}
