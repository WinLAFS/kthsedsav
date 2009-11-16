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

import static dks.comm.mina.CommunicationConstants.MINA_LOGGING_ENABLED;
import static dks.comm.mina.CommunicationConstants.MINA_SERIALIZATION_LOGGING_ENABLED;
import static dks.comm.mina.CommunicationConstants.PERMANENT_COUNTER;
import static dks.comm.mina.CommunicationConstants.SESSION_GARBAGE_COLLECTION_TIMER;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.apache.log4j.Logger;
//import org.apache.mina.common.ConnectFuture;
//import org.apache.mina.common.DefaultIoFilterChainBuilder;
//import org.apache.mina.common.IoConnector;
//import org.apache.mina.common.IoProcessor;
//import org.apache.mina.common.IoService;
//import org.apache.mina.common.IoSession;
//import org.apache.mina.common.WriteFuture;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.apache.mina.transport.socket.nio.NioProcessor;
//import org.apache.mina.transport.socket.nio.NioSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import dks.addr.DKSRef;
import dks.arch.Component;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.EventConsumer;
import dks.arch.EventsRegistry;
import dks.arch.Scheduler;
import dks.comm.MessageInfo;
import dks.comm.SendJob;
import dks.comm.mina.events.CommSendEvent;
import dks.comm.mina.events.CommSentEvent;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.comm.mina.events.SessionGarbageCollectionEvent;
import dks.comm.mina.events.SetConnectionStatusEvent;
import dks.comm.mina.messages.Close;
import dks.comm.mina.messages.SetConnectionStatusMessage;
import dks.messages.Message;
//import dks.niche.hiddenEvents.NicheCommSentEvent;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.messages.SendToNodeMessage;
import dks.stats.NodeStatistics;
import dks.timer.TimerComponent;
import dks.utils.serialization.DebugObjectSerializationCodecFactory;
import dks.utils.serialization.DebugSimpleBufferAllocator;

/**
 * The <code>CommunicationComponent</code> class
 * 
 * @author Roberto Roverso
 * @author Cosmin Arad
 * @version $Id: CommunicationComponent.java 294 2006-05-05 17:14:14Z roberto $
 */
public class CommunicationComponent extends Component {

	/*#%*/ protected static Logger log = Logger.getLogger(CommunicationComponent.class);

	protected DKSRef myRef;

	private CommunicationHandler communicationHandler;

	private EventsRegistry eventRegistry;

	/* Acceptors and connectors */
	protected NioDatagramAcceptor acceptorUDP;

	protected NioDatagramConnector connectorUDP;

	protected NioSocketAcceptor acceptorTCP;

	protected NioSocketConnector connectorTCP;

	/* Sessions maps */
	protected ConcurrentHashMap<BigInteger, IoSession> sessionsUDP;

	protected ConcurrentHashMap<BigInteger, IoSession> sessionsTCP;

	protected ConcurrentHashMap<BigInteger, ConnectionEstablishedListener> pendingConnections;

	private BigInteger myId;

	private TimerComponent timerComponent;
	
	ThreadMXBean bean;
	
	final int MINA_PROCESSOR_COUNT = 
		System.getProperty("dks.comm.minaProcessorCount") instanceof String ?
				Integer.parseInt(System.getProperty("dks.comm.minaProcessorCount"))
			:
				-1
	;
				
	final int reuseMainThreadPool = 
			System.getProperty("dks.comm.reuseMainThreadPool") instanceof String ?
					Integer.parseInt(System.getProperty("dks.comm.reuseMainThreadPool"))
				:
					0
	;

	final int TEST_MESSAGE_SIZE = 
		System.getProperty("dks.comm.testMessageSize") instanceof String ?
				Integer.parseInt(System.getProperty("dks.comm.testMessageSize"))
			:
				-1
	;
				
	final boolean TCP_NODELAY =
		System.getProperty("dks.comm.tcpNodelay") instanceof String ?
				0 < Integer.parseInt(System.getProperty("dks.comm.tcpNodelay"))
			:
				false
	;
		

