package counter.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.ComponentId;


public class CounterChangedEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
//	private ComponentId source;
	private int counterNumber;
	
	public CounterChangedEvent( int counterNumber){
		this.counterNumber = counterNumber;
	}

	public int getCounterNumber() {
		return counterNumber;
	}
//
//	public ComponentId getSource() {
//		return source;
//	}
}
