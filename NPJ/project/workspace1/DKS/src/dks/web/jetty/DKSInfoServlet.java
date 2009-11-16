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

import static dks.comm.mina.CommunicationConstants.CONNECTION_ENDPOINT;
import static dks.comm.mina.CommunicationConstants.MESSAGES_RECEIVED;
import static dks.comm.mina.CommunicationConstants.MESSAGES_SENT;
import static dks.comm.mina.CommunicationConstants.PERMANENT_COUNTER;
import static dks.comm.mina.CommunicationConstants.TRANSPORT_PROTOCOL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.mina.core.session.IoSession;

//import org.apache.mina.common.IoSession;

import dks.DKSParameters;
import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.comm.mina.CommunicationComponent;
import dks.comm.mina.TransportProtocol;
import dks.ring.RingMaintenanceComponentInt;
import dks.ring.RingState;
import dks.router.GenericRoutingTableInterface;
import dks.router.Router;
import dks.router.RoutingTableEntry;
import dks.stats.NodeStatistics;
import dks.utils.RingSuccessorListOrder;
import dks.web.jetty.util.SessionModuloOrder;

/**
 * The <code>DKSInfoServlet</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: DKSInfoServlet.java 294 2006-05-05 17:14:14Z roberto $
 */
public class DKSInfoServlet extends HttpServlet {

	private static final long serialVersionUID = -3193453691581023645L;

	private RingMaintenanceComponentInt ringMaintainer;

	private DKSRef myDKSRef;

	private DKSParameters dksParameters;

	private CommunicationComponent communicator;

	private Router router;

	private String osName;

	private String osVersion;

	private String osArch;

	private String jvmVersion;

	private String jvmName;

	private String hostname;

	private String hostip;

	private ComponentRegistry registry;

	public DKSInfoServlet(DKSRef myDKSRef, DKSParameters dksParameters,
			Router router) {
		super();
		this.myDKSRef = myDKSRef;
		this.dksParameters = dksParameters;
		this.router = router;

		this.registry = ComponentRegistry.getInstance();
		this.ringMaintainer = registry.getRingMaintainerComponent();
		this.communicator = registry.getCommunicatorComponent();

		osName = System.getProperty("os.name");
		osVersion = System.getProperty("os.version");
		osArch = System.getProperty("os.arch");
		jvmVersion = System.getProperty("java.vm.version");
		jvmName = System.getProperty("java.vm.name");
		hostname = myDKSRef.getIp().getHostName();
		hostip = myDKSRef.getIp().getHostAddress();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		// response
		// .addHeader("link",
		// " href=\"http://dks.sics.se/bt2_main.css\rel=\"stylesheet
		// type=\"text/css");

		printInfo(response.getWriter());

	}