				/*#%*/ private Message lastSeenMessage;
	private Object[] staticPayload = 
	{
		"",
		"1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm",
		
		"1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm"
		+ "1234567890"
		+ "abcdefghijklmnopqrstuvxuz"
		+ "0987654321"
		+ "qrqtqyquqiqoqpqaqsqdqfqgqhqjqkqlqzqxqcqvqbqnqm",
		""
	};
	
	public CommunicationComponent(Scheduler scheduler,
			ComponentRegistry registry, Executor executor, DKSRef myRef) {
		super(scheduler, registry);

		this.myRef = myRef;

		this.myId = myRef.getId();

		this.timerComponent = registry.getTimerComponent();

		this.sessionsUDP = new ConcurrentHashMap<BigInteger, IoSession>();
		this.sessionsTCP = new ConcurrentHashMap<BigInteger, IoSession>();

		this.eventRegistry = registry.getEventsRegistry();

		this.pendingConnections = new ConcurrentHashMap<BigInteger, ConnectionEstablishedListener>();

		NioProcessor nioProcessor = new NioProcessor(executor);
		
		registry.setCommunicatorComponent(this);

		SocketAddress socketAddress = new InetSocketAddress(myRef.getIp(),
				myRef.getPort());

		/* All acceptors and connectors handler */
		communicationHandler = new CommunicationHandler(this, TCP_NODELAY);

		/* UDP Acceptor */

		acceptorUDP = 0 < reuseMainThreadPool ?
				new NioDatagramAcceptor(executor)
				:
				new NioDatagramAcceptor()
		;
				
		initIoService(acceptorUDP, communicationHandler);

		/* Bind */
		acceptorUDP.getSessionConfig().setReuseAddress(true);

		try {

			acceptorUDP.bind(socketAddress);

		} catch (IOException e) {
			e.printStackTrace();
		}

		/* UDP connector */

		connectorUDP = 1 < reuseMainThreadPool ?
				new NioDatagramConnector(nioProcessor)
				:
				new NioDatagramConnector();
		;
		
		initIoService(connectorUDP, communicationHandler);

		/* TCP acceptor */

		acceptorTCP = 0 < reuseMainThreadPool ?
				new NioSocketAcceptor(nioProcessor)
			:
				new NioSocketAcceptor()
		;
				
		initIoService(acceptorTCP, communicationHandler);

		/* Bind */
		acceptorTCP.getSessionConfig().setReuseAddress(true);

		try {

			acceptorTCP.bind(socketAddress);

		} catch (IOException e) {
			e.printStackTrace();
		}

		/* TCP connector */

		/*#%*/ String logMsg;
		if(MINA_PROCESSOR_COUNT < 0) {
			
			if(1 < reuseMainThreadPool) {
				
				connectorTCP = new NioSocketConnector(executor, nioProcessor);
				/*#%*/ logMsg = "Creating connectorTCP which is linked with the main threadpool";
				
			} else {				
				
				connectorTCP = new NioSocketConnector();
				/*#%*/ logMsg = "Creating connectorTCP with default no of threads";
				
			}
		} else {
			
//			if(1 < reuseMainThreadPool) {
//				
//				connectorTCP = new NioSocketConnector(executor, nioProcessor);
//				logMsg = "Creating connectorTCP which is linked with the main threadpool";
//				
//			} else
			{				
				
				connectorTCP = new NioSocketConnector(MINA_PROCESSOR_COUNT);
				/*#%*/ logMsg = "Creating connectorTCP with " + MINA_PROCESSOR_COUNT + " no of threads";
				
			}
		}
		
		/*#%*/ log.debug(logMsg);
		initIoService(connectorTCP, communicationHandler);

		registerForEvents();

		bean = ManagementFactory.getThreadMXBean();
		/* Start timer for connection garbage collection */
		timerComponent.registerTimer(SessionGarbageCollectionEvent.class, null,
				SESSION_GARBAGE_COLLECTION_TIMER);
		
		/*#%*/ lastSeenMessage = new SendToNodeMessage();
		/*#%*/ lastSeenMessage.setPayload(
		/*#%*/ 		staticPayload[2]
		/*#%*/ );

	}

