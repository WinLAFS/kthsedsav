/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.dht;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import dks.addr.DKSRef;
import dks.arch.ComponentRegistry;
import dks.arch.Event;
import dks.arch.HooksNumberTable;
import dks.arch.Scheduler;
import dks.comm.CommunicatingComponent;
import dks.comm.mina.TransportProtocol;
import dks.comm.mina.events.DeliverMessageEvent;
import dks.dht.events.ApplicationGetResponseEvent;
import dks.dht.events.ApplicationPutAckEvent;
import dks.dht.events.ApplicationRemoveAckEvent;
import dks.dht.events.GetRequestEvent;
import dks.dht.events.HandoverTimeoutEvent;
import dks.dht.events.LastDataChunkSentEvent;
import dks.dht.events.PutRequestEvent;
import dks.dht.events.RemoveRequestEvent;
import dks.dht.messages.GetRequestMessage;
import dks.dht.messages.GetResponseMessage;
import dks.dht.messages.HandoverMessage;
import dks.dht.messages.PredecessorHandoverMessage;
import dks.dht.messages.PutAckMessage;
import dks.dht.messages.PutRequestMessage;
import dks.dht.messages.RemoveAckMessage;
import dks.dht.messages.RemoveRequestMessage;
import dks.dht.messages.SuccessorHandoverMessage;
import dks.fd.events.SuspectEvent;
import dks.messages.Message;
import dks.niche.hiddenEvents.SendToIdAckEvent;
import dks.ring.RingState;
import dks.ring.events.RingLeaveDoneInterceptorAckEvent;
import dks.ring.events.RingLeaveDoneInterceptorEvent;
import dks.ring.events.RingNewPredecessorEvent;
import dks.router.Router.LookupStrategy;
import dks.timer.TimerComponent;
import dks.utils.IntervalsList;
import dks.utils.SimpleIntervalException;
import dks.utils.SimpleInterval.Bounds;

/**
 * The <code>DHTComponent</code> class
 * 
 * @author Ahmad Al-Shishtawy
 * @author Joel
 * @version $Id: DHTComponent.java 294 2006-05-05 17:14:14Z alshishtawy $
 */
public class DHTComponent extends CommunicatingComponent {
    
    public static enum getFlavor {GET_ALL, GET_ANY, GET_FIRST, GET_LAST, GET_AT};
    public static enum putFlavor {PUT_ADD, PUT_OVERWRITE}; //PUT_AT is not implemented yet
    public static enum removeFlavor {REMOVE_ALL, REMOVE_ANY, REMOVE_FIRST, REMOVE_LAST, REMOVE_AT};


    public static final String HASH_ALGORITHM = "SHA-1";

    public static final int DHT_HANDOVER_CHUNK_SIZE = 5;

    public static final int DHT_HANDOVER_CHUNK_TIMEOUT = 50;

    //public static final int DHT_HANDOVER_RETRY_TIMEOUT = 500;

    int succChunkCounter;

    DKSRef myRef;

    DKSRef succIndirectionRef;

    int succIndirectionCounter; // should be incremented when there is succ

    // number of nodes we are receiving data from / sending data to
    // zero if send/receive operation is completed
    // a node can only receive data when it joins so max receiveDataLink is
    // one
    // but it can send data to many other joining nodes and when the node
    // leaves
    int receiveDataLink;

    int sendDataLink;

    // indirection & decremented when

    // indirection stops. Get & Put requests are processed if this is none
    // positive
    // otherwise requests are placed in queue.
    // negative value means that I have the data but I'll not receive any
    // requests because join not finished yet.
    ArrayList<Message> joinRequestQueue;				// for put/get/remove requests
    Hashtable<BigInteger, PredRequestQueue> predRequestQueues;	// for put/get/remove requests

    ArrayList<Event> receiveDoneWaitQueue;			// for handover requests
    ArrayList<Event> sendDoneWaitQueue;				// for handover requests

    BigInteger N;

    LocalStore myLocalStore;
    
    HashMap<BigInteger, ArrayList<Message>> pendingLookups;

    /*
     * 
     * Below are constructor and methods for initialization
     * 
     */

