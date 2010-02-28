package se.kth.ict.id2203.uc.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.ac.events.ACDecide;
import se.kth.ict.id2203.ac.events.ACPropose;
import se.kth.ict.id2203.ac.ports.AbortableConsensus;
import se.kth.ict.id2203.beb.events.BebBroadcast;
import se.kth.ict.id2203.beb.events.BebDecidedDeliver;
import se.kth.ict.id2203.beb.events.BebMessage;
import se.kth.ict.id2203.beb.ports.BEBPort;
import se.kth.ict.id2203.eld.events.Trust;
import se.kth.ict.id2203.eld.ports.ELD;
import se.kth.ict.id2203.pfd.events.CrashEvent;
import se.kth.ict.id2203.riwc.RIWCInit;
import se.kth.ict.id2203.riwc.events.ReadRequest;
import se.kth.ict.id2203.riwc.events.WriteMessage;
import se.kth.ict.id2203.uc.UCInit;
import se.kth.ict.id2203.uc.events.UCDecide;
import se.kth.ict.id2203.uc.events.UCPropose;
import se.kth.ict.id2203.uc.ports.UniformConsensus;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;

public class PaxosUniformConsensus extends ComponentDefinition {
	Positive<BEBPort> beb = positive(BEBPort.class);
	Positive<ELD> eld = positive(ELD.class);
	Positive<AbortableConsensus> ac = positive(AbortableConsensus.class);
	Negative<UniformConsensus> uc = negative(UniformConsensus.class);

	private static final Logger logger = LoggerFactory.getLogger(PaxosUniformConsensus.class);

	private Set<Address> neighborSet;
	private Set<Integer> seenIds;
	private Address self;
	private boolean leader;
	private HashMap<Integer, String> proposal;
	private HashMap<Integer, Boolean> proposed;
	private HashMap<Integer, Boolean> decided;
	
	public PaxosUniformConsensus() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleELDTrust, eld);
		subscribe(handleBebMessage, beb);
		subscribe(handleACReturn, ac); //TODO
		subscribe(handleUCPropose, uc); //TODO
	}

	Handler<UCInit> handleInit = new Handler<UCInit>() {
		public void handle(UCInit event) {
			neighborSet = event.getNeighborSet();
			self = event.getSelf();
			proposal = new HashMap<Integer, String>();
			proposed = new HashMap<Integer, Boolean>();
			decided = new HashMap<Integer, Boolean>();
			/*
			 * upon event ⟨ Init ⟩ do seenIds := ∅; leader := false; 4: end
			 * event
			 */
			seenIds = new HashSet<Integer>();
			leader = false;
			
			logger.debug("UC :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};
	
	Handler<Trust> handleELDTrust = new Handler<Trust>() {
		public void handle(Trust event) {
			/*
			 * upon event ⟨ trust | pi ⟩ do 
			 * 	if (pi = self) then 
			 * 		leader := true;
			 * 		for all id ∈ seenIds do 
			 * 			tryPropose(id); 
			 * 		end for 
			 * 	else 
			 * 		leader := false; 
			 * 	end if 
			 * end event
			 */
			if (event.getLeader().equals(self)) {
				leader = true;
				for (int id : seenIds) {
					tryPropose(id);
				}
			}
			else {
				leader = false;
			}
		}
	};
	
	Handler<BebDecidedDeliver> handleBebMessage = new Handler<BebDecidedDeliver>() {
		public void handle(BebDecidedDeliver event) {
			/* upon event ⟨ bebDeliver | pi , [Decided, id, v] ⟩ do
			 * 	initInstance(id); 
			 * 	if (decided[id] = false) then 
			 * 		decided[id] := true; 
			 * 		trigger ⟨ ucDecide | id, v ⟩; 
			 * 	end if 
			 * end event
			 */
			
			initInstance(event.getId());
			if (decided.get(event.getId()) == false) {
				decided.put(event.getId(), true);
				trigger(new UCDecide(event.getId(), event.getValue()), uc);
			}
		}
	};
	
	Handler<ACDecide> handleACReturn = new Handler<ACDecide>() {
		public void handle(ACDecide event) {
			/* upon event ⟨ acReturn | id, result ⟩ do 
			 * 	if (result ̸= ⊥) then
			 * 		trigger ⟨ bebBroadcast | [Decided, id, result] ⟩; 
			 * 	else
			 * 		proposed[id]:= false; 
			 * 		tryPropose(id); 
			 * 	end if 
			 * end event
			 */
			BebDecidedDeliver bebDecidedDeliver = new BebDecidedDeliver(self, event.getId(), event.getValue());
			if (!event.getValue().equals("-1")) {
				trigger(new BebBroadcast(new BebMessage(self, bebDecidedDeliver) , self), beb);
			}
			else {
				proposed.put(event.getId(), false);
				tryPropose(event.getId());
			}
		}
	};
	
	Handler<UCPropose> handleUCPropose = new Handler<UCPropose>() {
		public void handle(UCPropose event) {
			/*
			 * upon event ⟨ ucPropose | id, v ⟩ do 
			 * 		initInstance(id);
			 * 		proposal[id] := v; 
			 * 		tryPropose(id); 
			 * end event
			 */
			initInstance(event.getId());
			proposal.put(event.getId(), event.getValue());
			tryPropose(event.getId());
		}
	};
	
	
	private void initInstance(int id) {
		 /* procedure initInstance(id) is 
		 * 	if (not(id ∈ seenIds)) then 
		 * 		proposal[id] := ⊥; 
		 * 		proposed[id] := decided[id] := false; 
		 * 		seenIds := seenIds ∪ {id};
		 * 	end if 
		 * end procedure
		 */
		if (!seenIds.contains(id)) {
			proposal.put(id, "-1");
			proposed.put(id, false);
			decided.put(id, false);
			seenIds.add(id);
		}
	}
	
	private void tryPropose(int id) {
		 /* procedure tryPropose(id) is 
		  * 	if (leader = true ∧ proposed[id] = false ∧ proposal[id] ̸= ⊥) then 
		  * 		proposed[id] := true; 
		  * 		trigger ⟨ acPropose | id, proposal[id] ⟩; 
		  * 	end if 
		  * end procedure
		 */
		if ((leader == true) && (proposed.get(id) == false) && (!proposal.get(id).equals("-1"))) {
			proposed.put(id, true);
			trigger(new ACPropose(id, proposal.get(id)), ac);
		}
	}
	
}
