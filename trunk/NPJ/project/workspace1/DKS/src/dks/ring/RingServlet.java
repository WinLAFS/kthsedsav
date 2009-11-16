/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.ring;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>RingServlet</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingServlet.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class RingServlet extends HttpServlet {

	private static final long serialVersionUID = -4953281452622906223L;

	RingState ringState;

	public RingServlet(RingState ringState) {
		this.ringState = ringState;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		getRingProperties(response.getWriter());
	}

	private void getRingProperties(PrintWriter out) {

		Properties props = new Properties();
		props.setProperty("successor", ringState.successor.getId().toString());
		props.setProperty("predecessor", ringState.predecessor.getId().toString());
		
		
		try {
			props.store(out, "");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		out.write("STOP");

	}

}
