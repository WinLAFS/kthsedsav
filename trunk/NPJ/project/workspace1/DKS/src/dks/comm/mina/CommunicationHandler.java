/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.comm.mina;

import static dks.comm.mina.CommunicationConstants.CONNECTION_ENDPOINT;
import static dks.comm.mina.CommunicationConstants.MESSAGES_RECEIVED;
import static dks.comm.mina.CommunicationConstants.MESSAGES_SENT;
import static dks.comm.mina.CommunicationConstants.NEWLY_CREATED;
import static dks.comm.mina.CommunicationConstants.PERMANENT_COUNTER;
import static dks.comm.mina.CommunicationConstants.TRANSPORT_PROTOCOL;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Iterator;

import org.apache.log4j.Logger;
//import org.apache.mina.common.IoHandlerAdapter;
//import org.apache.mina.common.IoSession;
//import org.apache.mina.common.IoHandlerAdapter;
//import org.apache.mina.common.IoSession;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;

import dks.addr.DKSRef;
import dks.comm.mina.messages.Close;
import dks.messages.Message;
import dks.stats.NodeStatistics;

/**
 * The <code>CommunicationHandler</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: CommunicationHandler.java 294 2006-05-05 17:14:14Z roberto $
 */
public class CommunicationHandler extends IoHandlerAdapter {

	/*#%*/ private static Logger log = Logger.getLogger(CommunicationHandler.class);

	public CommunicationComponent communicationComponent;

	private boolean TCP_NODELAY;

	public CommunicationHandler(CommunicationComponent communicationComponent, boolean nodelay) {
		super();

		this.communicationComponent = communicationComponent;

	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {

		session.setAttribute(NEWLY_CREATED, true);
		SocketSessionConfig sessionConf = (SocketSessionConfig) session.getConfig();
		sessionConf.setTcpNoDelay(TCP_NODELAY);

	}

	@Override
	public void messageReceived(IoSession session, Object messageObject)
			throws Exception {

		boolean duplicate = false;

		Message message = (Message) messageObject;

		/*
		 * If hello message, i.e. first message sent through the connection, put
		 * the session into the right sessions map
		 */
		if (!session.containsAttribute(CONNECTION_ENDPOINT)) {

			/* Unflatten Id from message and create DKSRef */
			InetSocketAddress address = (InetSocketAddress) session
					.getRemoteAddress();

			DKSRef source = new DKSRef(address.getAddress(), message
					.getSourcePort(), new BigInteger(message.getFlattenedId()));

			/* Set the endpoint in the session's attributes */
			session.setAttribute(CONNECTION_ENDPOINT, source);

			/* Set permanent counter in the session's attributes */
			session.setAttribute(PERMANENT_COUNTER, new Integer(0));

			TransportProtocol protocol = null;

			/* Find out used transport protocol */
			if (session.getService().getClass().equals(
					NioDatagramAcceptor.class)) {

				protocol = TransportProtocol.UDP;

			} else {

				protocol = TransportProtocol.TCP;

			}

			/*#%*/ log.debug(protocol + " connection recognized " + source);

			session.setAttribute(TRANSPORT_PROTOCOL, protocol);

			session.setAttribute(MESSAGES_SENT, new Integer(0));

			session.setAttribute(MESSAGES_RECEIVED, new Integer(0));

			communicationComponent.addToSessionsMap(source.getId(), session,
					protocol);

		} else if (session.getAttribute(NEWLY_CREATED) != null) {

			session.removeAttribute(NEWLY_CREATED);

			/* Duplicate connection breaking */
			// TODO
			duplicate = true;
		}

		DKSRef ref = (DKSRef) session.getAttribute(CONNECTION_ENDPOINT);

		TransportProtocol protocol = (TransportProtocol) session
				.getAttribute(TRANSPORT_PROTOCOL);

		if (duplicate &&  communicationComponent.hasIoSession(ref.getId(), protocol)) {
			//getIoSession can still be null, obviously, therefor check added
			
			/* Get the already existing session */
			session = communicationComponent
					.getIoSession(ref.getId(), protocol);
	
			
		}

		/*#%*/ log.debug("Message " + message.getClass().getSimpleName()
		/*#%*/ 		+ " received from: " + ref);

		/* Session Stats */
		Integer received = (Integer) session.getAttribute(MESSAGES_RECEIVED);
		session.setAttribute(MESSAGES_RECEIVED, received + 1);

		/* If from connection garbage collection, close the connection */
		if (message.getClass().equals(Close.class)) {

			if (communicationComponent.sessionsTCP.containsKey(ref.getId())) {

				communicationComponent.sessionsTCP.remove(ref.getId());

			} else if (communicationComponent.sessionsUDP.containsKey(ref
					.getId())) {

				communicationComponent.sessionsUDP.remove(ref.getId());

			}
			session.close();

		} else {

			/* Deliver message */
			communicationComponent.deliverMessage(ref, message, protocol);

		}

	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		
		//FIXME
		/*#%*/ log.warn("Connection to " + session.getRemoteAddress() + " is being closed due to " + cause.getMessage());
		IoSession tempSession;
		
		if (communicationComponent.sessionsTCP.containsValue(session)) {
			
			for (Iterator<IoSession> iterator =
				communicationComponent.sessionsTCP.values().iterator();
					iterator.hasNext();) {
						tempSession = iterator.next();
						if (tempSession.equals(session)) {
							iterator.remove();
							break;
						}
			}
		} else if (communicationComponent.sessionsUDP.containsValue(session)) {
			for (Iterator<IoSession> iterator =
				communicationComponent.sessionsUDP.values().iterator();
					iterator.hasNext();) {
						tempSession = iterator.next();
						if (tempSession.equals(session)) {
							iterator.remove();
							break;
						}
			}
			
		}
			
		//FIXME: check garbage collect 
		session.close();
		
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {

		/* Node Stats */
		NodeStatistics.messagesSent.incrementAndGet();

		/* Session Stats */
		Integer sent = (Integer) session.getAttribute(MESSAGES_SENT);
		session.setAttribute(MESSAGES_SENT, sent + 1);

	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		/*#%*/ log.warn("Connection closed by: " + session.getRemoteAddress());
	}

}
