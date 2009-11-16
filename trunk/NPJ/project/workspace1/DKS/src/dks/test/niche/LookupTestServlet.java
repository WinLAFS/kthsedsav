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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The <code>RingServlet</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: RingServlet.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class LookupTestServlet extends NicheServlet {

	private static final long serialVersionUID = -4953281452622906223L;

	Properties props;

	public LookupTestServlet(Properties props) {
		this.props = props;
		setContext("/lookup");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		System.out.println("Is this written?");
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		getRingProperties(response.getWriter());
		
		ArrayList<String> toRemove = new ArrayList<String>();
		Set<Entry<Object, Object>> set = props.entrySet();
		for (Entry<Object, Object> entry : set) {
			if(!entry.getValue().equals("P") && !entry.getKey().equals("lookup") && !entry.getKey().equals("transfer") && !entry.getKey().equals("done")) {
				toRemove.add((String)entry.getKey());
			}
		}
		
		
		for (String key : toRemove) {
			props.remove(key);
		}
	}

	private void getRingProperties(PrintWriter out) {

		
//		props.setProperty("successor", ringState.successor.getId().toString());
//		props.setProperty("predecessor", ringState.predecessor.getId().toString());
				
		try {
			synchronized(props) {
				props.store(out, "");
				}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		out.write("STOP");

	}

}