	private void initIoService(IoService service,
			CommunicationHandler communicationHandler) {

		service.setHandler(communicationHandler);

		DefaultIoFilterChainBuilder serviceChain = service.getFilterChain();

		/* We use logging and serialization as filters */

		if (MINA_LOGGING_ENABLED) {
			serviceChain.addLast("logger", new LoggingFilter());
		}

		ProtocolCodecFactory protocolCodecFactory;
		if(MINA_SERIALIZATION_LOGGING_ENABLED) {
			protocolCodecFactory = new DebugObjectSerializationCodecFactory();
			/*#%*/ IoBuffer.setAllocator(new DebugSimpleBufferAllocator(log));
		} else {
			protocolCodecFactory = new ObjectSerializationCodecFactory(); 
		}
		
		serviceChain.addLast(
				"protocol",
				new ProtocolCodecFilter(protocolCodecFactory)
		);

	}

	@Override
	protected void registerForEvents() {

		register(CommSendEvent.class, "handleSendEvent");
		register(SetConnectionStatusEvent.class, "handleSetConnectionStatus");
		register(SessionGarbageCollectionEvent.class,
				"handleSessionGarbageCollectionEvent");

	}

	public void handleSendEvent(CommSendEvent event) {

//		log.debug("SENDING:"
//				+ event.getMessage().getClass().getSimpleName()
//				+ " to "
//				+ event.getDestination()
//				+ ((0 < event.getOperationId()) ? " with operationId "
//						+ event.getOperationId() : ""));

		event.getMessage().setSource(myRef);
		send(event.getMessage(), event.getDestination(), event.getProtocol(), event.getSendJob(), false);
//		send(event.getMessage(), event.getDestination(), event.getProtocol(),
//				event.getOperationId(), false);

	}

	public void handleSessionGarbageCollectionEvent(
			SessionGarbageCollectionEvent event) {

//		Object maybeSession = event.getAttachment();
//
//		if (maybeSession instanceof IoSession) {
//			IoSession session = (IoSession) maybeSession;
//			sessionsTCP.values().remove(session);
//			sendClose(session);
//			log.debug("Removing session X");
//			return;
//		}
		
		/*#%*/ log.debug("handleSessionGarbageCollectionEvent");
		/* For all TCP sessions */
		for (Iterator<IoSession> iterator = sessionsTCP.values().iterator(); iterator
				.hasNext();) {
			IoSession session = iterator.next();
			if (isTemporary(session)) {
				iterator.remove();
				/*#%*/ log.debug("handleSessionGarbageCollectionEvent is closing tcp connection to " + session.getRemoteAddress());
				sendClose(session);
			}
		}

		/* For all UDP sessions */
		for (Iterator<IoSession> iterator = sessionsUDP.values().iterator(); iterator
				.hasNext();) {
			IoSession session = iterator.next();
			if (isTemporary(session)) {
				iterator.remove();
				/*#%*/ log.debug("handleSessionGarbageCollectionEvent is closing udp connection to " + session.getRemoteAddress());
				sendClose(session);
			}
		}

		 /* Start timer for connection garbage collection */
//		 timerComponent.registerTimer(SessionGarbageCollectionEvent.class,
//		 null,
//		 SESSION_GARBAGE_COLLECTION_TIMER);

	}

