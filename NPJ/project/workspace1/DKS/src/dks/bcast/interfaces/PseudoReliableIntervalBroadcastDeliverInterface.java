package dks.bcast.interfaces;

import dks.bcast.IntervalBroadcastInfo;

public interface PseudoReliableIntervalBroadcastDeliverInterface {

	public IntervalBroadcastInfo getInfo();
	
	public void setInfo(IntervalBroadcastInfo info);
	
}
