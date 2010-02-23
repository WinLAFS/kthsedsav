package se.kth.ict.id2203.riwcm.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebMessage;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.kth.ict.id2203.riwcm.RIWCMInit;
import se.kth.ict.id2203.riwcm.beans.ReadSetBean;
import se.kth.ict.id2203.riwcm.events.ACKMessage;
import se.kth.ict.id2203.riwcm.events.ReadMessage;
import se.kth.ict.id2203.riwcm.events.ReadRequest;
import se.kth.ict.id2203.riwcm.events.ReadResponse;
import se.kth.ict.id2203.riwcm.events.ReadValueDeliver;
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
	Negative<AtomicRegister> atomicRegister = negative(AtomicRegister.class);

	private static final Logger logger = LoggerFactory.getLogger(ReadImposeWriteConsultMajority.class);

	private Set<Address> neighborSet;
	private ArrayList<Address> neighborList;
	private Address self;
	
	//application specific
	private int i;
	private int r;
	private HashMap<Integer, ArrayList<Address>> writeSet;
	private HashMap<Integer, ArrayList<ReadSetBean>> readSet;
	private ArrayList<Boolean> reading = new ArrayList<Boolean>();
	private ArrayList<Integer> reqid = new ArrayList<Integer>();
	private ArrayList<String> readval = new ArrayList<String>();
	private ArrayList<String> v = new ArrayList<String>();
	private ArrayList<Integer> ts = new ArrayList<Integer>();
	private ArrayList<Integer> mrank = new ArrayList<Integer>();
	private ArrayList<String> writeval = new ArrayList<String>();
	private int majoritySize;

	public ReadImposeWriteConsultMajority() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handlePp2pAckDeliver, pp2p);
		subscribe(handlePp2pReadValueDeliver, pp2p);
		subscribe(handleBebWrite, beb);
		subscribe(handleBebRead, beb);
		subscribe(handleReadRequestMessage, atomicRegister);
		subscribe(handleWriteRequestMessage, atomicRegister);
	}

	Handler<RIWCMInit> handleInit = new Handler<RIWCMInit>() {
		public void handle(RIWCMInit event) {
			neighborSet = event.getNeighborSet();
			neighborList = new ArrayList<Address>();
			neighborList.addAll(neighborSet);
			self = event.getSelf();
			i = self.getId();
			r = event.getNumberOfRegister();
			majoritySize = event.getMajoritySize();
			
			logger.info("#Neighbors: " + neighborList.size() + " : #Majority:" + majoritySize);
			
			writeSet = new HashMap<Integer, ArrayList<Address>>();
			readSet = new HashMap<Integer, ArrayList<ReadSetBean>>();
			//4-12
			for(int j=0; j<r; j++){
				writeSet.put(j, new ArrayList<Address>());
				reading.add(j, false);
				reqid.add(j, 0);
				readval.add(j, "0");
				v.add(j, "0");
				ts.add(j, 0);
				mrank.add(j, 0);
				writeval.add(j, "0");
				readSet.put(j, new ArrayList<ReadSetBean>());
			}
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};
	
	Handler<ACKMessage> handlePp2pAckDeliver = new Handler<ACKMessage>() {
		public void handle(ACKMessage event) {
			logger.info("<2> ACK from: "+event.getSource());
			ACKMessage ack = (ACKMessage) event;
			int rr = ack.getRegister();
			//10-12
			if(ack.getRequestID()==reqid.get(ack.getRegister())){
				writeSet.get(rr).add(ack.getSource());
				checkIfReturn(rr);
			}
		}
	};
	
	Handler<ReadValueDeliver> handlePp2pReadValueDeliver = new Handler<ReadValueDeliver>() {
		public void handle(ReadValueDeliver event) {
			
			int rr = event.getRegister();
			
			if (event.getRequestID() == reqid.get(rr)){
				logger.info("<1> Read value from:" +event.getSource());
				ReadSetBean rsb = new ReadSetBean(event.getTimestamp(), event.getProcessRank(), event.getValue());
				readSet.get(rr).add(rsb);
				checkIfGotGlobalId(rr);
			}
		}
	};
	
	Handler<ReadMessage> handleBebRead = new Handler<ReadMessage>() {
		public void handle(ReadMessage event) {
			
			ReadMessage rm = (ReadMessage) event;
			int rr = rm.getRegister();
			
			logger.info("<1> Send my values to:"+rm.getSender() + " for R: " + rr);
			ReadValueDeliver rvd = new ReadValueDeliver(self, rm.getRegister(), rm.getRequestID(), ts.get(rr), mrank.get(rr), v.get(rr));
			trigger(new Pp2pSend(rm.getSender(), rvd), pp2p);
			
		}
	};
	
	Handler<WriteMessage> handleBebWrite = new Handler<WriteMessage>() {
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
//			logger.info("Sending ACK. From:"+self+"\t To:"+wm.getSender()+"\tR:"+wm.getRegister());
			trigger(new Pp2pSend(wm.getSender(), new ACKMessage(self, wm.getRegister(), wm.getRequestID())), pp2p);
		}
	};

	
	Handler<ReadRequest> handleReadRequestMessage = new Handler<ReadRequest>() {
		public void handle(ReadRequest event) {
			//
			int rr = event.getRegister();
			reqid.set(rr, (reqid.get(rr)+1));
			reading.set(rr, true);
			readSet.put(rr, new ArrayList<ReadSetBean>());
			writeSet.put(rr, new ArrayList<Address>());
//			readval.set(rr, v.get(rr));
			
			//
//			WriteMessage wm = new WriteMessage(v.get(rr), self, rr, reqid.get(rr), ts.get(rr), mrank.get(rr));
//			logger.info("Received read request. R:"+rr+"\t Value:"+v.get(rr));
//			trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
			
			ReadMessage rm = new ReadMessage(self, rr, reqid.get(rr));
			logger.info("<1> Starting READ request. R:"+rr);
			trigger(new BebBroadcast(new BebMessage(self, rm), self), beb);
		}
	};
	
	Handler<WriteRequest> handleWriteRequestMessage = new Handler<WriteRequest>() {
		public void handle(WriteRequest event) {
			//25-26
			int rr = event.getRegister();
			String val = event.getValue();
			
			reqid.set(rr, (reqid.get(rr)+1));
			writeval.set(rr, val);
			writeSet.put(rr, new ArrayList<Address>());
			readSet.put(rr, new ArrayList<ReadSetBean>());
			
			//27
//			WriteMessage wm = new WriteMessage(val, self, rr, reqid.get(rr), (ts.get(rr)+1), i);
//			logger.info("Received write request. R:"+rr+"\t Value:"+val);
//			trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
			
			ReadMessage rm = new ReadMessage(self, rr, reqid.get(rr));
			logger.info("<1> Starting WRITE request. R: "+rr + "\tV: " + val);
			trigger(new BebBroadcast(new BebMessage(self, rm), self), beb);
		}
	};
	
	private void checkIfReturn(int rr){
		if (writeSet.get(rr).size() >= majoritySize){
			writeSet.put(rr, new ArrayList<Address>());
			if(reading.get(rr) == true){
				reading.set(rr, false);
				logger.info("<2> Finished read. R: "+rr+"\tV: "+readval.get(rr));
				trigger(new ReadResponse(rr, readval.get(rr)), atomicRegister);
			} else {
				logger.info("<2> Finished write. R: "+rr);
				trigger(new WriteResponse(rr), atomicRegister);
			}
		}
	}
	
	private void checkIfGotGlobalId(int rr){
		if(readSet.get(rr).size()>=majoritySize){
			
			ReadSetBean rsbh = new ReadSetBean(-1, -1, "");
			ArrayList<ReadSetBean> rsbList = readSet.get(rr);
			
			readSet.put(rr, new ArrayList<ReadSetBean>());
			
			Iterator<ReadSetBean> it = rsbList.iterator();
			while (it.hasNext()) {
				ReadSetBean readSetBean = (ReadSetBean) it.next();
				
				if( (readSetBean.getTimestamp()>rsbh.getTimestamp()) || (readSetBean.getTimestamp()==rsbh.getTimestamp() && readSetBean.getProcessRank() > rsbh.getProcessRank()) ){
					rsbh = readSetBean;
				}
			}
			
			readval.set(rr, rsbh.getValue());
			logger.info("<1> Finished update TS for: R: " + rr);
			if(reading.get(rr)){
				WriteMessage wm = new WriteMessage(readval.get(rr), self, rr, reqid.get(rr), rsbh.getTimestamp(), rsbh.getProcessRank());
				trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
			} else {
				WriteMessage wm = new WriteMessage(writeval.get(rr), self, rr, reqid.get(rr), rsbh.getTimestamp()+1, i);
				trigger(new BebBroadcast(new BebMessage(self, wm), self), beb);
			}
		}
	}
}
