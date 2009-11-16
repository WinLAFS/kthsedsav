package dks.niche.wrappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dks.comm.SendJob;
import dks.comm.mina.TransportProtocol;
import dks.comm.mina.events.CommSendEvent;
import dks.messages.Message;
import dks.niche.components.NicheCommunicatingComponent;
import dks.niche.interfaces.MessageManagerInterface;
import dks.niche.interfaces.NicheAsynchronousInterface;
import dks.niche.interfaces.NicheMessageInterface;


public class NicheSendClass implements Runnable {

	public final static int SEND_SUCCESS = 0;
	public final static int LOOKUP_FAILURE = 1;
	public final static int CONNECTION_CLOSED_FAILURE = 2;
	public final static int SEND_FAILURE = 3;
	
	public static final int MESSAGE_QUEUE = 5000;
	//static final int MAX_CONCURRENT_OPERATIONS = SIZE;
	
	static final int CLEANUP_DELAY = 
		System.getProperty("niche.sendFailure.resendDelay") instanceof String ?
			Integer.parseInt(System.getProperty("niche.sendFailure.resendDelay"))
		:
			100;

	//static final int WAIT_MAX = 500;
	static final int ID_RETRY_DELAY = 1000;
	static final int RETRY_MAX = 10;
	
	NicheAsynchronousInterface niche;
	NicheCommunicatingComponent parent;
	
	HashMap<Integer, SendJob> waitingForProperlyReceivedConfirmation;
	ArrayList<SendJob> waitingForResend;
	
	HashMap<Integer, Boolean> generator;
	Map<Integer, Boolean> waitingErrors;


	Object myLock;
	Object myFlag;
	String waitFlag = "lockflag";
	
	boolean running = false;
	//int messageId;

		

		//MessageManagerInterface messageManager;

		public NicheSendClass(NicheAsynchronousInterface logger, NicheCommunicatingComponent parent) {
			
			this.niche = logger;
			this.parent = parent;
			
			waitingForProperlyReceivedConfirmation = new HashMap<Integer, SendJob>(MESSAGE_QUEUE);
			waitingForResend = new ArrayList<SendJob>(MESSAGE_QUEUE);
			generator = new HashMap<Integer, Boolean>();
			waitingErrors = Collections.synchronizedMap(generator);
			
			myLock = new Object();
			myFlag = new Object();
			
		}
			
	
		public synchronized void addJob(int status, SendJob sendJob) {
			
			/*#%*/ String logMessage;
			boolean handleErrorFlag = false;
			
			synchronized (generator) {
				
				if(waitingErrors.containsKey(sendJob.getOperationId())) {
					
					/*#%*/ logMessage = "Adding job with id " + sendJob.getOperationId() + " which was waiting for treatment!";
					//System.out.println(logMessage);
					/*#%*/ niche.log(logMessage);
					waitingErrors.remove(sendJob.getOperationId());
					handleErrorFlag = handleError(sendJob);
					
				} else {
					
					MessageManagerInterface messageManager = sendJob.getMessageManager();
					
					switch(status) {
						case SEND_SUCCESS:
							/*#%*/ logMessage = "Adding normal job with id " + sendJob.getOperationId();
							/*#%*/ niche.log(logMessage);
							if(messageManager != null) {
								if(messageManager.invokeOnSendSuccess()) {
									messageManager.notify(MessageManagerInterface.SUCCESSFULLY_SENT, sendJob.getMessage().getOriginalMessage());
								}
							}
							
							waitingForProperlyReceivedConfirmation.put(sendJob.getOperationId(), sendJob);
							break;
							
						case SEND_FAILURE:
						case CONNECTION_CLOSED_FAILURE:
							
							/*#%*/ logMessage = "SendClass got send-failure for id " + sendJob.getOperationId();						
							
							if(messageManager != null) {
								
								if (messageManager.invokeOnChannelError()) {
									//sends on bindings should go here
									/*#%*/ logMessage += " and gives the failed job to the message manager";
									/*#%*/ niche.log(logMessage);
									//Obs obs, the message manager does not know about niche-wrapped messages
									//- the original message must be "unwrapped"
									messageManager.notify(MessageManagerInterface.CHANNEL_ERROR, sendJob.getMessage().getOriginalMessage());
									niche.publicExecute(messageManager);
									return;
								} 
								
								if(messageManager.dropMessage()) {
									/*#%*/ logMessage += " and is told to drop the message!";
									/*#%*/ niche.log(logMessage);
									return; //do nothing
								}
							}
							/*#%*/ logMessage += " and adds the message to the resend queue";
							if(!running) {
								handleErrorFlag = true;
								/*#%*/ logMessage += " which needs to be activated";
							} else {
								/*#%*/ logMessage += " which is already activated";
							}
							
							/*#%*/ niche.log(logMessage);
							waitingForResend.add(sendJob);

							break;
					} //end switch
				}
			}//end waitingErrors-sync
			
			if(handleErrorFlag) {
				notify("time to resend!");
			}
		}
		
