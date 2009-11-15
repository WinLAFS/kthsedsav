package se.kth.ict.daiia09.company;

//se.kth.ict.daiia09.company.PricingAgent
import java.util.ArrayList;
import java.util.Iterator;


import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 *Pricing agent is responsible for calculating and updating the price of
 *the products. It communicates with it inventory/monitoring agent in order
 *to get data about the availability of the products.
 *<p> It also provides a service for receiving price requests for products
 *and responding with the price of the product.
 */
public class PricingAgent extends Agent {
	AID inventoryMonitoringAgent = null;
	ArrayList<LaptopBrand> laptopBrandArrayList = new ArrayList<LaptopBrand>();
	
	public void setup() {
		System.out.println("pricing agent " + getAID().getName() + " started.");
		
		//hard-coded item prices
		if (getAID().getLocalName().indexOf("c1") >= 0) {
			laptopBrandArrayList.add(new LaptopBrand("LG", 8000));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 11000));
			laptopBrandArrayList.add(new LaptopBrand("Mac", 13000));
			laptopBrandArrayList.add(new LaptopBrand("Lenovo", 8000));
		}
		else {
			laptopBrandArrayList.add(new LaptopBrand("LG", 9000));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 10000));
			laptopBrandArrayList.add(new LaptopBrand("Dell", 12000));
			laptopBrandArrayList.add(new LaptopBrand("Siemens", 10000));
		}
		
		
		//register the service as an "inventory-monitoring" service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("pricing");
		//the corresponding inventory/monitoring agent
		sd.setName(getAID().getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		//TODO
	}
	
	protected void takeDown() {
		//try to deregister the services when the agent goes down
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	private class LaptopBrand {
		private String name = null;
		private int price = Integer.MAX_VALUE;
		private int availability = 0;
		
		
		
		public int getAvailability() {
			return availability;
		}
		public void setAvailability(int availability) {
			this.availability = availability;
		}
		public LaptopBrand(String name) {
			this.name = name;
		}
		public LaptopBrand(String name, int price) {
			this.name = name;
			this.price = price;
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
}
