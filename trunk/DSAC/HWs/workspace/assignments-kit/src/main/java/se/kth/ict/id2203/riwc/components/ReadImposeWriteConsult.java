package se.kth.ict.id2203.riwc.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebDeliver;
import se.kth.ict.id2203.beb.events.BebMessage;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.lpb.components.LazyPB;
import se.kth.ict.id2203.pfd.events.CrashEvent;
import se.kth.ict.id2203.pfd.ports.PerfectFailureDetector;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pDeliver;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.kth.ict.id2203.riwc.RIWCInit;
import se.kth.ict.id2203.riwc.events.ACKMessage;
import se.kth.ict.id2203.riwc.events.ReadRequest;
import se.kth.ict.id2203.riwc.events.ReadResponse;
import se.kth.ict.id2203.riwc.events.WriteMessage;
import se.kth.ict.id2203.riwc.events.WriteRequest;
import se.kth.ict.id2203.riwc.events.WriteResponse;
import se.kth.ict.id2203.riwc.ports.AtomicRegister;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class ReadImposeWriteConsult extends ComponentDefinition {
	Positive<BEBPort> beb = positive(BEBPort.class);
	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Positive<PerfectFailureDetector> pfd = positive(PerfectFailureDetector.class);
	Negative<AtomicRegister> atomicRegister = negative(AtomicRegister.class);

	private static final Logger logger = LoggerFactory.getLogger(LazyPB.class);

	private Set<Address> neighborSet;
	private ArrayList<Address> neighborList;
	private Address self;
	
	//application specific
	private ArrayList<Address> correct; 
	private int i;
	private int r;
	private HashMap<Integer, ArrayList<Address>> writeSet;
	private ArrayList<Boolean> reading;
	private ArrayList<Integer> reqid;
	private ArrayList<String> readval;
	private ArrayList<String> v;
	private ArrayList<Integer> ts;
	private ArrayList<Integer> mrank;

	public ReadImposeWriteConsult() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pDeliver, pp2p);
		subscribe(handleBebMessage, beb);
		subscribe(handleCrashMessage, pfd);
		subscribe(handleReadRequestMessage, atomicRegister);
		subscribe(handleWriteRequestMessage, atomicRegister);
	}

	Handler<RIWCInit> handleInit = new Handler<RIWCInit>() {
		public void handle(RIWCInit event) {
			neighborSet = event.getNeighborSet();
			neighborList = new ArrayList<Address>();
			neighborList.addAll(neighborSet);
			correct = neighborList;
			self = event.getSelf();
			i = self.getId();
			r = event.getNumberOfRegister();
			
			//4-12
			for(int j=0; j<r; j++){
				writeSet.put(j, new ArrayList<Address>());
				reading.set(j, false);
				reqid.set(j, 0);
				readval.set(j, "0");
				v.set(j, "0");
				ts.set(j, 0);
				mrank.set(j, 0);
			}
			
			logger.debug("lazyPBroadcast :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};
	
	Handler<Pp2pDeliver> handlePp2pDeliver = new Handler<Pp2pDeliver>() {
		public void handle(Pp2pDeliver event) {
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
	
	Handler<BebDeliver> handleBebMessage = new Handler<BebDeliver>() {
		public void handle(BebDeliver event) {
			//2-6
			WriteMessage wm = (WriteMessage) event;
			int rr = wm.getRegister();
			if( (wm.getTimestamp()>ts.get(rr)) || (wm.getTimestamp()==ts.get(rr) && wm.getProcessRank() > mrank.get(rr)) ){
				v.set(rr, wm.getMessage());
				ts.set(rr, wm.getTimestamp());
				mrank.set(rr, wm.getProcessRank());
			}
			//7
			trigger(new Pp2pSend(wm.getSender(), new ACKMessage(self, wm.getRegister(), wm.getRequestID())), pp2p);
		}
	};
	
	Handler<CrashEvent> handleCrashMessage = new Handler<CrashEvent>() {
		public void handle(CrashEvent event) {
			//15
			Address crashedAddress = event.getAddress();
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
			trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
		}
	};
	
	private void checkIfReturn(int rr){
		if (writeSet.get(rr).containsAll(correct)){
			if(reading.get(rr) == true){
				reading.set(rr, false);
				trigger(new ReadResponse(rr, readval.get(rr)), atomicRegister);
			} else {
				trigger(new WriteResponse(rr), atomicRegister);
			}
		}
	}
}
