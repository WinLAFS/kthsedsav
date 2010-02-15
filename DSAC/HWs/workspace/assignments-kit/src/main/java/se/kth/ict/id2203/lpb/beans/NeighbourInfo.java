package se.kth.ict.id2203.lpb.beans;

import java.util.HashSet;

public class NeighbourInfo {
	private HashSet<Integer> missing = new HashSet<Integer>();
	private int maxDelivered;
	
	public NeighbourInfo() {
		maxDelivered = 0;
	}
	
	public HashSet<Integer> getMissing() {
		return missing;
	}
	public void setMissing(HashSet<Integer> missing) {
		this.missing = missing;
	}
	public int getMaxDelivered() {
		return maxDelivered;
	}
	public void setMaxDelivered(int maxDelivered) {
		this.maxDelivered = maxDelivered;
	}
}