	private void printInfo(PrintWriter out) {
		// out
		// .write("<head><link
		// href=\"http://dks.sics.se/bt2_main.css\rel=\"stylesheet\"
		// type=\"text/css\"></head><body>");

		out.write("<h1>Node: " + htmlUrl(myDKSRef) + "</h1>");

		out
				.write("<table cellspacing=8 cellpadding=10><tbody valign=\"top\"><tr><th>Routing Info</th><th>Statistics</th>"
						+ "<th>System Info</th></tr>");

		out.write("<tr><td>");

		// Reading the current status
		RingState ringState = ringMaintainer.getRingState();

		out.write("<b>Node Status</b>: " + ringState.status + "<br>");
		// out.write("<b>Node Lock</b>: "
		// + (ringState.lockTaken ? "TAKEN" : "FREE") + "<br>");
		out.write("<b>Successor</b>:   " + htmlUrl(ringState.successor)
				+ "<br>");
		out.write("<b>Predecessor</b>: " + htmlUrl(ringState.predecessor)
				+ "<br>");

		out.write("<b>Stabilization Running</b>: "
				+ ringMaintainer.isStabilizationRunning() + "<br>");
		// LinkedList<DKSRef> successorList = null;
		//
		// try {
		// successorList = new LinkedList<DKSRef>(ringState.successorList
		// .getSuccessorsList());
		//
		// out.flush();
		//
		// RingSuccessorListOrder.order(successorList, myDKSRef);
		//
		// } catch (NullPointerException e) {
		// System.out.println(""
		// + ringState.successorList.getSuccessorsList().toString());
		//		}
		out.write("<br><b>Successors List</b>: <br>");

		StringBuffer string = new StringBuffer();
		
		for (Iterator<DKSRef> iter = ringState.successorList
				.getSuccessorsList().iterator(); iter.hasNext();) {
			DKSRef ref = (DKSRef) iter.next();
			string.append(htmlUrl(ref));
			if (iter.hasNext())
				string.append(",");
		
		}
		out.write(string.toString());

		// out.flush();

		// Printing Routing Table

		GenericRoutingTableInterface routingTable = getRouterComponent()
				.getRoutingTable();

		long intervals = routingTable.getIntervalsNumber();
		out.write("<br>");
		out.write("<br><b>Router used:</b> "
				+ getRouterComponent().getClass().getSimpleName() + "<br>");

		out.write("<b>Topology Maintenance running</b>: "
				+ router.isTopoloMaintenanceRunning() + "<br>");

		out
				.write("<a href=\"http://" + myDKSRef.getIp().getHostAddress()
						+ ":" + myDKSRef.getPort()
						+ "/lookups\">Lookups information</a><br>");

		out
				.write("<br><DIV ALIGN=\"CENTER\"><table CELLSPACING=\"12\" VALIGN=TOP><tr><td><br><b>Routing Table</b><br>");
		out.write("<table VALIGN=TOP cellspacing=1 cellpadding=1>"
				+ " <tbody valign=\"top\"> " + "<tr><th>i</th>"
				+ "<th>f(i)</th>" + "<th>Finger</th>" + "</tr>");

		for (long i = 1; i <= intervals; i++) {
			printRoutingEntry(routingTable.getRoutingTableEntry(i), out);
		}
		out.write("</tbody> </DIV></table></td>");

		out.write("<td valign=\"top\"><br><b>BackList</b><br>");
		out
				.write("<table cellspacing=1 cellpadding=1>"
						+ " <tbody valign=\"top\"> " + "<tr><th>Pointer</th>"
						+ "</tr>");

		List<DKSRef> backList = getRouterComponent().getBackList();

		RingSuccessorListOrder.order(backList, myDKSRef);

		for (DKSRef ref : backList) {
			out.write("<tr><td><font size=\"2\">");
			out.write(htmlUrl(ref));
			out.write("</font></td></tr>");
		}
		out.write("</tbody></table></td></tr></table></DIV>");

		out.write("</td><td>");

		out.write("<b>Msg Sent</b>: " + NodeStatistics.messagesSent + "<br>");
		// out.write("<b>Failed</b>: " + ci.msgsFailed + "<br>");
		out.write("<b>Msg Received</b>: " + NodeStatistics.messagesReceived
				+ "<br>");

		// Connections statistics
		out
				.write("<br><DIV ALIGN=\"CENTER\"><b>Connections Statistics</b> </DIV><br>");
		// out.flush();

		Set<IoSession> sessions = communicator.getAllSessions();

		List<IoSession> permanent = new LinkedList<IoSession>();

		List<IoSession> temporary = new LinkedList<IoSession>();

		for (IoSession ioSession : sessions) {

			int count = (Integer) ioSession.getAttribute(PERMANENT_COUNTER);

			if (count > 0) {
				permanent.add(ioSession);
			} else {
				temporary.add(ioSession);
			}

		}

		out.write("<b>Permanent conns</b>: " + permanent.size() + "<br>");

		out.write("<b>Temporary conns</b>: " + temporary.size() + "<br>");

		out.write("<b>Total conns</b>: "
				+ (permanent.size() + temporary.size()) + "<br><br>");

		// out.write("<table cellspacing=8 cellpadding=1 >"
		// + " <tbody valign=\"top\"> " + "<tr><th>PeerID</th>"
		// + "<th>MS</th>" + "<th>MR</th>"
		// + "<th>UM</th>" + "<th>RTO</th>" + "<th>FD status</th>"
		// + "</tr>");

		out.write("<table cellspacing=8 cellpadding=1 >"
				+ " <tbody valign=\"top\"> " + "<tr><th>PeerID</th>"
				+ "<th>CT</th>" + "<th>MS</th>" + "<th>MR</th>" + "</tr>");

		out.flush();

		SessionModuloOrder.orderSessions(permanent, myDKSRef);

		// Permanent Connections
		out.write("<tr><td><font size=\"3\">Permanent</font></td></tr>");
		if (permanent != null) {
			for (IoSession session : permanent) {
				printSession(session, out);
			}
		}

		SessionModuloOrder.orderSessions(temporary, myDKSRef);

		// Permanent Connections
		out.write("<tr><td><font size=\"3\">Temporary</font></td></tr>");
		if (temporary != null) {
			for (IoSession session : temporary) {
				printSession(session, out);
			}
		}

		out.write("</tbody></table>");

		out.write("</td><td>");

		// out.flush();

		out.write("<b>OS</b>: " + osName + " " + osVersion + " (" + osArch
				+ ")<br>");
		out.write("<b>JVM</b>: " + jvmName + "<br>");
		out.write("<b>JVM Version</b>: " + jvmVersion + "<br>");
		out.write("<b>Hostname</b>: " + hostname + "<br>");
		out.write("<b>IP Address</b>: " + hostip + "<br>");
		out.write("<b>N</b>: " + dksParameters.N + "<br>");
		out.write("<b>K</b>: " + dksParameters.K + "<br>");
		out.write("<b>L</b>: " + dksParameters.L + "<br>");

		// out.flush();

		out.write("</td></tr></tbody>");
		out.write("</table>");
		out.write("</body>");
		out.flush();
	}

