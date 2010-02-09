package se.kth.ict.id2203.pfd;

import java.util.Set;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public class Application1Init extends Init {
	private final String commandScript;

	private final Set<Address> neighborSet;
	
	private final Address self;
	
	private final long delta;
	private final long gamma;
	
	
	/**
	 * Instantiates a new application0 init.
	 * 
	 * @param commandScript
	 *            the command script
	 * @param neighborSet
	 *            the neighbor set
	 * @param self
	 *            the self
	 */
	public Application1Init(String commandScript, Set<Address> neighborSet, Address self, long gamma, long delta) {
		super();
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
		this.delta = delta;
		this.gamma = gamma;
	}
	
	public Application1Init(String commandScript, Set<Address> neighborSet, Address self) {
		super();
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
		delta = 0;
		gamma = 0;
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

	public long getDelta() {
		return delta;
	}

	public long getGamma() {
		return gamma;
	}
	
	
}
