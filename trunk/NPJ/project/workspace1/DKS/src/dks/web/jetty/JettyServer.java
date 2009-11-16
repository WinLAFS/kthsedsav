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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.BoundedThreadPool;

import dks.addr.DKSRef;

/**
 * The <code>JettyServer</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: JettyServer.java 294 2006-05-05 17:14:14Z roberto $
 */
public class JettyServer {

	private final static String WELCOME = "Welcome to the DKS jetty server of node: ";

	private final static int THREAD_NUMBER = 4;

	private Server server;

	private ContextHandlerCollection contexts;

	public JettyServer(DKSRef myDKSRef) {

		server = new Server(myDKSRef.getPort() + 1);

		/* Setting max number of threads to one */
		BoundedThreadPool threadPool = new BoundedThreadPool();
		threadPool.setMaxThreads(THREAD_NUMBER);
		server.setThreadPool(threadPool);

		/* Setting NIO selector as default connector */
		Connector connector = new SelectChannelConnector();
		connector.setPort(myDKSRef.getPort() + 1);
		server.setConnectors(new Connector[] { connector });

		contexts = new ContextHandlerCollection();
		server.setHandler(contexts);

//		/* Default context */
//		Context root = new Context(contexts, "/", Context.SESSIONS);
//		root.addServlet(new ServletHolder(new HelloServlet(myDKSRef)), "/*");
//
//		StatisticsHandler stats = new StatisticsHandler();
//		contexts.addHandler(stats);

	}

	public void start() throws Exception {

		server.start();
	}

	/**
	 * Adds a Servlet to this Server.
	 * 
	 * @param servlet
	 *            the {@link HttpServlet} instance
	 * @param context
	 *            the context to which it belongs
	 */
	public void addServlet(HttpServlet servlet, String context) {

		//System.out.println("servlet is " + servlet + " context is "+ context + " contexts are " + contexts);
		Context addedContext = new Context(contexts, context, Context.SESSIONS);
		addedContext.addServlet(new ServletHolder(servlet), "/*");

	}
	
	public void pausAndAddServlet(HttpServlet servlet, String context) {

		try {
			server.stop();
		//System.out.println("servlet is " + servlet + " context is "+ context + " contexts are " + contexts);
		Context addedContext = new Context(contexts, context, Context.SESSIONS);
		addedContext.addServlet(new ServletHolder(servlet), "/*");
		server.start();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class HelloServlet extends HttpServlet {

		private static final long serialVersionUID = 6361227307639625164L;

		private DKSRef myDKSRef;

		public HelloServlet(DKSRef myDKSRef) {
			this.myDKSRef = myDKSRef;
		}

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().println(
					"<h1>" + WELCOME + "" + myDKSRef + " </h1>");
		}
	}

}