    /**
     * @param scheduler
     * @param registry
     */
    public DHTComponent(Scheduler scheduler, ComponentRegistry registry) {
	super(scheduler, registry);

	myRef = registry.getRingMaintainerComponent().getMyDKSRef();
	myLocalStore = new HashtableLocalStore();
	N = registry.getRingMaintainerComponent().getDksParameters().N;
	succIndirectionRef = null;
	succIndirectionCounter = 0;
	succChunkCounter = 0;
	receiveDataLink = 0;
	sendDataLink = 0;

	joinRequestQueue = new ArrayList<Message>();
	predRequestQueues = new Hashtable<BigInteger, PredRequestQueue>();
	receiveDoneWaitQueue = new ArrayList<Event>();
	sendDoneWaitQueue = new ArrayList<Event>();
	
	pendingLookups = new HashMap();

	registerForEvents();
	registerMsgConsumers();
	registerHooks();
	//registerLookupOperations();
    }

    private void registerHooks() {
	/**
	 * Sets the indirection flag before joining the ring. To avoid
	 * processing get request when data handover is not complete.
	 */
	registerHook(HooksNumberTable.HOOK_JOIN_BEFORE_POINT, this,
	"handleJoinBeforePointHook");
	registerHook(HooksNumberTable.HOOK_LEAVE_BEFORE_POINT, this,
	"handleLeaveBeforePointHook");

    }

    /**
     * 
     */
    protected void registerMsgConsumers() {
	registerConsumer("handleSuccessorHandoverMessage", SuccessorHandoverMessage.class);
	registerConsumer("handlePredecessorHandoverMessage", PredecessorHandoverMessage.class);
	
	registerConsumer("receiveHandler", PutRequestMessage.class);
	registerConsumer("receiveHandler", GetRequestMessage.class);
	registerConsumer("receiveHandler", RemoveRequestMessage.class);
	
	registerConsumer("handlePutAckEvent", PutAckMessage.class);
	registerConsumer("handleGetResponseEvent", GetResponseMessage.class);
	registerConsumer("handleRemoveAckEvent", RemoveAckMessage.class);
	
    }

//    protected void registerLookupOperations() {
//	try {
//
//	    registerLookupOperation(PutRequestMessage.class, ExternalPutRequestEvent.class, "handleExternalPutRequestEvent");
//	    registerLookupOperation(GetRequestMessage.class, ExternalGetRequestEvent.class, "handleExternalGetRequestEvent");
//	    registerLookupOperation(RemoveRequestMessage.class, ExternalRemoveRequestEvent.class, "handleExternalRemoveRequestEvent");
//	} catch (OperationAlreadyRegisteredException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}
//    }

    @Override
    protected void registerForEvents() {

	register(PutRequestEvent.class, "handlePutRequestEvent");
	register(GetRequestEvent.class, "handleGetRequestEvent");
	register(RemoveRequestEvent.class, "handleRemoveRequestEvent");
	
	register(RingNewPredecessorEvent.class, "handleRingNewPredecessorEvent");
	register(HandoverTimeoutEvent.class, "handleHandoverTimeoutEvent");
	register(RingLeaveDoneInterceptorEvent.class, "handleRingLeaveDoneInterceptorEvent");
	register(SuspectEvent.class, "handleCommPeerSuspectedEvent");
	register(LastDataChunkSentEvent.class, "handleLastDataChunkSentEvent");

    }
    
    
    public void handleLookupResponse(SendToIdAckEvent e) {
    	DKSRef node = e.getResponsible();

    	ArrayList<Message> at = pendingLookups.get(e.getLookedUpId());
    	
    	for (Message m: at) {
    		//System.out.println("DHT-lookupresulthandler says: Message "+ m +" to id "+ e.getLookedUpId()+" should go to node "+node);
    		send(m, myRef, node, TransportProtocol.TCP);		
    	}
    	//empty the pending-list
    	at = new ArrayList();
    	pendingLookups.put(e.getLookedUpId(), at);
    	
    }

    public void handleJoinBeforePointHook(Object obj) {
	succIndirectionRef = (DKSRef) obj;
	succIndirectionCounter++;
	receiveDataLink++;
//	System.out.println("Debug: receiveDataLink = " + receiveDataLink);
//
//	System.out.println("DEBUG: Hook called at "
//		+ System.currentTimeMillis()
//		+ " and Indirection flag is set to "
//		+ succIndirectionRef.getId() + " on node " + myRef.getId());
     }

