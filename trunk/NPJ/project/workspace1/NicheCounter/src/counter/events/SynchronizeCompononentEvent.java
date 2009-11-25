package counter.events;

import java.io.Serializable;

import dks.arch.Event;

public class SynchronizeCompononentEvent extends Event implements Serializable {

private static final long serialVersionUID = 1L;
	
	private int counterNumber;
	
	public SynchronizeCompononentEvent(int counterNumber){
		this.counterNumber = counterNumber;
	}

	public int getCounterNumber() {
		return counterNumber;
	}
}
