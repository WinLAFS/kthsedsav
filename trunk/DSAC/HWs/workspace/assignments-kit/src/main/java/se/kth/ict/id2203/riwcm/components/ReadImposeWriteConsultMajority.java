package se.kth.ict.id2203.riwcm.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebMessage;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.pfd.events.CrashEvent;
import se.kth.ict.id2203.pfd.ports.PerfectFailureDetector;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.kth.ict.id2203.riwcm.RIWCMInit;
import se.kth.ict.id2203.riwcm.events.ACKMessage;
import se.kth.ict.id2203.riwcm.events.ReadRequest;
import se.kth.ict.id2203.riwcm.events.ReadResponse;
import se.kth.ict.id2203.riwcm.events.WriteMessage;
import se.kth.ict.id2203.riwcm.events.WriteRequest;
import se.kth.ict.id2203.riwcm.events.WriteResponse;
import se.kth.ict.id2203.riwcm.ports.AtomicRegister;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class ReadImposeWriteConsultMajority extends ComponentDefinition {
	Positive<BEBPort> beb = positive(BEBPort.class);
	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Negative<PerfectFailureDetector> pfd = negative(PerfectFailureDetector.class);
	Negative<AtomicRegister> atomicRegister = negative(AtomicRegister.class);

	private static final Logger logger = LoggerFactory.getLogger(ReadImposeWriteConsultMajority.class);

	private Set<Address> neighborSet;
	private ArrayList<Address> neighborList;
	private Address self;
	
	//application specific
	private ArrayList<Address> correct; 
	private int i;
	private int r;
	private HashMap<Integer, ArrayList<Address>> writeSet;
	private ArrayList<Boolean> reading = new ArrayList<Boolean>();
	private ArrayList<Integer> reqid = new ArrayList<Integer>();
	private ArrayList<String> readval = new ArrayList<String>();
	private ArrayList<String> v = new ArrayList<String>();
	private ArrayList<Integer> ts = new ArrayList<Integer>();
	private ArrayList<Integer> mrank = new ArrayList<Integer>();

	public ReadImposeWriteConsultMajority() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pDeliver, pp2p);
		subscribe(handleBebMessage, beb);
		subscribe(handleCrashMessage, pfd);
		subscribe(handleReadRequestMessage, atomicRegister);
		subscribe(handleWriteRequestMessage, atomicRegister);
	}

	Handler<RIWCMInit> handleInit = new Handler<RIWCMInit>() {
		public void handle(RIWCMInit event) {
			neighborSet = event.getNeighborSet();
			neighborList = new ArrayList<Address>();
			neighborList.addAll(neighborSet);
			correct = neighborList;
			self = event.getSelf();
			i = self.getId();
			r = event.getNumberOfRegister();
			
			writeSet = new HashMap<Integer, ArrayList<Address>>();
			//4-12
			for(int j=0; j<r; j++){
				writeSet.put(j, new ArrayList<Address>());
				reading.add(j, false);
				reqid.add(j, 0);
				readval.add(j, "0");
				v.add(j, "0");
				ts.add(j, 0);
				mrank.add(j, 0);
			}
			
			logger.debug("lazyPBroadcast :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};
	
	Handler<ACKMessage> handlePp2pDeliver = new Handler<ACKMessage>() {
		public void handle(ACKMessage event) {
			logger.info("ACK from "+event.getSource());
			ACKMessage ack = (ACKMessage) event;
			int rr = ack.getRegister();
			//10-12
			if(ack.getRequestID()==reqid.get(ack.getRegister())){
				//TODO check if works
				writeSet.get(rr).add(ack.getSource());
				checkIfReturn(rr);
			}
		}
	};
	
	Handler<WriteMessage> handleBebMessage = new Handler<WriteMessage>() {
		public void handle(WriteMessage event) {
			//2-6
			WriteMessage wm = (WriteMessage) event;
			int rr = wm.getRegister();
			if( (wm.getTimestamp()>ts.get(rr)) || (wm.getTimestamp()==ts.get(rr) && wm.getProcessRank() > mrank.get(rr)) ){
				v.set(rr, wm.getMessage());
				ts.set(rr, wm.getTimestamp());
				mrank.set(rr, wm.getProcessRank());
			}
			//7
			logger.info("Sending ACK. From:"+self+"\t To:"+wm.getSender()+"\tR:"+wm.getRegister());
			trigger(new Pp2pSend(wm.getSender(), new ACKMessage(self, wm.getRegister(), wm.getRequestID())), pp2p);
		}
	};
	
	Handler<CrashEvent> handleCrashMessage = new Handler<CrashEvent>() {
		public void handle(CrashEvent event) {
			//15
			Address crashedAddress = event.getAddress();
			logger.info("received crash event. Node:"+crashedAddress);
			//TODO check if works
			correct.remove(crashedAddress);
		}
	};
	
	Handler<ReadRequest> handleReadRequestMessage = new Handler<ReadRequest>() {
		public void handle(ReadRequest event) {
			//18-21
			int rr = event.getRegister();
			reqid.set(rr, (reqid.get(rr)+1));
			reading.set(rr, true);
			writeSet.put(rr, new ArrayList<Address>());
			readval.set(rr, v.get(rr));
			
			//22
			WriteMessage wm = new WriteMessage(v.get(rr), self, rr, reqid.get(rr), ts.get(rr), mrank.get(rr));
			logger.info("Received read request. R:"+rr+"\t Value:"+v.get(rr));
			trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
		}
	};
	
	Handler<WriteRequest> handleWriteRequestMessage = new Handler<WriteRequest>() {
		public void handle(WriteRequest event) {
			//25-26
			int rr = event.getRegister();
			String val = event.getValue();
			reqid.set(rr, (reqid.get(rr)+1));
			writeSet.put(rr, new ArrayList<Address>());
			
			//27
			WriteMessage wm = new WriteMessage(val, self, rr, reqid.get(rr), (ts.get(rr)+1), i);
			logger.info("Received write request. R:"+rr+"\t Value:"+val);
			trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
		}
	};
	
	private void checkIfReturn(int rr){
		if (writeSet.get(rr).containsAll(correct)){
			if(reading.get(rr) == true){
				reading.set(rr, false);
				logger.info("Sending read responce. R:"+rr+"\t Value:"+readval.get(rr));
				trigger(new ReadResponse(rr, readval.get(rr)), atomicRegister);
			} else {
				logger.info("Sending write responce. R:"+rr);
				trigger(new WriteResponse(rr), atomicRegister);
			}
		}
	}
}
