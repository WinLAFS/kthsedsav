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
	private int lamport;
	
	public ComponentOutOfSyncEvent(int counterNumber, int lamport){
		this.counterNumber = counterNumber;
		this.lamport = lamport;
	}
	
	

	public int getLamport() {
		return lamport;
	}



	public void setLamport(int lamport) {
		this.lamport = lamport;
	}



	public int getCounterNumber() {
		return counterNumber;
	}
}