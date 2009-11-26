package counter.events;

import java.io.Serializable;

import counter.aggregators.ServiceSupervisor;
import counter.executors.CounterStateChangedExecutor;
import counter.managers.ConfigurationManager;
import dks.arch.Event;

/**
 * The event is used to make a notification that some components are not
 * synchronized. Is sent by {@link ServiceSupervisor} to
 * {@link ConfigurationManager} and from {@link ConfigurationManager} to
 * {@link CounterStateChangedExecutor}
 * 
 */
public class ComponentOutOfSyncEvent extends Event implements Serializable {

	private static final long serialVersionUID = 1L;

	private int counterNumber;
	private int roundId;

	/**
	 * Constructor
	 * 
	 * @param counterNumber
	 *            Maximum number of the counter
	 * @param roundId
	 *            Id of the round when counter number was updated
	 */
	public ComponentOutOfSyncEvent(int counterNumber, int roundId) {
		this.counterNumber = counterNumber;
		this.roundId = roundId;
	}

	public int getLamport() {
		return roundId;
	}

	public void setLamport(int lamport) {
		this.roundId = lamport;
	}

	public int getCounterNumber() {
		return counterNumber;
	}
}