	private void printSession(IoSession session, PrintWriter out) {
		out.write("<tr><td><font size=\"2\">");
		out.write(htmlUrl((DKSRef) session.getAttribute(CONNECTION_ENDPOINT)));
		out.write("</font></td><td><font size=\"2\">");
		out.write(""
				+ (TransportProtocol) session.getAttribute(TRANSPORT_PROTOCOL));
		out.write("</font></td><td><font size=\"2\">");
		out.write("" + (Integer) session.getAttribute(MESSAGES_SENT));
		out.write("</font></td><td><font size=\"2\">");
		out.write("" + (Integer) session.getAttribute(MESSAGES_RECEIVED));
		// out.write("</font></td><td><font size=\"2\">");
		// out.write("" + (Integer) session.getAttribute(PERMANENT_COUNTER));
		out.write("</font></td></tr>");

	}

	private void printRoutingEntry(RoutingTableEntry entry, PrintWriter out) {
		out.write("<tr><td><font size=\"2\">");
		out.write(String.valueOf(entry.getIntervalNumber()));
		out.write("</font></td><td><font size=\"2\">");
		out.write(entry.getIntervalStartId().toString());
		out.write("</font></td><td><font size=\"2\">");

		out.write((entry.getIntervalPointer() == null ? "null" : htmlUrl(entry
				.getIntervalPointer())));
		out.write("</font></td></tr>");
		out.flush();
	}

	public String htmlUrl(DKSRef dksRef) {
		if (dksRef == null) {
			return "null";
		} else
			return "<a href=\"" + dksRef.getDKSWebURL() + "\"> "
					+ dksRef.getId() + "</a>";
	}

	private Router getRouterComponent() {
		return this.router = registry.getRouterComponent();
	}

}
