package dks.utils.serialization;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.future.DefaultWriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
//import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
//import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

class PayLoad implements Serializable {
	static final long serialVersionUID = 1L;
	char payload[];
	public PayLoad(int size) {
		payload = new char[size];
	}
}

class IOHandlerClient extends IoHandlerAdapter {
	ClientObject client;

	public IOHandlerClient(ClientObject clientIn) {
		client = clientIn;
	}

	@Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
    	client.incReceived();
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Client IDLE " + session.getIdleCount(status));
    }
};

class ClientObject implements Runnable {
	public Args args;
	IoSession session;
	int sent, received;
	ExecutorService waitingThreadPool = null;
	final Lock ioHandlerLock = new ReentrantLock();
	final Condition awaitingLastMsg = ioHandlerLock.newCondition();
	final Condition fullPipeline = ioHandlerLock.newCondition();
	protected static Logger log;

    public ClientObject(Args argsIn, ExecutorService thrPool) {
    	args = argsIn;
    	waitingThreadPool = thrPool;

    	sent = received = 0;
    	if (args.log)
    		log = Logger.getLogger(Client.class);
    	else
    		log = null;
    	NioSocketConnector connector = new NioSocketConnector();
    	IOHandlerClient ioHandler = new IOHandlerClient(this);
    	connector.setHandler(ioHandler);
    	DefaultIoFilterChainBuilder serviceChain = connector.getFilterChain();
    	if (args.log)
    		serviceChain.addLast("logger-1", new DebugLoggingFilter("buffer")); // sees the serialized message;
		serviceChain.addLast("protocol",
				new ProtocolCodecFilter(new DebugObjectSerializationCodecFactory()));
    	if (args.log)
    		serviceChain.addLast("logger-2", new DebugLoggingFilter("message")); // sees the original message;
		IoBuffer.setAllocator(new DebugSimpleBufferAllocator(log));

        try {
        	ConnectFuture future = connector.connect(new InetSocketAddress(args.host, args.port));
        	future.awaitUninterruptibly();
        	session = future.getSession();
        } catch (RuntimeIoException e) {
			System.err.println("client: an attempt to connect to " + args.host + ":" + args.port + " failed.");
			e.printStackTrace();
			System.exit(1);
        }

        SocketSessionConfig sessionConf = (SocketSessionConfig) session.getConfig();

    	if (args.mina_bufsize > 0) { /* non-default */
    		sessionConf.setReadBufferSize(args.mina_bufsize);
    		sessionConf.setReadBufferSize(args.mina_bufsize);
    	}
    	if (args.io_bufsize > 0) {   /* non-default */
    		sessionConf.setSendBufferSize(args.io_bufsize);
    		sessionConf.setReceiveBufferSize(args.io_bufsize);
    	}
    	sessionConf.setIdleTime(IdleStatus.BOTH_IDLE, args.idle_time);
    	sessionConf.setTcpNoDelay(args.tcp_nodelay);
    }

    ExecutorService getThreadPool() { return (waitingThreadPool); }
    
    public void incReceived() {
		ioHandlerLock.lock();
   		received++;
   		if (sent < received + args.pipeline)
   			fullPipeline.signal();
   		if (received >= args.iterations)
   			awaitingLastMsg.signal();
   		ioHandlerLock.unlock();
    }

    public void run() {
		WriteFuture writeFuture;
		PayLoad message = new PayLoad(args.msgsize);

    	sent = received = 0;
		while (sent < args.iterations) {
	    	if (args.log)
	    		log.debug("Calling session.write()");
	    	if (args.dryrun) {
	    		writeFuture = new DefaultWriteFuture(null);
	    		writeFuture.setWritten(); // deprecated method for internal MINA use;
	    		incReceived(); // simulate the arrival of the response;
	    	} else {
	    		writeFuture = session.write(message);
	    	}
	    	if (args.log)
	    		log.debug(".. session.write() terminated");

			if (args.wait_write) {
				WriteWait write_wait = new WriteWait(message, writeFuture, log);
				waitingThreadPool.execute(write_wait);
			}

			sent++;
			ioHandlerLock.lock();
	    	while (sent >= received + args.pipeline) {
	    		fullPipeline.awaitUninterruptibly();
	    	}
	    	ioHandlerLock.unlock();
	    	if (args.log)
	    		log.debug(".. synchronized with writeFuture");
	    	
	    	if (args.delay > 0) {
	    		try {
					Thread.sleep(args.delay);
				} catch (InterruptedException e) {
					;
				}
	    	}

	    	if (args.newmsg == false) {
	    		message = new PayLoad(args.msgsize);
	    	}
		}

		ioHandlerLock.lock();
    	while (received < args.iterations) {
    		awaitingLastMsg.awaitUninterruptibly();
    	}
    	ioHandlerLock.unlock();
    	// System.out.println("a client done " + args.iterations + " iterations.");
    	Client.clientDone();
     }
};

public class Client {
	static ExecutorService clientsThreadPool = null;
	static ExecutorService waitingThreadPool = null;
	static ExecutorService computeThreadPool = null;
	static final Lock clientsLock = new ReentrantLock();
	static final Condition awaitingLastClient = clientsLock.newCondition();
	static int clientsDone = 0;
	static Args argsObj;

	public static void main(String[] args) {
	    int i;
	    ClientObject clients[];
		Compute compute;

	    argsObj = new Args(args);
		clientsThreadPool = Executors.newCachedThreadPool();
		computeThreadPool = Executors.newCachedThreadPool();
		if (argsObj.wait_write)
		    waitingThreadPool = Executors.newCachedThreadPool();
    	if (argsObj.log)
    		PropertyConfigurator.configure(System.getProperty("org.apache.log4j.config.file"));
		compute = new Compute(argsObj, computeThreadPool);

		clients = new ClientObject[argsObj.links];
		for (i = 0; i < argsObj.links; i++) {
			Args linkArgs = new Args(argsObj);
			linkArgs.iterations = argsObj.iterations / argsObj.links;
			linkArgs.port = argsObj.port + i;
			clients[i] = new ClientObject(linkArgs, waitingThreadPool);
		}

		for (i = 0; i < argsObj.links; i++) {
			clientsThreadPool.execute(clients[i]);
		}

		compute.resume();

		clientsLock.lock();
    	while (clientsDone < argsObj.links) {
    		awaitingLastClient.awaitUninterruptibly();
    	}
    	clientsLock.unlock();
		System.out.println("client: done " + argsObj.iterations + " iterations.");
		System.exit(0);
	}

	public static void clientDone() {
		clientsLock.lock();
		clientsDone++;
    	if (clientsDone >= argsObj.links) {
    		awaitingLastClient.signal();
    	}
    	clientsLock.unlock();
	}
};

