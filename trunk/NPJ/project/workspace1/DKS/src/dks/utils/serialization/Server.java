
package dks.utils.serialization;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
//import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
//import org.apache.mina.filter.logging.LoggingFilter;

class IOHandlerServer extends IoHandlerAdapter {
	ServerObject server;
	char reply = 'o';

	public IOHandlerServer(ServerObject serverIn) {
		server = serverIn;
	}

	@Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
		WriteFuture writeFuture;

		if (server.args.short_ack)
			writeFuture = session.write(reply);
		else
			writeFuture = session.write(message);

		if (server.args.wait_write) {
			WriteWait write_wait = new WriteWait(message, writeFuture, ServerObject.log);
			(server.getThreadPool()).execute(write_wait);
		}
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Server IDLE " + session.getIdleCount(status));
    }
};

class ServerObject {
	ExecutorService waitingThreadPool = null;
	public static Logger log;
	public Args args;

    public ServerObject(Args argsIn, ExecutorService thrPool, int num) {
    	args = argsIn;
    	waitingThreadPool = thrPool;

    	if (args.log)
    		log = Logger.getLogger(Server.class);
    	else
    		log = null;
    	NioSocketAcceptor acceptor = new NioSocketAcceptor();
    	SocketSessionConfig sessionConf = acceptor.getSessionConfig();
    	DefaultIoFilterChainBuilder serviceChain = acceptor.getFilterChain();
    	IOHandlerServer ioHandler = new IOHandlerServer(this);

    	acceptor.setHandler(ioHandler);

		if (args.verbose && num == 0) {
			int max_read_buffer_size = sessionConf.getMaxReadBufferSize();
			int min_read_buffer_size = sessionConf.getMinReadBufferSize();
			int read_buffer_size = sessionConf.getReadBufferSize();
			System.out.println("MINA IoSession parameters:");
			System.out.println("   MaxReadBufferSize = " + max_read_buffer_size);
			System.out.println("   MinReadBufferSize = " + min_read_buffer_size);
			System.out.println("   ReadBufferSize = " + read_buffer_size);
		}

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

    	if (args.log)
    		serviceChain.addLast("logger-1", new DebugLoggingFilter("buffer"));
		serviceChain.addLast("protocol",
				new ProtocolCodecFilter(new DebugObjectSerializationCodecFactory()));
    	if (args.log)
    		serviceChain.addLast("logger-2", new DebugLoggingFilter("message"));
		IoBuffer.setAllocator(new DebugSimpleBufferAllocator(log));

        try {
			acceptor.bind( new InetSocketAddress(args.port) );
		} catch (IOException e) {
			System.err.println("server: an attempt to bind the socket to port " + args.port + " failed.");
			e.printStackTrace();
			System.exit(1);
		}
    }

    ExecutorService getThreadPool() { return (waitingThreadPool); }
};

public class Server {
	static ExecutorService waitingThreadPool = null;
	static ExecutorService computeThreadPool = null;

	public static void main(String[] args) {
		Args argsObj = new Args(args);
		computeThreadPool = Executors.newCachedThreadPool();
		if (argsObj.verbose)
			System.out.println("cwd=" + System.getProperty("user.dir"));
		if (argsObj.log)
			PropertyConfigurator.configure(System.getProperty("org.apache.log4j.config.file"));
		Compute compute = new Compute(argsObj, computeThreadPool);

    	if (argsObj.wait_write)
    		waitingThreadPool = Executors.newCachedThreadPool();

		for (int i = 0; i < argsObj.links; i++) {
			Args linkArgs = new Args(argsObj);
			linkArgs.port = argsObj.port + i;
			new ServerObject(linkArgs, waitingThreadPool, i);
		}

		compute.resume();
	}
};

