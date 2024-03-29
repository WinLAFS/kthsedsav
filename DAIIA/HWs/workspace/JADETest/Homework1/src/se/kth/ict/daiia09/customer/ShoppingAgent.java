package se.kth.ict.daiia09.customer;

import java.util.ArrayList;
import java.util.Iterator;

import se.kth.ict.daiia09.company.InventoryMonitoringAgent;
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
		
		/*
		//hard-coding the AID of the agents of the 2 companies 
		Company company1 = new Company();
		company1.setPricingAgent(new AID("c1PA", AID.ISLOCALNAME));
		company1.setInventoryMonitoringAgent(new AID("c1IMA", AID.ISLOCALNAME));
		Company company2 = new Company();
		company2.setPricingAgent(new AID("c2PA", AID.ISLOCALNAME));
		company2.setInventoryMonitoringAgent(new AID("c2IMA", AID.ISLOCALNAME));
		companies.add(company1);
		companies.add(company2);*/
		
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
				ServiceDescription sed = (ServiceDescription) result[i].getAllServices().next();
				c.setInventoryMonitoringAgent(new AID(sed.getName(), AID.ISLOCALNAME));
				System.out.println(":"+ getAID().getLocalName() + ": New service provider : " + c.toString());
				companies.add(c);
			}		
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		//subscribe to the DF to get notified about additions or removals of service providers
		AID df = getDefaultDF();
		ACLMessage subs = DFService.createSubscriptionMessage(this, df, tempate, null);
		Behaviour monitoringPricingAgents = new SubscriptionInitiator(this, subs) {
			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfd = DFService.decodeNotification(inform.getContent());
					if (fistTime) {
						fistTime = false;
					}
					else {
						for (int i = 0; i < dfd.length; i++) {
							boolean newCompany = dfd[i].getAllServices().hasNext();
							if (newCompany) {
								Company c = new Company();
								c.setPricingAgent(dfd[i].getName());
								ServiceDescription sed = (ServiceDescription) dfd[i].getAllServices().next();
								c.setInventoryMonitoringAgent(new AID(sed.getName(), AID.ISLOCALNAME));
								System.out.println(":"+ getAID().getLocalName() + ": New service provider : " + c.toString());
								companies.add(c);
								
								//get the availability of the newly added company
								addBehaviour(new getThisAvailability(c));
							}
							else {
								//check if the specified service provider already exists. That would
								//mean that the service provider has be removed from the DFService
								Iterator<Company> cIterator = companies.iterator();
								while (cIterator.hasNext()) {
									Company company = cIterator.next();
									if (dfd[i].getName().equals(company.getPricingAgent())) {
										companies.remove(company);
										System.out.println(":"+ getAID().getLocalName() + ": Stopped providing service : " + company.toString());
										newCompany = false;
										break;
									}
								}
							}
						}		
					}
				} 
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		};
		
		addBehaviour(monitoringPricingAgents);
		/*
		 * the behaviour of the ShoppingAgent is an FSMBehaviour. Control flow:
		 * 1> check availability of the product (availability)
		 * 2> receiving the availabilities (receiveAvailability)
		 * 3a> availability == 0: inform user that no availability (noAvailableItems)
		 * 3b> availability > 0: go to price requests in order to send requests to the pricing agents
		 * 		about the price of the product (priceRequests)
		 * 4b> receive responses from the pricing agents (priceReceive)
		 * 5ba> if item found with accepted price : inform user that item found (foundItem)
		 * 5bb> else if not reached max price : increase price and goto 2b (priceRequests)
		 * 5bc> else (reached max price) : inform user that no item found within the price limits (notFoundItem)
		 */
		
		FSMBehaviour shoppingAgentBehaviour = new FSMBehaviour(this);
		shoppingAgentBehaviour.registerFirstState(new CheckAvailabilityBehaviour(laptopBrand), "availability");
		shoppingAgentBehaviour.registerState(new ReceiveAvailability(), "receiveAvailability");
		shoppingAgentBehaviour.registerTransition("availability", "receiveAvailability", 1);
		shoppingAgentBehaviour.registerLastState(new OneShotBehaviour(this) {
			public void action() {
				System.out.println(":" + getAID().getLocalName() + ":Shopping agent: NO AVAILABLE LAPTOPS OF BRAND " + laptopBrand + " FOUND");
			}
			
		}, "noAvailableItems");
		shoppingAgentBehaviour.registerTransition("receiveAvailability", "noAvailableItems", 0);
		shoppingAgentBehaviour.registerDefaultTransition("receiveAvailability", "priceRequests");
		shoppingAgentBehaviour.registerState(new sendPriceRequestsBehaviour(this, 4000), "priceRequests");
		shoppingAgentBehaviour.registerState(new receivePricesBehaviour(), "priceReceive");
		shoppingAgentBehaviour.registerTransition("priceRequests", "priceReceive", 1);
		shoppingAgentBehaviour.registerLastState(new OneShotBehaviour() {
			
			public void action() {
				System.out.println(":" + getAID().getLocalName() + ": Found from pricing agent: " + mySeller.getPricingAgent().getName());
				System.out.println(":" + getAID().getLocalName() + ": PRICE: " + mySeller.getPrice() + " SEK");
			}
			
		}, "foundItem");
		shoppingAgentBehaviour.registerLastState(new OneShotBehaviour() {
			
			public void action() {
				System.out.println(":" + getAID().getLocalName() + ": COULD NOT FIND ITEM WITH PRICE LOWER THAN " + maxPrice + " SEK");
			}
			
		}, "notFoundItem");
		
		shoppingAgentBehaviour.registerTransition("priceReceive", "priceRequests", 0, new String[] {"priceRequests", "priceReceive"});
		shoppingAgentBehaviour.registerTransition("priceReceive", "foundItem", 1);
		shoppingAgentBehaviour.registerTransition("priceReceive", "notFoundItem", 2);
		addBehaviour(shoppingAgentBehaviour);
	}
	
	/**
	 * 
	 * Gets the availability and updates the value of a specific company
	 * that if specified in the constructor
	 *
	 */
	private class getThisAvailability extends OneShotBehaviour {
		Company c;
		getThisAvailability(Company c) {
			super();
			this.c = c;
		}
		
		public void action() {
			//send an availability request to i/m agent
			ACLMessage availabilityQuery = new ACLMessage(ACLMessage.QUERY_IF);
			availabilityQuery.addReceiver(c.getInventoryMonitoringAgent());
			availabilityQuery.setContent(laptopBrand);
			myAgent.send(availabilityQuery);
			
		
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
			//do a blocking receive for the response that will include the availability
			//for this company
			ACLMessage incomingMsg = myAgent.blockingReceive(mt);
			if (incomingMsg != null) {
				AID senderIMA = incomingMsg.getSender();
				int availability = new Integer(incomingMsg.getContent());
				c.setAvailability(availability);
			}
			else {
				block();
			}
		}
	}
	
	private class receivePricesBehaviour extends Behaviour {
		//accept only inform messages
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
		int received = 0;
		
		//receiving the responses for the price from all the pricing agents
		//and updating the data
		public void action() {
			ACLMessage incomingMsg = myAgent.receive(mt);
			if (incomingMsg != null) {
				received++;
				AID senderPA = incomingMsg.getSender();
				int price = new Integer(incomingMsg.getContent());
				
				Iterator<Company> cIterator = companies.iterator();
				while (cIterator.hasNext()) {
					Company company = cIterator.next();
					if (company.getPricingAgent().equals(senderPA)) {
						company.setPrice(price);
					}
				}
			}
			else {
				block();
			}
		}

		public boolean done() {
			//done when received as many prices responses as requests sent
			return (received == waitingFor);
		}
		
		public int onEnd() {
			//result = 0 means, not found and continue with increased price
			int result = 0;
			
			//result = 1 means, that we have found a buyer with a suitable price
			Iterator<Company> cIterator = companies.iterator();
			while (cIterator.hasNext()) {
				Company company = cIterator.next();
				int companyPrice = company.getPrice();
				//if new lower price found
				if (companyPrice > 0 && companyPrice <= currentPrice) {
					currentPrice = companyPrice;
					mySeller = company;
					result = 1;
				}
			}
			
			//result = 2 means that we have reached the upper limit of price
			if (result != 1) {
				currentPrice += (maxPrice / 10);
				result = (currentPrice > maxPrice) ? 2 : result;
			}
			
			waitingFor = 0;
			received = 0;
			
			return result;
		}
	}
	
	//sending the requests for the prices to the pricing agents
	private class sendPriceRequestsBehaviour extends WakerBehaviour {
		
		public sendPriceRequestsBehaviour(Agent a, long timeout) {
			super(a, timeout);
		}

		//after timeout, send pricing request to all pricing agents that correspond to companies
		//with availability of the product > 0
		public void onWake() {
			System.out.println(":" + getAID().getLocalName() + ": Trying to buy with max price " + currentPrice + " SEK");
			ACLMessage request = new ACLMessage(ACLMessage.INFORM);
			request.setContent(laptopBrand);
			Iterator<Company> cIterator = companies.iterator();
			while (cIterator.hasNext()) {
				Company company = cIterator.next();
				if (company.getAvailability() > 0) {
					waitingFor++;
					request.addReceiver(company.getPricingAgent());
				}
			}
			myAgent.send(request);
		}				
		
		public int onEnd() {
			return 1;
		}
	}
	
