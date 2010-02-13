package se.kth.ict.id2203.lpb;

import java.util.Set;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public class lazyPBInit extends Init {
	private final String commandScript;

	private final Set<Address> neighborSet;

	private final Address self;
	
	private final double storeTreshold;

	public lazyPBInit(String commandScript, Set<Address> neighborSet, Address self, double storeTreshold) {
		super();
		this.storeTreshold = storeTreshold;
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
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
