package se.kth.ict.daiia09.ontologies;

import jade.content.Predicate;
import jade.core.AID;

public class Owns implements Predicate {
	public AID owner; 
	public Netbook netbook;
	
	public AID getOwner() {
		return owner;
	}
	public void setOwner(AID owner) {
		this.owner = owner;
	}
	public Netbook getNetbook() {
		return netbook;
	}
	public void setNetbook(Netbook netbook) {
		this.netbook = netbook;
	}
	
	
}
