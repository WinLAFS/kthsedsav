/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package yass.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dks.test.niche.NicheServlet;

/**
 * The <code>Servlet</code> class
 * 
 * @author Ahmad
 * @author Joel
 * @version $Id: Servlet.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class HealingTestServlet extends NicheServlet {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -5257023922102951404L;

	//remember empty constructor!
	public HealingTestServlet() {
		super("/" + HealingTest.HEALING_TEST_NAME);
		System.out.println("Servlet STARTED without PROPS");
	}
	public HealingTestServlet(Properties props) {
		super("/" + HealingTest.HEALING_TEST_NAME);
		this.props = props;
		System.out.println("Servlet STARTED");
		//setContext();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

		long currentLocalTime = System.currentTimeMillis();
		props.put(HealingTest.CURRENT_LOCAL_TIME, ""+currentLocalTime);
		//System.out.println("i put " + currentLocalTime + " " + props.toString());
		getRingProperties(response.getWriter());

		//Do NOT delete entries from the prop, just keep sending/resending them...
		
//		ArrayList<String> toRemove = new ArrayList<String>();
//		Set<Entry<Object, Object>> set = props.entrySet();
//		for (Entry<Object, Object> entry : set) {
//			if(!entry.getValue().equals("P") && !entry.getKey().equals("lookup") && !entry.getKey().equals("transfer") && !entry.getKey().equals("done")) {
//				toRemove.add((String)entry.getKey());
//			}
//		}
//		
//		
//		for (String key : toRemove) {
//			props.remove(key);
//		}
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
