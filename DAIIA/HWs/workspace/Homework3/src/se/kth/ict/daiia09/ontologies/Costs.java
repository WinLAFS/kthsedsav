package se.kth.ict.daiia09.ontologies;

import jade.content.Predicate;


public class Costs implements Predicate {
	
	private Netbook item;
	private int price;
	public Netbook getItem() {
		return item;
	}
	public void setItem(Netbook item) {
		this.item = item;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	
	
}
