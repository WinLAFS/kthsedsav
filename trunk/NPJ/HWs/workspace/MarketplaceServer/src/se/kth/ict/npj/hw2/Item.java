package se.kth.ict.npj.hw2;

import java.io.Serializable;

public class Item implements Serializable {
	String name = null;
	int price = 0;
	String owner = null;
	
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
}