    public void handleLeaveBeforePointHook(Object obj) {
	DKSRef leavingPred = registry.getRingMaintainerComponent().getRingState().predecessor;
	DKSRef newPred = (DKSRef) obj;

//	System.out.println("Debug " + System.currentTimeMillis()
//		+ ": handleLeaveBeforePointHook called at " +myRef.getId()+" for interval ]"+newPred.getId()+", "+leavingPred.getId()+"]");
//
	receiveDataLink++;
//	System.out.println("Debug: receiveDataLink = " + receiveDataLink);

	synchronized (this) {
	    predRequestQueues.put(leavingPred.getId(), new PredRequestQueue(newPred.getId(), leavingPred.getId()));
	}


    }

    /*
     * 
     * Below are the handlers for the events issued internally, from the
     * application using the dht
     * 
     */
    public void handlePutRequestEvent(PutRequestEvent event) {
	BigInteger id = calculateID(event.getKey());
//	System.out.println("DEBUG: Put request sent to node responsible for: "+ id);
	
	
	PutRequestMessage m = new PutRequestMessage(id, event);
	
	ArrayList at = pendingLookups.get(id);
	if(at == null) { at = new ArrayList(); at.add(m); pendingLookups.put(id, at);}
	else {at.add(m);}

	triggerReliableLookupRequest(id, LookupStrategy.TRANSITIVE,
		null, SendToIdAckEvent.class, "handleLookupResponse");

    }

    public void handleGetRequestEvent(GetRequestEvent event) {
	BigInteger id = calculateID(event.getKey());
//	System.out.println("DEBUG: Get request sent to node responsible for: "+ id);
	
	GetRequestMessage m = new GetRequestMessage(id, event);
	
	ArrayList at = pendingLookups.get(id);
	if(at == null) { at = new ArrayList(); at.add(m); pendingLookups.put(id, at);}
	else {at.add(m);}

	triggerReliableLookupRequest(id, LookupStrategy.TRANSITIVE,
		null, SendToIdAckEvent.class,
	"handleLookupResponse");

    }
    
