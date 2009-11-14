package se.kth.ict.daiia09.company;

import java.util.ArrayList;
import java.util.Iterator;

import com.sun.org.apache.bcel.internal.generic.RETURN;
import com.sun.xml.internal.ws.api.wsdl.parser.ServiceDescriptor;

import jade.core.Agent;
import jade.core.AgentDescriptor;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * the inventory/monitoring agent
 * keeps track of the availability of laptop items
 * and the shipping days that they need
 * 
 */
public class InventoryMonitoringAgent extends Agent {
	//the list keeping the info about the available laptops
	private ArrayList<LaptopBrand> laptopBrandArrayList = new ArrayList<LaptopBrand>();
	
	public void setup() {
		System.out.println("Inventory/monitoring agent " + getAID().getName() + " started.");

		//hard-coded data
		if (getAID().getLocalName().indexOf("c1") >= 0) {
			System.out.println("Loading data for company 1");
			laptopBrandArrayList.add(new LaptopBrand("LG", 12, 9));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 3, 5));
			laptopBrandArrayList.add(new LaptopBrand("Mac", 1, 15));
			laptopBrandArrayList.add(new LaptopBrand("Lenovo", 22, 3));	
		}
		else {
			System.out.println("Loading data for company 2");
			laptopBrandArrayList.add(new LaptopBrand("LG", 4, 7));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 4, 5));
			laptopBrandArrayList.add(new LaptopBrand("Dell", 1, 3));
			laptopBrandArrayList.add(new LaptopBrand("Siemens", 2, 15));			
		}
		
		//register the service as an "inventory-monitoring" service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sdAvailability = new ServiceDescription();
		sdAvailability.setType("im-availability");
		sdAvailability.setName(getLocalName());
		dfd.addServices(sdAvailability);
		ServiceDescription sdShippingDays = new ServiceDescription();
		sdShippingDays.setType("im-shippingDays");
		sdShippingDays.setName(getLocalName());
		dfd.addServices(sdShippingDays);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		//cyclic behaviour accepting requests for either availability
		//or shippment days of a product
		addBehaviour(new CyclicBehaviour(this) {
			String target = null;
			
			public void action() {
				
				//first check for availability request
				ACLMessage incomingMsg = myAgent.receive();
				if (incomingMsg != null) {
					target = incomingMsg.getContent();
		
					ACLMessage reply = incomingMsg.createReply();
					
					if (incomingMsg.getPerformative() == ACLMessage.QUERY_IF) {
						int availability = searchAvailability(target);
						reply.setPerformative(ACLMessage.INFORM_IF);
						reply.setContent(availability + "");
						reply.setLanguage(target);
						myAgent.send(reply);
					}
					//else if it is a shippment days request
					else if (incomingMsg.getPerformative() == ACLMessage.QUERY_REF) {
						int shippingDays = searchShippingDays(target);
						reply.setPerformative(ACLMessage.INFORM_REF);
						reply.setContent(shippingDays + "");
						myAgent.send(reply);
					}
				} 
				else {
					block();
				}
			}	
		});
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
	/**
	 * Search for the available number of items of the specified brand
	 * 
	 * @param brand the target brand
	 * @return the number of available items
	 */
	private int searchAvailability(String brand) {
		int availability = 0;
		
		Iterator<LaptopBrand> lbIterator= laptopBrandArrayList.iterator();
		//iterates through all items, and if the item of the specified
		//item is found, its availability is returned
		while (lbIterator.hasNext()) {
			LaptopBrand laptopBrand = lbIterator.next();
			if (laptopBrand.getName().equalsIgnoreCase(brand))
				return laptopBrand.getAvailableItems();
		}
		
		return availability;
	}
	
	/**
	 * Search for the shipping days for an item. If the item does not
	 * exist, it returns -1
	 * 
	 * @param brand the target brand
	 * @return the number of shipping days or -1
	 */
	private int searchShippingDays(String brand) {
		int availability = -1;
		
		Iterator<LaptopBrand> lbIterator= laptopBrandArrayList.iterator();
		//iterates through all items, and if the item of the specified
		//item is found, its availability is returned
		while (lbIterator.hasNext()) {
			LaptopBrand laptopBrand = lbIterator.next();
			if (laptopBrand.getName().equalsIgnoreCase(brand))
				return laptopBrand.getShippingDays();
		}
		
		return availability;
	}
	
	/*
	 * inner class LaptopBrand keeping the data regarding the laptops
	 */
	private class LaptopBrand {
		private String name = null; //brand name
		private int availableItems = 2; //availability
		private int shippingDays = 7; //shipment days
		
		public LaptopBrand(String name, int availableItems, int shippingDays) {
			super();
			this.name = name;
			this.availableItems = availableItems;
			this.shippingDays = shippingDays;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAvailableItems() {
			return availableItems;
		}
		public void setAvailableItems(int availableItems) {
			this.availableItems = availableItems;
		}
		public int getShippingDays() {
			return shippingDays;
		}
		public void setShippingDays(int shippingDays) {
			this.shippingDays = shippingDays;
		}
	}
}