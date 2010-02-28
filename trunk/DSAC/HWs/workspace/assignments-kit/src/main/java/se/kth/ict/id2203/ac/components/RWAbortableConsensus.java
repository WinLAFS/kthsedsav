package se.kth.ict.id2203.ac.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ac.RWAbortableConsensusInit;
import se.kth.ict.id2203.ac.beans.ReadSetBean;
import se.kth.ict.id2203.ac.ports.AbortableConsensus;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.pp2p.PerfectPointToPointLink;
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
//		subscribe(handlePp2pMessage, pp2p);
//		subscribe(handleUnreliabeBroadcast, beb);
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
}