//	//a wake behaviour responsible for getting the price of the item and deciding to
//	//buy or not
//	private class FindPricesBehaviour extends WakerBehaviour {
//		
//		public FindPricesBehaviour(Agent a, long timeout) {
//			super(a, timeout);
//		}
//		
//		public void onWake() {
//			System.out.println(":" + getAID().getLocalName() + ": Trying to buy with price: " + currentPrice + " SEK");
//		}
//		
//		public int onEnd() {
//			return 1;
//		}
//	}
	
	//receive availability details from inventory/monitoring agents behaviour
	private class ReceiveAvailability extends Behaviour {
		//accept only INFORM_IF messages
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
		int received = 0;
		int maxAvailability = 0;
		
		public void action() {
			//receive the availabilities from the inventory/monitoring agent and store them
			//in the Company item in order to use them later to check the prices
			ACLMessage incomingMsg = myAgent.receive(mt);
			if (incomingMsg != null) {
				received++;
				AID senderIMA = incomingMsg.getSender();
				int availability = new Integer(incomingMsg.getContent());
				maxAvailability = (availability > maxAvailability) ? availability : maxAvailability;
				
				Iterator<Company> cIterator = companies.iterator();
				while (cIterator.hasNext()) {
					Company company = cIterator.next();
					if (company.getInventoryMonitoringAgent().equals(senderIMA))
						company.setAvailability(availability);
				}
			}
			else {
				block();
			}
		}
		
		public boolean done() {
			//we expect as many availability responses as the companies are
			return (received == companies.size());
		}
		
		public int onEnd() {
			//if maxAvaialability is still 0, the transition will be that the item was not found
			return maxAvailability;
		}
	}
	
	//send requsts to the i / m agents about the availability of the product
	private class CheckAvailabilityBehaviour extends OneShotBehaviour {
		private String laptopBrand = null;
		
		public CheckAvailabilityBehaviour(String laptopBrand) {
			super();
			this.laptopBrand = laptopBrand;
		}
		
		//ask inventory/monitoring agents for the availability of a product
		public void action() {
			//create a message, add as receivers all the inv/mon agents
			//and as content the laptop brand
			ACLMessage availabilityQuery = new ACLMessage(ACLMessage.QUERY_IF);
			Iterator<Company> companiesIterator = ShoppingAgent.this.companies.iterator();
			while (companiesIterator.hasNext()) {
				availabilityQuery.addReceiver(companiesIterator.next().getInventoryMonitoringAgent());
			}
			availabilityQuery.setContent(laptopBrand);
			myAgent.send(availabilityQuery);
		}
		
		public int onEnd() {
			return 1;
		}

	}

	//a class for keeping the AID of the pricing and the inventory/monitoring agent of a company
	private class Company {
		AID pricingAgent = null;
		AID inventoryMonitoringAgent = null;
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
		public AID getInventoryMonitoringAgent() {
			return inventoryMonitoringAgent;
		}
		public void setInventoryMonitoringAgent(AID inventoryMonitoringAgent) {
			this.inventoryMonitoringAgent = inventoryMonitoringAgent;
		}
		
		public String toString() {
			return inventoryMonitoringAgent.getLocalName() + " / " + pricingAgent.getLocalName();
		}
	}
}


