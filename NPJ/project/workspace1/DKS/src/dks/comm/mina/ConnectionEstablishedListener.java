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
import static dks.comm.mina.CommunicationConstants.PERMANENT_COUNTER;
import static dks.comm.mina.CommunicationConstants.TRANSPORT_PROTOCOL;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
//import org.apache.mina.common.ConnectFuture;
//import org.apache.mina.common.IoFuture;
//import org.apache.mina.common.IoFutureListener;
//import org.apache.mina.common.IoSession;

import dks.addr.DKSRef;
import dks.comm.SendJob;
import dks.comm.mina.events.CommFailedEvent;
import dks.comm.mina.events.CommSentEvent;
import dks.messages.Message;

/**
 * The <code>ConnectionEstablishedListener</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: ConnectionEstablishedListener.java 294 2006-05-05 17:14:14Z
 *          roberto $
 */
public class ConnectionEstablishedListener implements
		IoFutureListener<IoFuture> {

	/*#%*/ private static Logger log = Logger.getLogger(CommunicationHandler.class);

	private CommunicationComponent communicationComponent;

	private DKSRef endPoint;

	private List<Message> pendingMessages;
	private List<SendJob> pendingJobs;

	private boolean mustBePermanent = false;

	private BigInteger endPointId;

	private TransportProtocol protocol;

	public ConnectionEstablishedListener(DKSRef endPoint,
			TransportProtocol protocol,
			CommunicationComponent communicationComponent) {
		this.endPoint = endPoint;
		this.endPointId = endPoint.getId();
		this.protocol = protocol;
		this.communicationComponent = communicationComponent;
		this.pendingMessages = new LinkedList<Message>();
		this.pendingJobs = new LinkedList<SendJob>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.common.IoFutureListener#operationComplete(org.apache.mina.common.IoFuture)
	 */
	public void operationComplete(IoFuture future) {
		ConnectFuture connFuture = (ConnectFuture) future;
		IoSession session = null;
		if (connFuture.isConnected()) {

			session = future.getSession();

			boolean duplicate = false;

			if (communicationComponent.alreadyConnected(endPointId, protocol)) {

				// TODO Solve duplicate connection

				session = communicationComponent.getIoSession(endPointId,
						protocol);

				duplicate = true;
			}

			/*
			 * Removing from pending connections and adding to sessions right
			 * map
			 */
			communicationComponent.pendingConnections.remove(endPointId);

			if (!duplicate) {

				communicationComponent.addToSessionsMap(endPointId, session,
						protocol);

				session.setAttribute(CONNECTION_ENDPOINT, endPoint);

				Integer counter = new Integer(0);

				if (mustBePermanent) {

					counter++;

					// Message message=new
					// SetConnectionStatusMessage(mustBePermanent);
					// message.setSource(communicationComponent.myRef);
					// System.out.println("SENT MESSAGE FOR PERMANENCY:
					// "+mustBePermanent+ " to" +endPoint);

					// session.write(message);

				}

				session.setAttribute(PERMANENT_COUNTER, counter);

				session.setAttribute(TRANSPORT_PROTOCOL, protocol);

				session.setAttribute(MESSAGES_SENT, new Integer(0));

				session.setAttribute(MESSAGES_RECEIVED, new Integer(0));

			}

			/*#%*/ log.debug("Connected to:" + session.getRemoteAddress());

			/* Sending queued messages */
			WriteFuture writeFuture;
			for (Message message : pendingMessages) {

				/*#%*/ log.debug("Sending message "
				/*#%*/ 		+ message.getClass().getSimpleName() + " to  "
				/*#%*/ 		+ endPoint);

				writeFuture = session.write(message);
				CommSentEvent commSentEvent = new CommSentEvent(endPoint);
				commSentEvent.setAttachment(new SendJob(message.getClass().getSimpleName()).setWriteFuture(writeFuture));
				communicationComponent.trigger(commSentEvent, true);
			}
			for (SendJob sendJob : pendingJobs) {

				/*#%*/ log.debug("Sending message "
				/*#%*/ 		+ sendJob.getMessage().getClass().getSimpleName() + " to  "
				/*#%*/ 		+ endPoint);

				writeFuture = session.write(sendJob.getMessage());
				CommSentEvent commSentEvent = new CommSentEvent(endPoint);
				sendJob.setWriteFuture(writeFuture);
				commSentEvent.setAttachment(sendJob);
				communicationComponent.trigger(commSentEvent, true);
			}

		} else {

			communicationComponent.pendingConnections.remove(endPointId);

			/*#%*/ if(session != null) {
			/*#%*/ 	log.debug("Could not connect to node " + endPointId + " at "+ session.getRemoteAddress());
			/*#%*/ } else {
			/*#%*/ 	log.debug("Could not connect to node " + endPointId);
			/*#%*/ }
			
			/*#%*/ for (Message message : pendingMessages) {			
				
				/*#%*/ log.debug("Dropping message "
				/*#%*/ 			+ message.getClass().getSimpleName() 
				/*#%*/ 			+ " that could not be delivered to "
				/*#%*/ 			+ endPointId
				/*#%*/ 			);

			/*#%*/ }
			/*#%*/ boolean bouncingMessages = false;
			for (SendJob sendJob: pendingJobs) {
				
				int operationId = sendJob.getOperationId();
				
				/*#%*/ log.debug(
				/*#%*/ 		"Bouncing message "
				/*#%*/ 		+ sendJob.getMessage().getClass().getSimpleName() 
				/*#%*/ 		+ " with id "
				/*#%*/ 		+ operationId
				/*#%*/ 		+ " that could not be delivered to "
				/*#%*/ 		+ endPointId
				/*#%*/ );
				/*#%*/ 

				CommFailedEvent commFailedEvent = new CommFailedEvent(endPoint);
				sendJob.setConnectionFailure(true);
				commFailedEvent.setAttachment(sendJob);
				communicationComponent.trigger(commFailedEvent, true);
			}
			/*#%*/ if(bouncingMessages) {
			/*#%*/ 		System.err.println("Bouncing messages "
			/*#%*/	 		+ " that could not be delivered to "
			/*#%*/			+ endPointId
			/*#%*/		);
			/*#%*/ }
		}
	}

	public void addPendingMessage(Message message) {
		pendingMessages.add(message);
	}
	public void addPendingJob(SendJob sendJob) {
		pendingJobs.add(sendJob);
	}

	public void setMustBePermanent() {
		this.mustBePermanent = true;
	}

}
