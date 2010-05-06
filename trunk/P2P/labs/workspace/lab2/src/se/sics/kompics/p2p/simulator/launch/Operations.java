package se.sics.kompics.p2p.simulator.launch;

import java.math.BigInteger;

import se.sics.kompics.p2p.experiment.dsl.adaptor.Operation1;
import se.sics.kompics.p2p.simulator.BTPeerFail;
import se.sics.kompics.p2p.simulator.BTPeerJoin;
import se.sics.kompics.p2p.simulator.TrackerJoin;

@SuppressWarnings("serial")
public class Operations {

//-------------------------------------------------------------------
	static Operation1<BTPeerJoin, BigInteger> peerJoin(final PeerType peerType) {
		return new Operation1<BTPeerJoin, BigInteger>() {
			public BTPeerJoin generate(BigInteger id) {
				return new BTPeerJoin(id, peerType);
			}
		};
	}

//-------------------------------------------------------------------
	static Operation1<TrackerJoin, BigInteger> trackerJoin = new Operation1<TrackerJoin, BigInteger>() {
		public TrackerJoin generate(BigInteger id) {
			return new TrackerJoin(id);
		}
	};
	
//-------------------------------------------------------------------
	static Operation1<BTPeerFail, BigInteger> peerFail = new Operation1<BTPeerFail, BigInteger>() {
		public BTPeerFail generate(BigInteger id) {
			return new BTPeerFail(id);
		}
	};
}
