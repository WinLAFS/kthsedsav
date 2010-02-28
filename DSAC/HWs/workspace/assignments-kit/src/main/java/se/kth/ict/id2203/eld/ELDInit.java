package se.kth.ict.id2203.eld;

import java.util.Set;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public class ELDInit extends Init {
	private final String commandScript;

	private final Set<Address> neighborSet;
	
	private final Address self;
	private final int delta;
	private final int timeDelay;
	

	public ELDInit(String commandScript, Set<Address> neighborSet,
			Address self, int delta, int timeDelay) {
		super();
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
		this.delta = delta;
		this.timeDelay = timeDelay;
	}

	public int getDelta() {
		return delta;
	}
	
	public int getTimeDelay() {
		return timeDelay;
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
