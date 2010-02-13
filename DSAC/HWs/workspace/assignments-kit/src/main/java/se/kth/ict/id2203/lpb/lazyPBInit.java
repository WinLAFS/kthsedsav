package se.kth.ict.id2203.lpb;

import java.util.Set;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public class lazyPBInit extends Init {
	private final String commandScript;

	private final Set<Address> neighborSet;

	private final Address self;
	
	private final double storeTreshold;
	
	private final int fanouts;
	
	private final int ttl;

	public lazyPBInit(String commandScript, Set<Address> neighborSet, Address self, double storeTreshold, int fanouts, int ttl) {
		super();
		this.storeTreshold = storeTreshold;
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
		this.fanouts = fanouts;
		this.ttl = ttl;
	}

	public int getFanouts() {
		return fanouts;
	}

	public int getTtl() {
		return ttl;
	}

	public String getCommandScript() {
		return commandScript;
	}

	public Set<Address> getNeighborSet() {
		return neighborSet;
	}

	public Address getSelf() {
		return self;
	}

	public double getStoreTreshold() {
		return storeTreshold;
	}

}
