package se.kth.ict.id2203.ac.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ac.RWAbortableConsensusInit;
import se.kth.ict.id2203.ac.beans.ReadSetBean;
import se.kth.ict.id2203.ac.events.ACDecide;
import se.kth.ict.id2203.ac.events.ACPropose;
import se.kth.ict.id2203.ac.events.BEBACReadDeliver;
import se.kth.ict.id2203.ac.events.BEBACWriteDeliver;
import se.kth.ict.id2203.ac.events.NackPP2PDeliver;
import se.kth.ict.id2203.ac.events.ReadAckPP2PDeliver;
import se.kth.ict.id2203.ac.ports.AbortableConsensus;
import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebMessage;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class RWAbortableConsensus extends ComponentDefinition {
	Positive<PerfectPointToPointLink> pp2p = positive(PerfectPointToPointLink.class);
	Positive<BEBPort> beb = positive(BEBPort.class);
	Negative<AbortableConsensus> ac = negative(AbortableConsensus.class);

	private static final Logger logger = LoggerFactory.getLogger(RWAbortableConsensus.class);

	private Set<Address> neighborSet;
	private Address self;
	
	//RWAC specific
	private ArrayList<Integer> seenIds = new ArrayList<Integer>();
	int majority;
	private HashMap<Integer, String> tempValue = new HashMap<Integer, String>();
	private HashMap<Integer, String> val = new HashMap<Integer, String>();
	private HashMap<Integer, Integer> wAcks = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> rts = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> wts = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> tstamp = new HashMap<Integer, Integer>();
	private HashMap<Integer, ArrayList<ReadSetBean>> readSet = new HashMap<Integer, ArrayList<ReadSetBean>>();
	

	/**
	 * Instantiates a new application0.
	 */
	public RWAbortableConsensus() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleACPropose, ac);
		subscribe(handleBEBACReadDeliver, beb);
		subscribe(handleNackPP2PDeliver, pp2p);
		subscribe(handleReadAckPP2PDeliver, pp2p);
	}
	
	Handler<RWAbortableConsensusInit> handleInit = new Handler<RWAbortableConsensusInit>() {
		public void handle(RWAbortableConsensusInit event) {
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			
			seenIds = new ArrayList<Integer>();
			majority = neighborSet.size()/2+1;
			
			logger.debug("ac :: started");
		}
	};
	
	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};
	
	private void initInstance(int id){
		if(!seenIds.contains(id)){
			tempValue.put(id, "-1");
			val.put(id, "-1");
			wAcks.put(id, 0);
			rts.put(id, 0);
			wts.put(id, 0);
			tstamp.put(id, self.getId());
			readSet.put(id, new ArrayList<ReadSetBean>());
			seenIds.add(id);
		}
	}
	
	Handler<ACPropose> handleACPropose = new Handler<ACPropose>() {
		public void handle(ACPropose event) {
			int id = event.getId();
			String v = event.getValue();
			
			initInstance(id);
			tstamp.put(id, tstamp.get(id)+neighborSet.size());
			tempValue.put(id, v);
			
			BEBACReadDeliver bebd = new BEBACReadDeliver(id, tstamp.get(id), self);
			trigger(new BebBroadcast(new BebMessage(self, bebd), self), beb);
		}
	};
	
	Handler<BEBACReadDeliver> handleBEBACReadDeliver = new Handler<BEBACReadDeliver>() {
		public void handle(BEBACReadDeliver event) {
			int id = event.getId();
			int ts = event.getTs();
			
			initInstance(id);
			if(rts.get(id)>=ts || wts.get(id)>=ts){
				NackPP2PDeliver npp2pdeliver = new NackPP2PDeliver(self, id);
				trigger(new Pp2pSend(event.getSender(), npp2pdeliver), pp2p);
			} else {
				rts.put(id, ts);
				ReadAckPP2PDeliver rapp2pd = new ReadAckPP2PDeliver(self, id, wts.get(id), val.get(id), ts);
				trigger(new Pp2pSend(event.getSender(), rapp2pd), pp2p);
			}
		}
	};
	
	Handler<NackPP2PDeliver> handleNackPP2PDeliver = new Handler<NackPP2PDeliver>() {
		public void handle(NackPP2PDeliver event) {
			int id = event.getId();
			
			readSet.put(id, new ArrayList<ReadSetBean>());
			wAcks.put(id, 0);
			trigger(new ACDecide(id, "-1"), ac);
		}
	};
	
	Handler<ReadAckPP2PDeliver> handleReadAckPP2PDeliver = new Handler<ReadAckPP2PDeliver>() {
		public void handle(ReadAckPP2PDeliver event) {
			int sentts = event.getTs();
			int id = event.getId();
			int ts = event.getWts();
			String v = event.getVal();
			
			if(sentts==tstamp.get(id)){
				ReadSetBean rsb = new ReadSetBean(ts, v);
				readSet.get(id).add(rsb);
				if(readSet.get(id).size() == majority){
					ArrayList<ReadSetBean> beans = readSet.get(ts);
					ReadSetBean largestBean = findLargestReadSetBean(beans);
					if(!largestBean.getValue().equalsIgnoreCase("-1")){
						tempValue.put(id, largestBean.getValue());
					}
					//trigger
					BEBACWriteDeliver bebacWrite = new BEBACWriteDeliver(self, id, tstamp.get(id), tempValue.get(id));
					trigger(new BebBroadcast(new BebMessage(self, bebacWrite), self), beb);
				}
			}
		}
	};
	
	private ReadSetBean findLargestReadSetBean(ArrayList<ReadSetBean> list){
		ReadSetBean rsb = new ReadSetBean(0, "-1");
		
		for(ReadSetBean b :list){
			if(b.getTimestamp()> rsb.getTimestamp()){
				rsb = b;
			}
		}
		
		return rsb;
	}
}
