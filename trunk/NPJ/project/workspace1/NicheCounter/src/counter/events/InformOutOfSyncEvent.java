package counter.events;

import java.io.Serializable;

import dks.arch.Event;

public class InformOutOfSyncEvent extends Event implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int counterNumber;
	private int lamport;
	
	public InformOutOfSyncEvent(int counterNumber, int lamport){
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