		//watch out for race-conditions: you can get here before you
		//got the actual job from the wait-process
		public void reportError(int status, int operationId) {
			
			/*#%*/ String logMessage = "Node-error reported with id " + operationId;
			/*#%*/ niche.log(logMessage);
			//System.out.println(logMessage);
			boolean doHandleError = false;
			SendJob failedJob = null;
			
			synchronized (generator) {

				failedJob = waitingForProperlyReceivedConfirmation.remove(operationId);

				if(failedJob != null && System.currentTimeMillis() < failedJob.getTimeStamp() + 20*NicheWaitClass.SEND_TIMEOUT) {				
					doHandleError = true;

				} else {
					
					/*#%*/ logMessage = "And for " + operationId + " the sendjob was " + (failedJob == null ? " null" : " old") + ", wait for send-process";
					/*#%*/ niche.log(logMessage);
					//System.err.println(logMessage);				
					waitingErrors.put(operationId, true);
				}
		
			}
		
			if(doHandleError) {
				if(handleError(failedJob)) {
					notify("time to resend!");
				}
			}
		}
		
		private boolean handleError(SendJob failedJob) {
			
			MessageManagerInterface messageManager = failedJob.getMessageManager();
			
			if(messageManager != null) {
				if (messageManager.invokeOnIdError()) {
					messageManager.notify(MessageManagerInterface.ID_ERROR, failedJob.getMessage());
					//myThreadPool.
					niche.publicExecute(messageManager);
					return false;
				} 
				
				if(messageManager.dropMessage()) {
					return false; //do nothing
				}
			} else { //had no messageManager
				//System.out.println("No message-manager available for message " + failedJob.getMessage().getClass().getSimpleName());
			}
			waitingForResend.add(failedJob);

			//Do notify only if not already running!
			if(running) {
				return false;
			}
			return true;
		}
		
		public void run() {

			/*#%*/ String logMessage;
			for(;;){
				
				running = true;
				while (0 < waitingForResend.size()) {					
					
					
					ArrayList<SendJob> readyMessages;
					synchronized (waitingForResend) {
						readyMessages = (ArrayList<SendJob>) waitingForResend.clone();
						waitingForResend.clear();
					}
					boolean failure = false;

					/*#%*/ logMessage = "Triggering";
					
					for (SendJob sendJob : readyMessages) {
					
						if(sendJob.sendToNode()) {
							//trigger new comm-send-event
							parent.publicTrigger(new CommSendEvent((Message)sendJob.getMessage(), sendJob.getMessage().getSource(), TransportProtocol.TCP, sendJob));
							/*#%*/  logMessage += " re-send of " + sendJob.getOperationId();
							
						} else { 
							parent.addMessageToQueueAndTriggerLookup(sendJob, sendJob.getDestinationId());
							/*#%*/ logMessage += " re-lookup of " + sendJob.getOperationId();
						}
						//System.out.println(logMessage);
						
					
					} //end for every job in queue
					
					/*#%*/ niche.log(logMessage);
					

				} //end while
				running = false;
				/*#%*/ logMessage =
				/*#%*/ "Re-send thread goes to sleep. "
				/*#%*/ + (
				/*#%*/    (0 < waitingErrors.keySet().size()) ?
				/*#%*/ 		" Still waiting for " + waitingErrors.keySet().toString()
				/*#%*/ 	:
				/*#%*/ 		""
				/*#%*/ 	)
				/*#%*/ 	;
				/*#%*/ //System.out.println(logMessage);
				/*#%*/ niche.log(logMessage);
				myWait();
			}//end for-ever
	}//end run
		
	protected void myWait() {

			synchronized (myLock) {
								
				myFlag = waitFlag;
				while (myFlag.equals(waitFlag)) {
					try {
						myLock.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
			//allow a short break before restarting again
			//CANNOT be before the wait, because then notify can come 
			// in between => deadlock. 
			if(0 < CLEANUP_DELAY) {
				try {
					Thread.sleep(CLEANUP_DELAY);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
		
		public void notify(Object result) {

			//niche.log("SendClass says: notify is trying to enter critical section");
			
			synchronized (myLock) {
				
				//niche.log("SendClass says: notify has entered critical section");
						
				myFlag = result;
				myLock.notify();
				//System.err.println("Re-send thread will succeed to get running");
			}
		}

		
			
		
	} //end class