    public void handleRemoveRequestEvent(RemoveRequestEvent event) {
	BigInteger id = calculateID(event.getKey());
//	System.out.println("DEBUG: Remove request sent to node responsible for: "+ id);
	
	RemoveRequestMessage m = new RemoveRequestMessage(id, event);
	
	ArrayList at = pendingLookups.get(id);
	if(at == null) { at = new ArrayList(); at.add(m); pendingLookups.put(id, at);}
	else {at.add(m);}
	
	triggerReliableLookupRequest(id, LookupStrategy.TRANSITIVE,
		null, SendToIdAckEvent.class,
	"handleLookupResponse");
    }
    
    
    private BigInteger calculateID(Object key) {
	BigInteger id = null;
	// calculate the ID of this key in the IDSpace;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	try {
	    new ObjectOutputStream(stream).writeObject(key);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	try {
	    MessageDigest digest;
	    digest = MessageDigest.getInstance(HASH_ALGORITHM);
	    id = new BigInteger(digest.digest(stream.toByteArray())).mod(N);
	} catch (NoSuchAlgorithmException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return id;
    }

    
    public void handleHandoverTimeoutEvent(HandoverTimeoutEvent event) {
	ArrayList<HandoverMessage> chunkBuffer = (ArrayList<HandoverMessage>) event
	.getAttachment();
	if (chunkBuffer.size() > 1) {
	    send((Message)chunkBuffer.get(0), chunkBuffer.get(0).getFrom(), chunkBuffer
		    .get(0).getTo());
	    chunkBuffer.remove(0);
	    TimerComponent timer = registry.getTimerComponent();
	    timer.registerTimer(HandoverTimeoutEvent.class, chunkBuffer,
		    DHT_HANDOVER_CHUNK_TIMEOUT);
	} else { // sending last chunk
		
		  send((Message)chunkBuffer.get(0), chunkBuffer.get(0).getFrom(), chunkBuffer
				    .get(0).getTo());
			    chunkBuffer.remove(0);

			    //old:
//		MessageInfo messageInfo = new MessageInfo(chunkBuffer.get(0)
//		    .getFrom(), chunkBuffer.get(0).getTo(),
//		    LastDataChunkSentEvent.class, null, 0);
//	    MarshallMessageEvent marshallMessageEvent = new MarshallMessageEvent(
//		    (Message)chunkBuffer.get(0), messageInfo);
//	    trigger(marshallMessageEvent);
//	    chunkBuffer.remove(0);
	    
	    
	}

    }

    public void handleLastDataChunkSentEvent(LastDataChunkSentEvent e) {
	sendDataLink--;
//	System.out.println("Debug: sendDataLink = " + sendDataLink);
	if(sendDataLink==0)
	    processSendDoneWaitQueue();
    }


public void receiveHandler(DeliverMessageEvent e) {
	internalReceiveHandler(e.getMessage());
}


private void internalReceiveHandler(Message m) {
	
	//System.out.println("Blocked or not??");
	if(m instanceof SuccessorHandoverMessage) {
		handleSuccessorHandoverMessage((SuccessorHandoverMessage)m);
	}
	else if (m instanceof PredecessorHandoverMessage) {
		handlePredecessorHandoverMessage((PredecessorHandoverMessage)m);
	}
	else if (m instanceof PutRequestMessage) {
	//	System.out
	//	.println("DEBUG: Processing a put request from the queue_"+source);
		handleExternalPutRequestEvent((PutRequestMessage)m);
	}
	else if (m instanceof GetRequestMessage) {
	//	System.out
	//	.println("DEBUG: Processing a get request from the queue_"+source);
		handleExternalGetRequestEvent((GetRequestMessage)m);
	    }
	else if (m instanceof RemoveRequestMessage) {
	//	System.out
	//	.println("DEBUG: Processing a remove request from the queue");
		handleExternalRemoveRequestEvent((RemoveRequestMessage)m);
	}
	 else {
		 System.out.println("DEBUG: GRRRRRRRRRRRR! Unknown event of type " + m.getClass().getCanonicalName() + " in handling!");
	 }

	
	
}
    /*
     * 
     * Below are the handlers for the response events corresponding to
     * respons messages from other nodes which are responding to put, get or
     * remove requests
     * 
     */

    public void handlePutAckEvent(DeliverMessageEvent event) {

	PutAckMessage msg = (PutAckMessage) event.getMessage();
	// System.out.println("DEBUG: handlePutAckEvent: "+msg.getResult());
	trigger(new ApplicationPutAckEvent(msg));
    }

    public void handleGetResponseEvent(DeliverMessageEvent gre) {
	GetResponseMessage grm = (GetResponseMessage) gre.getMessage();

	ApplicationGetResponseEvent getResultEvent = new ApplicationGetResponseEvent(
		grm, grm.getResult());
	trigger(getResultEvent);

    }

    public void handleRemoveAckEvent(DeliverMessageEvent event) {
	RemoveAckMessage msg = (RemoveAckMessage) event.getMessage();
	trigger(new ApplicationRemoveAckEvent(msg));
		
    }

    /*
     * 
     * Below are the handlers for the request events corresponding to
     * request messages from other nodes which have made put, get or remove
     * requests
     * 
     */

    public synchronized void handleExternalPutRequestEvent(PutRequestMessage prm) {
	

	//if (succIndirectionCounter <= 0) {
	if (receiveDataLink==0) { // I'm not receiving anything! so I have all data
		processExternalPutRequest(prm);
	} else {
		//System.out.println("No, I'm going here");
	    for (PredRequestQueue queue : predRequestQueues.values()) {
		if(queue.range.contains(prm.getId())){
		    queue.queue.add(prm);
//		    System.out.println("DEBUG: Put request from "
//			    + epre.getSource().getId() + " received at "
//			    + myRef.getId() + " ID: " + prm.getId() + " Key: "
//			    + prm.getKey() + " Value: " + prm.getValue()
//			    + " and placed in PRED_QUEUE_"+queue.range+", IndCnt="
//			    + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
		    return;
		}

	    }

	    if (succIndirectionCounter <= 0) {  // I'm receiving data but for this request I have data
		processExternalPutRequest(prm);
	    } else {
		joinRequestQueue.add(prm);
//		System.out.println("DEBUG: Put request from "
//			+ epre.getSource().getId() + " received at "
//			+ myRef.getId() + " ID: " + prm.getId() + " Key: "
//			+ prm.getKey() + " Value: " + prm.getValue()
//			+ " and placed in QUEUE, IndCnt=" + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
	    }
	}
    }
    
    private void processExternalPutRequest(PutRequestMessage prm) {
	
	boolean result = myLocalStore.put(prm.getId(), prm.getKey(), prm
		    .getValue(), prm.getFlavor(), prm.isMultiVal());

//	    System.out.println("DEBUG: Put request from "
//		    + prm.getSource().getId() + " received at "
//		    + myRef.getId() + " ID: " + prm.getId() + " Key: "
//		    + prm.getKey() + " Value: " + prm.getValue() + " Result: "
//		    + result + " IndCnt=" + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);

	    // if(prm.acknowledgementWanted()) {

	    PutAckMessage pam = new PutAckMessage(prm, result);
	    send(pam, myRef, prm.getSource());
//	    OperationResponseEvent operationResponseEvent = new OperationResponseEvent(
//		    epre, pam);
//	    trigger(operationResponseEvent);
	    // }
    }

    public synchronized void handleExternalGetRequestEvent(GetRequestMessage grm) {
	//GetRequestMessage grm = (GetRequestMessage) egre.getMessage();

	//if (succIndirectionCounter <= 0) {
	if (receiveDataLink==0) { // I'm not receiving anything! so I have all data
	    processExternalGetRequest(grm);
	} else {

	    for (PredRequestQueue queue : predRequestQueues.values()) {
		if(queue.range.contains(grm.getId())){
		    queue.queue.add(grm);
//		    System.out.println("DEBUG: Get request from "
//			    + egre.getSource().getId() + " received at "
//			    + myRef.getId() + " ID: " + grm.getId() + " Key: "
//			    + grm.getKey() + " and placed in PRED_QUEUE_"+queue.range+", IndCnt="
//			    + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
		    return;
		}

	    }

	    if (succIndirectionCounter <= 0) {
		processExternalGetRequest(grm);
	    } else {
		joinRequestQueue.add(grm);
//		System.out.println("DEBUG: Get request from "
//			+ egre.getSource().getId() + " received at "
//			+ myRef.getId() + " ID: " + grm.getId() + " Key: "
//			+ grm.getKey() + " and placed in JOIN_QUEUE, IndCnt="
//			+ succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
	    }
	}

    }

    private void processExternalGetRequest(GetRequestMessage grm) {
	//GetRequestMessage grm = (GetRequestMessage) egre.getMessage();

	 Object result = myLocalStore.get(grm.getId(), grm.getKey(), grm
		    .getFlavor(), grm.getPosition());
//	    System.out.println("DEBUG: Get request from "
//		    + egre.getSource().getId() + " received at "
//		    + myRef.getId() + " ID: " + grm.getId() + " Key: "
//		    + grm.getKey() + " Result: " + result + " IndCnt="
//		    + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
	    GetResponseMessage getResponseMessage = new GetResponseMessage(grm,
		    result);

	    send(getResponseMessage, myRef, grm.getSource());
//	    OperationResponseEvent operationResponseEvent = new OperationResponseEvent(
//		    egre, getResponseMessage);
//
//	    trigger(operationResponseEvent);
	
    }
    
    public synchronized void handleExternalRemoveRequestEvent(RemoveRequestMessage rrm) {
	//RemoveRequestMessage rrm = (RemoveRequestMessage) erre.getMessage();

	//if (succIndirectionCounter <= 0) {
	if (receiveDataLink==0) { // I'm not receiving anything! so I have all data
	    processExternalRemoveRequest(rrm);
	} else {

	    for (PredRequestQueue queue : predRequestQueues.values()) {
		if(queue.range.contains(rrm.getId())){
		    queue.queue.add(rrm);
//		    System.out.println("DEBUG: Remove request from "
//			    + erre.getSource().getId() + " received at "
//			    + myRef.getId() + " ID: " + rrm.getId() + " Key: "
//			    + rrm.getKey() + " and placed in PRED_QUEUE_"+queue.range+", IndCnt="
//			    + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
		    return;
		}

	    }

	    if (succIndirectionCounter <= 0) {
		processExternalRemoveRequest(rrm);
	    } else {
		joinRequestQueue.add(rrm);
//		System.out.println("DEBUG: Remove request from "
//			+ erre.getSource().getId() + " received at "
//			+ myRef.getId() + " ID: " + rrm.getId() + " Key: "
//			+ rrm.getKey() + " and placed in JOIN_QUEUE, IndCnt="
//			+ succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
	    }
	}

    }
    
    private void processExternalRemoveRequest(RemoveRequestMessage rrm) {
	//RemoveRequestMessage rrm = (RemoveRequestMessage) erre.getMessage();

	 Object result = myLocalStore.remove(rrm.getId(), rrm.getKey(), rrm.getFlavor(), rrm.getPosition());
	 
//	    System.out.println("DEBUG: Remove request from "
//		    + erre.getSource().getId() + " received at "
//		    + myRef.getId() + " ID: " + rrm.getId() + " Key: "
//		    + rrm.getKey() + " Result: " + result + " IndCnt="
//		    + succIndirectionCounter + ", rdl="+receiveDataLink+", sdl="+sendDataLink);
	    RemoveAckMessage removeResponseMessage = new RemoveAckMessage(rrm,result);

	    send(removeResponseMessage, myRef, rrm.getSource());
//	    OperationResponseEvent operationResponseEvent = new OperationResponseEvent(
//		    erre, removeResponseMessage);
//
//	    trigger(operationResponseEvent);
    }
   

    /*
     * 
     * Below are the handlers for dealing with nodes joining and leaving
     * 
     */

    public void handleRingNewPredecessorEvent(RingNewPredecessorEvent e) {

	//if (succIndirectionCounter <= 0) { // I have the data
	if(receiveDataLink == 0) { // I have the data
	    sendDataLink++; // indecating that I'm sending data to a node
//	    System.out.println("Debug: sendDataLink = " + sendDataLink);
	    BigInteger newPred = e.getNewPredecessor().getId();
	    BigInteger oldPred = e.getOldPredecessor().getId();
//	    System.out.println("DEBUG: New Predecessor found! Moving data ] "
//		    + oldPred + " ," + newPred + "] from " + myRef.getId()
//		    + " to " + newPred);

	    // DEBUG REMOVE ME or set me to 1
	    int mul = 1;

	    ArrayList<Object> buffer = myLocalStore.getRange(oldPred, newPred, DHT_HANDOVER_CHUNK_SIZE);
	    int i = 0;
	    ArrayList<SuccessorHandoverMessage> chunkBuffer = new ArrayList<SuccessorHandoverMessage>(buffer.size() * mul);

	    for (; i < (buffer.size() * mul); i++) {
		// send(new
		// SuccessorHandoverMessage(buffer.get(i%buffer.size()),i+1,
		// buffer.size()*mul), myRef, e.getNewPredecessor());
		chunkBuffer.add(new SuccessorHandoverMessage(buffer.get(i
			% buffer.size()), i + 1, buffer.size() * mul, myRef, e
			.getNewPredecessor()));
		// DEBUG REMOVE ME
		// Thread.yield();

		// try {
		// Thread.sleep(50);
		// } catch (InterruptedException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }

	    }

	    sendDoneWaitQueue.add(new DHTCleanupSchecule(oldPred, newPred));
	    TimerComponent timer = registry.getTimerComponent();
	    timer.registerTimer(HandoverTimeoutEvent.class, chunkBuffer,
		    DHT_HANDOVER_CHUNK_TIMEOUT);

	} else { // I don't have all data so put request in the queue
//	    BigInteger newPred = e.getNewPredecessor().getId();
//	    BigInteger oldPred = e.getOldPredecessor().getId();
//	    System.out
//	    .println("DEBUG: RECEIVE_WAIT_QUEUE: New Predecessor found! Moving data ] "
//		    + oldPred
//		    + " ,"
//		    + newPred
//		    + "] from "
//		    + myRef.getId() + " to " + newPred);
	    synchronized (this) {
		receiveDoneWaitQueue.add(e);
	    }
	}

    }

    public void handleRingLeaveDoneInterceptorEvent(RingLeaveDoneInterceptorEvent e) {

	RingState ringState = registry.getRingMaintainerComponent()
	.getRingState();

	if (ringState.predecessor.equals(ringState.successor)
		&& ringState.successor.equals(myRef)) {
	    // I'm the last node
//	    System.out.println("DEBUG: I'm the last node! no one to send data to!");
//	    System.out.println("Removing All DHT");
	    myLocalStore.removeAll();

	} else {

	    if(receiveDataLink == 0) {
		sendDataLink++;
//		System.out.println("Debug: sendDataLink = " + sendDataLink);
//
//		System.out.println("DEBUG " + System.currentTimeMillis()
//			+ ": Sending ALL DHT DataChunks from " + myRef.getId()
//			+ " to succ " + ringState.successor.getId());

		// DEBUG REMOVE ME or set me to 1    
		int mul = 1;

		ArrayList<Object> buffer = myLocalStore.getAll(DHT_HANDOVER_CHUNK_SIZE);
		int i = 0;
		ArrayList<PredecessorHandoverMessage> chunkBuffer = new ArrayList<PredecessorHandoverMessage>(
			buffer.size() * mul);

		for (; i < (buffer.size() * mul); i++) {
		    chunkBuffer.add(new PredecessorHandoverMessage(buffer.get(i % buffer.size()), i + 1, buffer.size() * mul, myRef,e.getSuccesor()));
		}

		synchronized (this) {
		    sendDoneWaitQueue.add(new RingLeaveDoneInterceptorAckEvent());
		}

		sendDoneWaitQueue.add(new DHTCleanupSchecule());
		TimerComponent timer = registry.getTimerComponent();
		timer.registerTimer(HandoverTimeoutEvent.class, chunkBuffer,
			DHT_HANDOVER_CHUNK_TIMEOUT);


	    } else {
		synchronized (this) {
//		    System.out.println("DEBUG: RECEIVE_WAIT_QUEUE: Sending ALL DHT DataChunks from " + myRef.getId()
//			    + " to succ " + ringState.successor.getId());;
			    receiveDoneWaitQueue.add(e);
		}
	    }
	}
    }

    
public void handleSuccessorHandoverMessage(SuccessorHandoverMessage msg) {
	

	if (succChunkCounter == 0)
	    succChunkCounter = msg.getTotalChunks();
	succChunkCounter--;

	// System.out.println("DEBUG: test buffer is = " + msg.getBuffer());
//	System.out.println("DEBUG " + System.currentTimeMillis()
//		+ ": Got successor handover data chunk " + msg.getChunkID()
//		+ "/" + msg.getTotalChunks() + " from "
//		+ event.getMessageInfo().getSource().getId() + " and "
//		+ succChunkCounter + " Chunks left");
	myLocalStore.putAll(msg.getBuffer());

	// if(msg.isStopIndirection()) {
	// synchronized(externalGetRequestQueue){
	if (succChunkCounter == 0) {

//	    System.out.println("DEBUG: Stopping the SUCC_QUEUE at "
//		    + System.currentTimeMillis());
	    succIndirectionRef = null; // what is good order to do this?
	    // (consistency guarantee)
	    succIndirectionCounter--;
	    processJoinRequestQueue();
	}
	// }

    }

    public void handlePredecessorHandoverMessage(PredecessorHandoverMessage msg) {

	BigInteger source = msg.getSource().getId();

	PredRequestQueue queue = predRequestQueues.get(source);

	if (queue.getChunkCounter() == 0)
	    queue.setChunkCounter(msg.getTotalChunks());
	queue.decrementChunkCounter();

//	System.out.println("DEBUG " + System.currentTimeMillis()
//		+ ": Got predecessor handover data chunk " + msg.getChunkID()
//		+ "/" + msg.getTotalChunks() + " from "
//		+ event.getMessageInfo().getSource().getId() + " and "
//		+ queue.getChunkCounter() + " Chunks left");
	myLocalStore.putAll(msg.getBuffer());

	if (queue.getChunkCounter() == 0) {
//	    System.out.println("DEBUG: Stopping the PRED_QUEUE_"+source+" at "+ System.currentTimeMillis());
	    processPredRequestQueues(source);

	}



    }

    /**
     * 
     */
    private synchronized void processReceiveDoneWaitQueue() {

	for (Event e : receiveDoneWaitQueue) {
	    if (e instanceof RingNewPredecessorEvent) {
//		System.out.println("DEBUG: Processing a RingNewPredecessorEvent from receiveDoneWaitQueue");
		handleRingNewPredecessorEvent((RingNewPredecessorEvent) e);
	    } else if (e instanceof RingLeaveDoneInterceptorEvent) {
//		System.out.println("DEBUG: Processing a RingLeaveDoneInterceptorEvent from receiveDoneWaitQueue");
		handleRingLeaveDoneInterceptorEvent((RingLeaveDoneInterceptorEvent) e);
	    } else {
//		System.out.println("DEBUG: GRRRRRRRRRRRR! Unknown event of type " + e.getClass().getCanonicalName() + " in processReceiveDoneWaitQueue!");
	    }
	}
	receiveDoneWaitQueue.clear();
    }

    /**
     * 
     */
    private synchronized void processSendDoneWaitQueue() {
	for (Event e : sendDoneWaitQueue) {
	    if (e instanceof RingLeaveDoneInterceptorAckEvent) {
//		System.out
//		.println("DEBUG: Processing a RingLeaveDoneInterceptorAckEvent from sendDoneWaitQueue");
		trigger(e);
	    } else if (e instanceof DHTCleanupSchecule) {
		DHTCleanupSchecule s = (DHTCleanupSchecule)e;
		
//		System.out.println("DEBUG: Cleaning up DHT! " + s);
		s.startCleanup();
		
	    } else {
//		System.out.println("DEBUG: GRRRRRRRRRRRR! Unknown event of type " + e.getClass().getCanonicalName() + " in processSendDoneWaitQueue!");
	    }
	}
	sendDoneWaitQueue.clear();
    }

    /**
     * 
     */
    private synchronized void processJoinRequestQueue() {
	receiveDataLink--;
//	System.out.println("Debug: receiveDataLink = " + receiveDataLink);
	
	for (Message m : joinRequestQueue) {
		internalReceiveHandler(m);
	}
	joinRequestQueue.clear();
	
	if(receiveDataLink==0)
	    processReceiveDoneWaitQueue();

    }

    private synchronized void processPredRequestQueues(BigInteger source) {
	PredRequestQueue queue = predRequestQueues.get(source);
	receiveDataLink--;
//	System.out.println("Debug: receiveDataLink = " + receiveDataLink);
	
	
	for (Message m : queue.queue) {
		//This is shortended by J
		internalReceiveHandler(m);
	}
	queue.queue.clear();
	predRequestQueues.remove(source);

	if(receiveDataLink==0)
	    processReceiveDoneWaitQueue();
    }

    /*
     * 
     * Below are the handlers for dealing with nodes failing
     * 
     */

    public void handleCommPeerSuspectedEvent(SuspectEvent event) {
	BigInteger susPeer = event.peer.getId();
//	System.out.println("DHT-handler: node " + susPeer
//		+ " is suspected at node " + myRef.getId());

    }

    private class PredRequestQueue {
	IntervalsList range;
	ArrayList<Message> queue;
	int chunkCounter;

	/**
	 * 
	 */
	public PredRequestQueue(BigInteger newPred, BigInteger leavingPred) {
	    queue = new ArrayList<Message>();
	    try {
		range = new IntervalsList(newPred, leavingPred, Bounds.OPEN_CLOSED, N);
	    } catch (SimpleIntervalException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    chunkCounter=0;
	}

	/**
	 * @return Returns the chunkCounter.
	 */
	public int getChunkCounter() {
	    return chunkCounter;
	}

	/**
	 * @param chunkCounter The chunkCounter to set.
	 */
	public void setChunkCounter(int chunkCounter) {
	    this.chunkCounter = chunkCounter;
	}

	public void decrementChunkCounter() {
	    chunkCounter--;
	}

    }

    private class DHTCleanupSchecule extends Event {  // this is not really an event! just to be compatible with other stuff in the queue
	BigInteger from;
	BigInteger to;
	boolean all;
	
	
	/**
	 * 
	 */
	public DHTCleanupSchecule() {
	    all = true;
	}
	/**
	 * 
	 */
	public DHTCleanupSchecule(BigInteger from, BigInteger to) {
	   this.from = from;
	   this.to = to;
	   this.all = false;
	}
	
	public void startCleanup() {
	    if(all) {
		myLocalStore.removeAll();
	    } else {
		myLocalStore.removeRange(from, to);
	    }
	}
	@Override
	public String toString() {
	    String s = "Removing ";
	    if(all) {
		s += "All";
	    } else {
		s+= "from " + from + " to " + to;
	    }
	    return s;
	}
	
	
    }
}
