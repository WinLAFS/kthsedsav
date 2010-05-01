package se.sics.kompics.p2p.simulator.launch;

import java.math.BigInteger;

import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;
import se.sics.kompics.p2p.simulator.PeerFail;
import se.sics.kompics.p2p.simulator.PeerJoin;
import se.sics.kompics.p2p.simulator.PeerLookup;

@SuppressWarnings("serial")
public class Operations {

//-------------------------------------------------------------------
	static Operation1<PeerJoin, BigInteger> peerJoin = new Operation1<PeerJoin, BigInteger>() {
			public PeerJoin generate(BigInteger id) {
				return new PeerJoin(id);
			}
		};
	
//-------------------------------------------------------------------
	static Operation1<PeerFail, BigInteger> peerFail = new Operation1<PeerFail, BigInteger>() {
		public PeerFail generate(BigInteger id) {
			return new PeerFail(id);
		}
	};

//-------------------------------------------------------------------
	static Operation1<PeerLookup, BigInteger> peerLookup = new Operation1<PeerLookup, BigInteger>() {
		public PeerLookup generate(BigInteger id) {
			return new PeerLookup(id);
		}
	};

}
