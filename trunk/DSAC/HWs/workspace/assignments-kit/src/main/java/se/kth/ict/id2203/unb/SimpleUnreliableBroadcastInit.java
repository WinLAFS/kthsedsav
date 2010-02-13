package se.kth.ict.id2203.unb;

import java.util.Set;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public class SimpleUnreliableBroadcastInit extends Init {
	private final String commandScript;

	private final Set<Address> neighborSet;
	
	private final Address self;
	
	
	
	public SimpleUnreliableBroadcastInit(String commandScript, Set<Address> neighborSet, Address self) {
		super();
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
	}
	
	/**
	 * Gets the command script.
	 * 
	 * @return the command script
	 */
	public final String getCommandScript() {
		return commandScript;
	}
	
	/**
	 * Gets the neighbor set.
	 * 
	 * @return the neighbor set
	 */
	public final Set<Address> getNeighborSet() {
		return neighborSet;
	}
	
	/**
	 * Gets the self.
	 * 
	 * @return the self
	 */
	public final Address getSelf() {
		return self;
	}
}
