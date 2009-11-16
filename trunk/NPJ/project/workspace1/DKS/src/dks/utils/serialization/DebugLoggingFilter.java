package dks.utils.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;

/**
 * Logs all MINA protocol events.  Each event can be
 * tuned to use a different level based on the user's specific requirements.  Methods
 * are in place that allow the user to use either the get or set method for each event
 * and pass in the {@link IoEventType} and the {@link LogLevel}.
 *
 * By default, all events are logged to the {@link LogLevel#INFO} level except
 * {@link IoFilterAdapter#exceptionCaught(IoFilter.NextFilter, IoSession, Throwable)},
 * which is logged to {@link LogLevel#WARN}.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 706057 $, $Date: 2008-10-19 21:40:20 +0200 (Sun, 19 Oct 2008) $
 * @org.apache.xbean.XBean
 */
public class DebugLoggingFilter extends LoggingFilter {
	/** The logger name */
    private final String name;
    /** The logger */
    private final Logger logger;

    /** The log level for the messageSent event. Default to INFO. */
    private LogLevel messageWriteLevel = LogLevel.INFO;

    /**
     * Default Constructor.
     */
    public DebugLoggingFilter() {
       this(LoggingFilter.class.getName());
   }
    
    /**
     * Create a new NoopFilter using a class name
     * 
     * @param clazz the cass which name will be used to create the logger
     */
    public DebugLoggingFilter(Class<?> clazz) {
        this(clazz.getName());
    }

    /**
     * Create a new NoopFilter using a name
     * 
     * @param name the name used to create the logger. If null, will default to "NoopFilter"
     */
    public DebugLoggingFilter(String name) {
        super(name);

        if (name == null) {
            this.name = LoggingFilter.class.getName();
        } else {
        	this.name = name;
        }
        
        logger = LoggerFactory.getLogger(name);
    }

    /**
     * @return The logger's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Log if the logger and the current event log level are compatible. We log
     * a message and an exception.
     * 
     * @param eventLevel the event log level as requested by the user
     * @param message the message to log
     * @param cause the exception cause to log
     */
    private void log(LogLevel eventLevel, String message, Throwable cause) {
    	if (eventLevel == LogLevel.TRACE) {
    		logger.trace(message, cause);
    	} else if (eventLevel.getLevel() > LogLevel.INFO.getLevel()) {
    		logger.info(message, cause);
    	} else if (eventLevel.getLevel() > LogLevel.WARN.getLevel()) {
    		logger.warn(message, cause);
    	} else if (eventLevel.getLevel() > LogLevel.ERROR.getLevel()) {
    		logger.error(message, cause);
    	}
    }

    /**
     * Log if the logger and the current event log level are compatible. We log
     * a formated message and its parameters. 
     * 
     * @param eventLevel the event log level as requested by the user
     * @param message the formated message to log
     * @param param the parameter injected into the message
     */
    private void log(LogLevel eventLevel, String message, Object param) {
    	if (eventLevel == LogLevel.TRACE) {
    		logger.trace(message, param);
    	} else if (eventLevel.getLevel() > LogLevel.INFO.getLevel()) {
    		logger.info(message, param);
    	} else if (eventLevel.getLevel() > LogLevel.WARN.getLevel()) {
    		logger.warn(message, param);
    	} else if (eventLevel.getLevel() > LogLevel.ERROR.getLevel()) {
    		logger.error(message, param);
    	} 
    }

    /**
     * Log if the logger and the current event log level are compatible. We log
     * a simple message. 
     * 
     * @param eventLevel the event log level as requested by the user
     * @param message the message to log
     */
    private void log(LogLevel eventLevel, String message) {
    	if (eventLevel == LogLevel.TRACE) {
    		logger.trace(message);
    	} else if (eventLevel.getLevel() > LogLevel.INFO.getLevel()) {
    		logger.info(message);
    	} else if (eventLevel.getLevel() > LogLevel.WARN.getLevel()) {
    		logger.warn(message);
    	} else if (eventLevel.getLevel() > LogLevel.ERROR.getLevel()) {
    		logger.error(message);
    	}
    }

    /**
     * 
     */
    public void filterWrite(NextFilter nextFilter, IoSession session,
            WriteRequest writeRequest) throws Exception {
    	log(messageWriteLevel, "WRITE: {}", writeRequest.getMessage() );
        nextFilter.filterWrite(session, writeRequest);
   }

 }
