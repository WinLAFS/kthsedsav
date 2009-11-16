package dks.bcast.interfaces;

import java.math.BigInteger;
import java.util.ArrayList;

import dks.addr.DKSRef;

public interface PseudoReliableIntervalBroadcastAckInterface {

	public ArrayList<Object> getValues();
	
	public void setValues(ArrayList<Object> values);
	
//	public BigInteger getInstanceId();
//	
//	public void setInstanceId(BigInteger instanceID);
//	
//	public Boolean getAggregate();
//
//	public void setAggregate(Boolean aggregate);
//
//	public DKSRef getInitiator();
//
//	public void setInitiator(DKSRef initiator);
//	
//	public String getUniqueId();
	
}
