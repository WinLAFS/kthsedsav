
package dks.utils.serialization;

import org.apache.log4j.Logger;
import org.apache.mina.core.future.WriteFuture;

public class WriteWait implements Runnable {
	Object message;
	WriteFuture writeFuture;
	Logger log;

	public WriteWait(Object messageIn, WriteFuture writeFutureIn, Logger logIn) {
		message = messageIn;
		writeFuture = writeFutureIn;
		log = logIn;
	}

	public void run() {
		writeFuture.awaitUninterruptibly();
		if (!writeFuture.isWritten()) {
			Throwable ex = writeFuture.getException();
			System.err.println("A message has NOT been sent properly");
			if (log != null)
				log.error("WriteWait says: a message has NOT been sent properly");
			if (ex != null)
				ex.printStackTrace();
		} else {
			if (log != null)
				log.debug("WriteWait says: message " + message + " written correctly");
		}
	}
}
