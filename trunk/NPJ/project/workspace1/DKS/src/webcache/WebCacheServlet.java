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

/**
 * The <code>WebCacheServlet</code> class
 *
 * @author Ahmad Al-Shishtawy
 * @version $Id: WebCacheServlet.java 294 2006-05-05 17:14:14Z alshishtawy $
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class WebCacheServlet extends HttpServlet{


	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -2458272845442081963L;
	private ArrayList<String> nodes;

	public WebCacheServlet() {
		nodes = new ArrayList<String>();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);

//		System.out.println(req.getRequestURL());
//		System.out.println(req.getContextPath());
//		System.out.println(req.getPathInfo());
//		System.out.println(req.getQueryString());

		if(!req.getMethod().equals("GET")) {
			PrintWriter out = response.getWriter();
			out.println("Invalid Request");
			return;
		}

		PrintWriter out = response.getWriter();
		Map<String, String> reqMap = req.getParameterMap();

		if(reqMap.containsKey("reset")) {
			nodes.clear();
			System.out.println("WebCache Cleared");
			return;
		}
		
		if(reqMap.containsKey("addDKSRef")) {
			nodes.add(req.getParameter("addDKSRef"));
			System.out.println(req.getParameter("addDKSRef"));
			return;
		}

//		generateResponse(response.getWriter());
		Collections.shuffle(nodes);
		for(int i = nodes.size()-1; i>= 0; i--) {
		String dksRef = nodes.get(i);
		//for (String dksRef : nodes) {
			out.println(dksRef);
			out.println("<br />");
		}

	}


}
