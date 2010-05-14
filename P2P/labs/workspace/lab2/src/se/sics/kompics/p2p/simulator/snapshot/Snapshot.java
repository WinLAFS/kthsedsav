package se.sics.kompics.p2p.simulator.snapshot;

import java.util.HashMap;

import se.sics.kompics.p2p.peer.PeerAddress;
import se.sics.kompics.p2p.simulator.launch.PeerType;

public class Snapshot {
	private static int counter = 0;
	private static HashMap<PeerAddress, PeerInfo> peers = new HashMap<PeerAddress, PeerInfo>();
	private static String FILENAME = "peer.out";

	static {
		FileIO.write("", FILENAME);
	}

//-------------------------------------------------------------------
	public static void addPeer(PeerAddress address, int numOfPieces, PeerType peerType) {
		peers.put(address, new PeerInfo(peerType, numOfPieces));
	}

//-------------------------------------------------------------------
	public static void removePeer(PeerAddress address) {
		peers.remove(address);
	}

//-------------------------------------------------------------------
	public static void addPiece(PeerAddress address, int piece) {
		PeerInfo peerInfo = peers.get(address);
		
		if (peerInfo == null)
			return;
		
		peerInfo.addPiece(piece);
	}

//-------------------------------------------------------------------
	public static void report() {
		String str = new String();
		str += "current time: " + counter++ + "\n";
		str += reportNetworkState();
//		str += reportDetailes();
		str += reportPeersStatus();
		str += "###\n";
		
		System.out.println(str);
		
		FileIO.append(str, FILENAME);
		
		if (isCompleted())
			System.exit(0);
	}

//-------------------------------------------------------------------
	private static String reportNetworkState() {
		return "total number of peers: " + peers.size() + "\n";
	}
	
//-------------------------------------------------------------------
	private static String reportDetailes() {
		PeerInfo peerInfo;
		String str = new String();

		for (PeerAddress peer : peers.keySet()) {
			peerInfo = peers.get(peer);
		
			str += "peer: " + peer;
			str += ", buffer: " + peerInfo.toString();
			str += "\n";
		}
		
		return str;
	}

//-------------------------------------------------------------------
	private static String reportPeersStatus() {
		PeerInfo peerInfo;
		String str = new String();
		int seeds = 0;
		int leechers = 0;

		for (PeerAddress peer : peers.keySet()) {
			peerInfo = peers.get(peer);
			
			if (peerInfo.isSeed())
				seeds++;
			else
				leechers++;
		}
		
		str += "seeds: " + seeds + "\n";
		str += "leechers: " + leechers + "\n";
		
		return str;
	}

//-------------------------------------------------------------------
	private static boolean isCompleted() {
		PeerInfo peerInfo;
		boolean result = false;
		int seeds = 0;

		for (PeerAddress peer : peers.keySet()) {
			peerInfo = peers.get(peer);
			
			if (peerInfo.isSeed())
				seeds++;
		}
		
		if (seeds > 1 && seeds == peers.size())
			result = true;
			
		return result;
	}

}
