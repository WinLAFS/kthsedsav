package dks.niche.wrappers;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.mina.core.future.WriteFuture;

import dks.addr.DKSRef;
import dks.comm.SendJob;
import dks.messages.Message;
import dks.niche.ids.NicheId;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheMessageInterface;
import dks.niche.interfaces.SendClassInterface;
import dks.niche.wrappers.ClientSideBindStub.BindSendJob;

public class NicheWaitClass implements Runnable {

	public static final int SEND_TIMEOUT = 
			System.getProperty("dks.comm.sendTimeout") instanceof String ?
					Integer.parseInt(System.getProperty("dks.comm.sendTimeout"))
					:
					5000  //Very high if not specified...
			;

	ArrayList<WaitJob> messagesWaiting;
	NicheAsynchronousInterface logger;
	NicheSendClass messageMonitor;
	boolean running = false;
	Object myLock;
	Object myFlag;
	String waitFlag = "waitFlag";
	String session;
	
	public NicheWaitClass(NicheAsynchronousInterface logger, String session, NicheSendClass messageMonitor) {
		this.logger = logger;
		this.session = session;
		this.messageMonitor = messageMonitor;
		messagesWaiting = new ArrayList<WaitJob>();
		myFlag = new Object();
		myLock = new Object();
	}
	
	public void addWaitJob(SendJob sendJob, WriteFuture writeFuture){
		synchronized (messagesWaiting) {
			messagesWaiting.add(new WaitJob(sendJob, writeFuture));
		}		
		if(false == running) {
			notify("klar att starta!");
		} //else {	}
	}
	
	public void run() {
			
		for(;;){
			running = true;
			while (0 < messagesWaiting.size()) {
				
				
				ArrayList<WaitJob> readyMessages;
				synchronized (messagesWaiting) {
					readyMessages = (ArrayList<WaitJob>) messagesWaiting.clone();
					messagesWaiting.clear();
				}
//				if(1 < readyMessages.size()) {
//					System.out.println(session + " is waiting for " + readyMessages.size() + " messages");
//				}
				for (WaitJob waitJob : readyMessages) {
									
					waitJob.writeFuture.awaitUninterruptibly(SEND_TIMEOUT);
					
					if (!waitJob.writeFuture.isWritten()) {
						
						
						if(waitJob.sendJob.messageType == null) {
							
							/*#%*/ logger.log(
							/*#%*/ 		"Message "
							/*#%*/ 		+waitJob.sendJob.getMessage().getClass().getSimpleName()
							/*#%*/ 		+ " with id "
							/*#%*/ 		+ ((NicheMessageInterface)waitJob.sendJob.getMessage()).getMessageId()
							/*#%*/ 		+ " NOT correctly written to "
							/*#%*/ 		+ session
							/*#%*/ );
							messageMonitor.addJob(NicheSendClass.SEND_FAILURE, waitJob.sendJob);
							
						}/*#%*/   else {
						/*#%*/ logger.log(
						/*#%*/ 			"DKS-Message "
						/*#%*/ 			+ waitJob.sendJob.messageType
						/*#%*/ 			+ "NOT correctly written to "
						/*#%*/ 			+ session
						/*#%*/ );
						/*#%*/ }
					} else {
						
						if(waitJob.sendJob.messageType == null) {
							
							/*#%*/ String logMessage =
							/*#%*/ 	"Message "
							/*#%*/ 	+waitJob.sendJob.getMessage().getClass().getSimpleName()
							/*#%*/ 	+ " with id "
							/*#%*/ 	+ ((NicheMessageInterface)waitJob.sendJob.getMessage()).getMessageId()
							/*#%*/ 	+ " correctly written to "
							/*#%*/ 	+ session
							/*#%*/ ;
							/*#%*/ logger.log(
							/*#%*/ 	logMessage	
							/*#%*/ );
							//System.out.println(logMessage);
							messageMonitor.addJob(NicheSendClass.SEND_SUCCESS, waitJob.sendJob);
							
						}  /*#%*/ else {
						/*#%*/ logger.log(
						/*#%*/ 			"DKS-Message "
						/*#%*/			+ waitJob.sendJob.messageType 
						/*#%*/			+" correctly written to "
						/*#%*/ 			+ session
						/*#%*/ 	);
						/*#%*/ }
					}
					
				}
//				if(1 < readyMessages.size()) {
//					System.out.println(session + " is done");
//				}

		} //end while
			running = false;
			myWait();
		}//end for-ever
}//end run
	
	protected void myWait() {

		// if(waitForResults[operationId].equals(waitForSynchronousReturnValue))
		// {
		// while(waitForResults[operationId].equals(waitForSynchronousReturnValue))
		// {
		synchronized (myLock) {
			//logger.log("WaitClass says: Entering critical section: " + session);
			
			myFlag = waitFlag;
			while (myFlag.equals(waitFlag)) {
				try {
					myLock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//logger.log("WaitClass says: Exiting critical section: " + session);
		}
		// }
	}
	
	public void notify(Object result) {

		//logger.log("WaitClass says: notify is trying to enter critical section: " + session);
		
		synchronized (myLock) {
			
			//logger.log("WaitClass says: notify is trying to enter critical section: "	+ session);
					
			myFlag = result;
			myLock.notify();
		}
	}


	class WaitJob {
		SendJob sendJob;
		WriteFuture writeFuture;
		WaitJob(SendJob sendJob, WriteFuture writeFuture) {
			this.sendJob = sendJob;
			this.writeFuture = writeFuture;
		}
	}
}