	/**
	 * Sends the message to the specified destination. A connection is created
	 * to the destination node if not already present.
	 * 
	 * @param protocol
	 * 
	 */
	private void send(Message message, DKSRef destination,
			TransportProtocol protocol, SendJob sendJob, boolean mustBePermanent) {

		// if(destination == null) {
		// System.out.println("ERROR: " + message.getClass().getSimpleName() + "
		// from " + message.getSource());
		// }

		message.flattenNodeRef();

		BigInteger destinationId = destination.getId();

		int operationId = 0;
		
		if (destinationId.equals(myId)) {

			/*#%*/ log.debug("DELIVERING:"
			/*#%*/ 		+ message.getClass().getSimpleName()
			/*#%*/ 		+ " locally "
			/*#%*/ 	);


			MessageInfo messageInfo = new MessageInfo(destination, destination,
					null, null, 0);

			DeliverMessageEvent deliverMessageEvent = new DeliverMessageEvent(
					message, messageInfo, protocol);

			
			
			if(message instanceof NicheMessageInterface) {
				CommSentEvent commSentEvent = new CommSentEvent(destination);
				operationId = ((NicheMessageInterface)message).getMessageId();
				sendJob.setLocalOperation(true);
				commSentEvent.setAttachment(sendJob);
				trigger(commSentEvent);
			} //Don't care: else {commSentEvent.setAttachment(new SendJob(message.getClass().getSimpleName()).setLocalOperation(true)); 	}
			
			

			/* Deliver the message */
			if (!deliverToConsumers(deliverMessageEvent)) {
				trigger(deliverMessageEvent);
			}

		} else {			
			
			
			if(message instanceof NicheMessageInterface) {
				operationId = ((NicheMessageInterface)message).getMessageId();
			}

			/* If not connected with the specified protocol */
			if (!alreadyConnected(destinationId, protocol)) {

				/* if in the pending connections */

				ConnectionEstablishedListener cel = pendingConnections
						.get(destinationId);
				if (cel != null) {
					synchronized (cel) {

						/*#%*/ log.debug("Using pending connection to  "
						/*#%*/ 			+ destination
						/*#%*/ 			+ " to send "
						/*#%*/ 			+ message.getClass().getSimpleName()
						/*#%*/ 			+ (
						/*#%*/ 					(0 < operationId) ?
						/*#%*/ 					" with operationId "+ operationId
						/*#%*/ 				:
						/*#%*/ 					""
						/*#%*/ 				)
						/*#%*/ 			);
						
						if(sendJob != null) {
							cel.addPendingJob(sendJob);
						} else {
							cel.addPendingMessage(message);
						}
					}
				} else {

					/*#%*/ log.debug("Creating "
					/*#%*/ 		+ protocol.toString()
					/*#%*/ 		+ " connection to  "
					/*#%*/ 		+ destination
					/*#%*/ 		+ " to be able to send "
					/*#%*/ 		+ message.getClass().getSimpleName()
					/*#%*/ 		+ (
					/*#%*/ 				(0 < operationId) ?
					/*#%*/ 				" with operationId "+ operationId
					/*#%*/ 				:
					/*#%*/ 				""
					/*#%*/ 		  )
					/*#%*/ 	
					/*#%*/ 		);

					/* connection not initiated, connecting now */

					/*
					 * Connect using the right connector (according to the
					 * chosen transport protocol)
					 */
					ConnectFuture connFuture = getConnector(protocol).connect(
							new InetSocketAddress(destination.getIp(),
									destination.getPort()));

					/* Create listener for the connection */
					ConnectionEstablishedListener listener = new ConnectionEstablishedListener(
							destination, protocol, this);

					if (mustBePermanent) {
						listener.setMustBePermanent();
					}

					/* Enqueue the message for later transmission */
					
					if(sendJob != null) {
						listener.addPendingJob(sendJob);
					} else {
						listener.addPendingMessage(message);
					}
					
					/* keep track of listeners for adding other pending messages */
					pendingConnections.put(destinationId, listener);

					/*
					 * Add listener to be executed when connection is
					 * established, in our case is always the communication
					 * handler
					 */
					connFuture.addListener(listener);
				}

			} else {

				/*
				 * write the message in the right session
				 */
				/*#%*/ if(0 < TEST_MESSAGE_SIZE) {
									
				/*#%*/ 	if(4 < TEST_MESSAGE_SIZE) {
				/*#%*/ 		lastSeenMessage.setPayload("");
				/*#%*/ 		message.setPayload(lastSeenMessage);
				/*#%*/ 	} else {
				/*#%*/ 		message.setPayload(staticPayload[TEST_MESSAGE_SIZE-1]);
				/*#%*/ 	}
				/*#%*/ }
				long beforeSend, afterSend;
				long beforeSendProc, afterSendProc;
				IoSession session = getIoSession(destinationId, protocol);
				WriteFuture writeFuture;
				/*#%*/ log.debug("Starting timer");
				/*#%*/ synchronized (session) {
					
				/*#%*/ beforeSendProc = bean.getCurrentThreadCpuTime();
				/*#%*/ beforeSend = System.currentTimeMillis();
					writeFuture = session.write(message);
					/*#%*/ 	afterSend = System.currentTimeMillis();
					/*#%*/ 	afterSendProc = bean.getCurrentThreadCpuTime();
					
					
					/*#%*/ }
				
				/*#%*/ log.debug("Has sent:"
				/*#%*/ 	+ message.getClass().getSimpleName()
				/*#%*/ 	+ " to "
				/*#%*/ 	+ destination
				/*#%*/ 	+ (
				/*#%*/ 			(0 < operationId) ?
				/*#%*/ 			" with operationId "+ operationId
				/*#%*/ 			:
				/*#%*/ 			""
				/*#%*/ 	  )
				/*#%*/ 	+ " using "
				/*#%*/ 	+ session.getRemoteAddress()
				/*#%*/ 	+ " in "
				/*#%*/ 	+ (afterSend - beforeSend)
				/*#%*/ 	+ " ms in wallclock, or "
				/*#%*/ 	+ ( (afterSendProc - beforeSendProc) / 1000000)
				/*#%*/ 	+ " ms in proctime "
				/*#%*/ );

				CommSentEvent commSentEvent = new CommSentEvent(destination);
				if (0 < operationId) {
					sendJob.setWriteFuture(writeFuture);
					/*#%*/ sendJob.setTimeStamp(afterSend);
					commSentEvent.setAttachment(sendJob);
				} else {
					commSentEvent.setAttachment(new SendJob(message.getClass().getSimpleName()).setWriteFuture(writeFuture));
				}
				trigger(commSentEvent);
				/*#%*/ lastSeenMessage = message;
				// if( != null) {
				// writeFuture = wf;
				// }

			}
		} // end outer else

	}

