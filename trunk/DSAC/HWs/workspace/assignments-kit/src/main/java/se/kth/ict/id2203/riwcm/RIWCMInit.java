package se.kth.ict.id2203.riwcm;

import java.util.Set;

import se.sics.kompics.Init;
import se.sics.kompics.address.Address;

public class RIWCMInit extends Init {
	private final String commandScript;
	private final Set<Address> neighborSet;
	private final Address self;
	private final int numberOfRegister;
	private final int majoritySize;
	
	
	
	public int getNumberOfRegister() {
		return numberOfRegister;
	}

	public RIWCMInit(String commandScript, Set<Address> neighborSet, Address self) {
		super();
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
		numberOfRegister = 1;
		this.majoritySize =  (int) (Math.floor((neighborSet.size() + 1) / 2) + 1);
	}
	
	public RIWCMInit(String commandScript, Set<Address> neighborSet,
			Address self, int numberOfRegister) {
		super();
		this.commandScript = commandScript;
		this.neighborSet = neighborSet;
		this.self = self;
		this.numberOfRegister = numberOfRegister;
		this.majoritySize =  (int) (Math.floor((neighborSet.size() + 1) / 2) + 1);
	}

	public int getMajoritySize() {
		return majoritySize;
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
