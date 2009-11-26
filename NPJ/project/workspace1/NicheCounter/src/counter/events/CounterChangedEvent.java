package counter.events;

import java.io.Serializable;

import counter.sensors.CounterStatusSensor;
import counter.watchers.CounterChangedWatcher;
import dks.arch.Event;
import dks.niche.ids.ComponentId;

/**
 * The event is by {@link CounterStatusSensor} to {@link CounterChangedWatcher}
 * to inform about change of the counter of a component
 * 
 */
public class CounterChangedEvent extends Event implements Serializable {

	private static final long serialVersionUID = 1L;

	private ComponentId cid;
	private int counterNumber;
	private int roundId;

	/**
	 * Constructor
	 * 
	 * @param cid
	 *            Id of the component that changed it's counter
	 * @param counterNumber
	 *            Number of the counter
	 * @param roundId
	 *            Id of the round
	 */
	public CounterChangedEvent(ComponentId cid, int counterNumber, int roundId) {
		this.counterNumber = counterNumber;
		this.cid = cid;
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

	public ComponentId getCid() {
		return cid;
	}

}