	public void handleSetConnectionStatus(SetConnectionStatusEvent event) {

		DKSRef endPointRef = event.getEndPointReference();

		BigInteger endPointId = endPointRef.getId();

		TransportProtocol protocol = event.getProtocol();

		boolean permanent = event.isPermanent();

		// System.out.println("GOT SETSTATUS " + event.isPermanent() + " FOR "
		// + event.getEndPointReference());

		if (alreadyConnected(endPointId, protocol)) {

			// int preCount = getPermanentCounter(endPointId, protocol);

			updatePermanentCounter(endPointId, protocol, event.isPermanent());

			// System.out.println("Pre count:" + preCount);
			// System.out.println("After count:" + afterCount);

			/* If connection became temporary or permanent */
			// if ((afterCount == 0 && preCount > 0)
			// || (afterCount > 0 && preCount == 0)) {
			// System.out.println("SENT MESSAGE FOR PERMANENCY: " + permanent
			// + " to " + endPointRef);
			// }
		} else if (pendingConnections.containsKey(endPointId)
				&& event.isPermanent()) {

			/*
			 * Setting has to be permanent flag, the permanent connection's
			 * counter will be increased later
			 */
			System.out.println("Pending connection to " + endPointRef);
			pendingConnections.get(endPointId).setMustBePermanent();

		}

		Message message = new SetConnectionStatusMessage(permanent);
		message.setSource(myRef);

		//send(message, endPointRef, protocol, -1, true);
		send(message, endPointRef, protocol, null, true);
	}

	public void deliverMessage(DKSRef source, Message message,
			TransportProtocol protocol) {

		/*
		 * If it's permanent request message, increase the counter for the
		 * specific connection
		 */
		if (message.getClass().equals(SetConnectionStatusMessage.class)) {

			SetConnectionStatusMessage statusMessage = (SetConnectionStatusMessage) message;

			// System.out.println("RECEIVED MESSAGE FOR PERMANENCY: "
			// + statusMessage.isPermanent() + " to " + source);

			updatePermanentCounter(source.getId(), protocol, statusMessage
					.isPermanent());

		} else {

			NodeStatistics.messagesReceived.incrementAndGet();

			/*#%*/ log.debug("Delivering message " + message.getClass() + " from "
			/*#%*/ 		+ source);

			message.setSource(source);

			MessageInfo messageInfo = new MessageInfo(source, myRef, null,
					null, 0);

			DeliverMessageEvent deliverMessageEvent = new DeliverMessageEvent(
					message, messageInfo, protocol);

			deliverToConsumers(deliverMessageEvent);

		}

	}

