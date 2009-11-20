package counter.events;

import java.io.Serializable;

import dks.arch.Event;
import dks.niche.ids.ComponentId;


public class CounterChangedEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ComponentId cid;
	private int counterNumber;
	
	public CounterChangedEvent(ComponentId cid, int counterNumber){
		this.counterNumber = counterNumber;
		this.cid = cid;
	}

	public int getCounterNumber() {
		return counterNumber;
	}
	
	public ComponentId getCid() {
		return cid;
	}
	
//
//	public ComponentId getSource() {
//		return source;
//	}
}
