package se.kth.ict.daiia09.ontologies;

import jade.content.Concept;

public class Netbook implements Concept {

	private static final long serialVersionUID = -5913200902863724657L;

	public int price;
	public int weight;
	public int screenSize;

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getScreenSize() {
		return screenSize;
	}

	public void setScreenSize(int screenSize) {
		this.screenSize = screenSize;
	}
	
	
}