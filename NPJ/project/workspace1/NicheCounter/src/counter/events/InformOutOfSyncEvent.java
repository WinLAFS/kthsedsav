package counter.events;

import java.io.Serializable;

import counter.executors.CounterStateChangedExecutor;
import counter.managers.ConfigurationManager;

import dks.arch.Event;

/**
 * The event is sent by {@link ConfigurationManager} to
 * {@link CounterStateChangedExecutor} to tell executor that some node is not
 * synchronized.
 * 
 */
public class InformOutOfSyncEvent extends Event implements Serializable {

	private static final long serialVersionUID = 1L;

	private int counterNumber;
	private int roundId;

	/**
	 * Counter
	 * 
	 * @param counterNumber
	 *            Number Current maximum counter number
	 * @param roundId
	 *            Id of the round when max counter was set
	 */
	public InformOutOfSyncEvent(int counterNumber, int roundId) {
		this.counterNumber = counterNumber;
		this.roundId = roundId;
	}

	public int getLamport() {
		return roundId;
	}

	public void setLamport(int roundId) {
		this.roundId = roundId;
	}

	public int getCounterNumber() {
		return counterNumber;
	}
}
