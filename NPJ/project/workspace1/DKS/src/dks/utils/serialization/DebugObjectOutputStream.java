package dks.utils.serialization;

import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import dks.niche.interfaces.NicheAsynchronousInterface;

public class DebugObjectOutputStream extends ObjectOutputStream {
	
	private static final Field depthField;
	Logger logger;
	static {
		try {
			depthField = ObjectOutputStream.class.getDeclaredField("depth");
			depthField.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new AssertionError(e);
		}
	}
	// Note the 'rec' registry is static - one for all uses;
	private static Hashtable<Class<?>,Boolean> rec = new Hashtable<Class<?>,Boolean>(1000);
	private static Hashtable<Class<?>,Integer> sizes = new Hashtable<Class<?>,Integer>(1000);
	
	boolean verbose = false;
	// can be null meaning this info is not available;
	BufferPosition bp = null;
	int currentPosition;

	int size = 0;
	
	Class mainClass;
	// Interesting: ObjectOutputStream tries to write an
	// IOException to the stream when serialization goes wrong,
	// doesn't it?
    // if (obj instanceof IOException && ..
	
	public DebugObjectOutputStream(Logger logger, OutputStream out, BufferPosition bpIn) throws IOException {
		super(out);
		enableReplaceObject(true);
		if (bpIn != null) {
			bp = bpIn;
		}
		this.logger = logger;
	}

	private int getCurrentDepth() {
		try {
			return(depthField.getInt(this));
		} catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
	}

	protected Object replaceObject(Object o) {
		int currentDepth = getCurrentDepth();

		if (currentDepth == 1) {
			verbose = false;
			currentPosition = 0;
			size = 0;
			
			Class<?> oClass = o.getClass();
			mainClass = oClass;
			
			verbose = true;
			if (logger != null) {
				logger.debug("Start serializing " + o);
				// logger.debug("Start serializing " + o.getClass().getCanonicalName() + ":");
			}
			if (!rec.containsKey(oClass))
			{
				rec.put(oClass, true);
				sizes.put(oClass, 0);				
				
			}
		}

		if (verbose) {
			if (bp != null) {
				int newPosition = bp.getPosition();
				int delta = (newPosition - currentPosition);
				size += delta;
				if (logger != null)
					logger.debug("  " + o.getClass().getCanonicalName() +
							" at depth " + currentDepth + 
							", +" + delta + " = " + size + " bytes.");
				currentPosition = newPosition;
				//System.out.println("Contains " + mainClass.getCanonicalName() + " ? " + sizes.containsKey(main));
				int oldSize = sizes.get(mainClass);
				if(oldSize < size) {
					sizes.put(mainClass, size);
					if (logger != null)
						logger.debug("Serializer says: new Max for "
								+ mainClass.getCanonicalName()
								+ " is "
								+ size);
				}
				
			} else {
				if (logger != null)
					logger.debug("  " + o.getClass().getCanonicalName() +
							" at depth " + currentDepth);
			}
		}

		return o;
	}
};