	protected IoConnector getConnector(TransportProtocol protocol) {
		return ((TransportProtocol.TCP.equals(protocol) ? connectorTCP
				: connectorUDP));
	}

	protected boolean alreadyConnected(BigInteger Id, TransportProtocol protocol) {

		if (protocol.equals(TransportProtocol.UDP)
				&& sessionsUDP.containsKey(Id))
			return true;

		if (protocol.equals(TransportProtocol.TCP)
				&& sessionsTCP.containsKey(Id))
			return true;

		return false;
	}

	protected IoSession getIoSession(BigInteger id, TransportProtocol protocol) {

		if (protocol.equals(TransportProtocol.UDP)
				&& sessionsUDP.containsKey(id)) {
			return sessionsUDP.get(id);
		}

		if (protocol.equals(TransportProtocol.TCP)
				&& sessionsTCP.containsKey(id)) {
			return sessionsTCP.get(id);
		}

		/*#%*/ log.warn("ID not connected in either transport protocols ");

		return null;
	}

	protected boolean hasIoSession(BigInteger id, TransportProtocol protocol) {

		if (protocol.equals(TransportProtocol.UDP)
				&& sessionsUDP.containsKey(id)) {
			return true;
		}

		if (protocol.equals(TransportProtocol.TCP)
				&& sessionsTCP.containsKey(id)) {
			return true;
		}
		return false;
	}

	protected void addToSessionsMap(BigInteger Id, IoSession session,
			TransportProtocol protocol) {

		if (protocol.equals(TransportProtocol.UDP)) {

			sessionsUDP.put(Id, session);

		} else if (protocol.equals(TransportProtocol.TCP)) {

			sessionsTCP.put(Id, session);

		} /*#%*/ else {
		/*#%*/ log.warn("unknown transport protocol");
		/*#%*/ }

	}

	/**
	 * Updates the permanency counter of the connection associated with the
	 * specified end point.
	 * 
	 * @return the updated counter value
	 */
	private int updatePermanentCounter(BigInteger endPointId,
			TransportProtocol protocol, boolean permanent) {

		IoSession session = getIoSession(endPointId, protocol);

		Integer counter = (Integer) session.getAttribute(PERMANENT_COUNTER);

		if (permanent) {

			counter++;

		} else if (counter > 0) {

			counter--;
		}

		session.setAttribute(PERMANENT_COUNTER, counter);

		// System.out.println("Count:" + counter);

		return counter;

	}

	protected boolean deliverToConsumers(DeliverMessageEvent deliverMessageEvent) {

		Set<EventConsumer> eventConsumerSet = eventRegistry
				.getEventConsumerSet(deliverMessageEvent.getMessage()
						.getClass());

		if (eventConsumerSet != null) {

			deliverMessageEvent.setConsumers(eventConsumerSet);
			trigger(deliverMessageEvent);

			return true;

		} else {

			return false;

		}

	}

	private boolean isTemporary(IoSession session) {
		return (((Integer) session.getAttribute(PERMANENT_COUNTER)) == 0);
	}

	// private int getPermanentCounter(BigInteger endPointId,
	// TransportProtocol protocol) {
	//
	// if (protocol.equals(TransportProtocol.UDP)) {
	//
	// return (Integer) sessionsUDP.get(endPointId).getAttribute(
	// PERMANENT_COUNTER);
	//
	// } else {
	//
	// return (Integer) sessionsTCP.get(endPointId).getAttribute(
	// PERMANENT_COUNTER);
	// }
	// }

	public void trigger(Event event, boolean dummy) {
		trigger(event);
	}
	
	public Set<IoSession> getAllSessions() {
		Set<IoSession> sessions = new HashSet<IoSession>(sessionsTCP.values());
		sessions.addAll(sessionsUDP.values());
		return sessions;
	}

	
	protected void sendClose(IoSession session) {
		Close closeMsg = new Close();
		session.write(closeMsg);
	}

}
