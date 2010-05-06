package se.sics.kompics.p2p.peer;

import se.sics.kompics.PortType;
import se.sics.kompics.p2p.peer.btpeer.JoinBTPeer;
import se.sics.kompics.p2p.peer.tracker.JoinTracker;

public class PeerPort extends PortType {{
	negative(JoinBTPeer.class);
	negative(JoinTracker.class);
}}
