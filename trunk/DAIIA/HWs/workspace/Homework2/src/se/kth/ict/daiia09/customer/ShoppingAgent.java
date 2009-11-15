package se.kth.ict.daiia09.customer;

import java.util.ArrayList;
import java.util.Iterator;

import sun.applet.AppletClassLoader;
import sun.font.EAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;

/**
 * The Shopping agent responsible for interacting with the Inventory/Monitoring
 * Agent and the Pricing Agents in order to check the availability and the 
 * price of a laptop product. Can be start with 2 parameters : 
 * 	-1st the name of the brand or the laptop wanted
 * 	-2ond the maximum price that the agent should look for
 */
public class ShoppingAgent extends Agent {
	//the List keeping the AID of pricing agents
	private ArrayList<Company> companies= new ArrayList<Company>();
	//the laptop brand that we want to buy
	private String laptopBrand = null;
	//the maximum price that we would accept
	int maxPrice = 15000;
	//the max number of days for shipment
	int maxDaysShipment = 7;
	//the price that the agent is currently negotiating
	int currentPrice;
	//the company that we ll buy the laptop from
	Company mySeller = null;
	int waitingFor = 0;
	boolean fistTime = true;
	
	/*
	 * (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	public void setup() {
		System.out.println("Shopping agent " + getAID().getName() + " started");
		
		//get our parameters from arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
				laptopBrand = (String) args[0];
				try {
					maxPrice = new Integer((String) args[1]);
				}
				catch (Exception e) {
				}
				try {
					maxDaysShipment = new Integer((String) args[2]);
				}
				catch (Exception e) {
				}
		}
		else {
			laptopBrand = "LG";
		}
		
		//we start our offers with the half of the price we are willing to pay
		currentPrice = maxPrice / 2;
		
		System.out.println("Trying to buy laptop of brand: " + laptopBrand);
		System.out.println("\t and max price: " + maxPrice);
		
		
		//search for the available service providers and add them
		//these are the initial service providers
		DFAgentDescription tempate = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("pricing");
		tempate.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, tempate);
			for (int i = 0; i < result.length; i++) {
				Company c = new Company();
				c.setPricingAgent(result[i].getName());
				System.out.println(":"+ getAID().getLocalName() + ": New service provider : " + c.toString());
				companies.add(c);
			}		
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
	}

	//a class for keeping the AID of the pricing and the inventory/monitoring agent of a company
	private class Company {
		AID pricingAgent = null;
		int availability = 0;
		int price = -1;
		
		
		public int getPrice() {
			return price;
		}
		public void setPrice(int price) {
			this.price = price;
		}
		public int getAvailability() {
			return availability;
		}
		public void setAvailability(int availability) {
			this.availability = availability;
		}
		public AID getPricingAgent() {
			return pricingAgent;
		}
		public void setPricingAgent(AID pricingAgent) {
			this.pricingAgent = pricingAgent;
		}
		public String toString() {
			return  pricingAgent.getLocalName();
		}
	}
}


