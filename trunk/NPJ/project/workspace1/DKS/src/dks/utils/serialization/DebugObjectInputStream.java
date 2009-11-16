package dks.utils.serialization;

import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.LinkedList;

import org.apache.log4j.Logger;

class LogEntry {
	public Class<?> cl;
	public int depth;
	public LogEntry(Class<?> clIn, int depthIn) {
		cl = clIn;
		depth = depthIn;
	}
};

public class DebugObjectInputStream extends ObjectInputStream {
	private static final Field depthField;
	Logger logger;
	static {
		try {
			depthField = ObjectInputStream.class.getDeclaredField("depth");
			depthField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new AssertionError(e);
		}
	}
	private static Hashtable<Class<?>,Boolean> rec = new Hashtable<Class<?>,Boolean>(1000);
	private LinkedList<LogEntry> log = new LinkedList<LogEntry>();

	public DebugObjectInputStream(Logger logger, InputStream in) throws IOException {
		super(in);
		enableResolveObject(true);
		this.logger = logger;
	}

	private int getCurrentDepth() {
		try {
			return(depthField.getInt(this));
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	protected Object resolveObject(Object o) throws IOException {
		int currentDepth = getCurrentDepth();
		Class<?> oClass = o.getClass();

		log.addLast(new LogEntry(oClass, currentDepth));

		if (currentDepth == 1) {
			if (!rec.containsKey(oClass)) {
				rec.put(oClass, true);
			} else {
				if (logger != null)
					logger.debug("Deserialized class " + oClass + " as follows:");
				while (!log.isEmpty()) {
					LogEntry le = log.removeFirst();
					if (logger != null)
						logger.debug("  " + le.cl.getCanonicalName()
								+ " at depth " + le.depth);
				}
			} 
		}

		return (o);
	}
};
