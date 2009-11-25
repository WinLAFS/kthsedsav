package counter.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.ComponentId;

public class ComponentOutOfSyncEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int counterNumber;
	
	public ComponentOutOfSyncEvent(int counterNumber){
		this.counterNumber = counterNumber;
	}

	public int getCounterNumber() {
		return counterNumber;
	}
}