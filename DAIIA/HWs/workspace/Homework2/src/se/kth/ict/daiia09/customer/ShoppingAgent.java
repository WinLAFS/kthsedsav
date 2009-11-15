package se.kth.ict.daiia09.customer;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

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
	//the price that the agent is currently negotiating
	int currentPrice;
	//the company that we ll buy the laptop from
	Company mySeller = null;
	int waitingFor = 0;
	
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
		}
		else {
			laptopBrand = "LG";
		}
		System.out.println("[LOG: Trying to buy laptop of brand: " + laptopBrand + ". Max Price : " + maxPrice);
		
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
		
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		cfp.setContent(laptopBrand);
		cfp.setProtocol("fipa-contract-net");
		Iterator<Company> cIterator = companies.iterator();
		while (cIterator.hasNext()) {
			Company company = (Company) cIterator.next();
			cfp.addReceiver(company.getPricingAgent());
		}
		
		addBehaviour(new ShoppingContractNetInitiator(this, cfp, maxPrice));
		
		
	}

	private class ShoppingContractNetInitiator extends ContractNetInitiator {
		String brand = null;
		int maxPrice = 0;
		
		public ShoppingContractNetInitiator(Agent a, ACLMessage cfp, int maxPrice) {
			super(a, cfp);
			brand = cfp.getContent();
			this.maxPrice = maxPrice;
		}

		protected Vector prepareCfps(ACLMessage cfp) {
			System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] sending cfp messages. Protocol: " + cfp.getProtocol());
			return super.prepareCfps(cfp);
		}
		
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			int bestPrice = -1;
			ACLMessage bestOfferReply = null;
			
			for (int i = 0; i < responses.size(); i++) {
				ACLMessage response = (ACLMessage) responses.get(i);
				if (response.getPerformative() == ACLMessage.PROPOSE) {
					int price = 0;
					try {
						price = Integer.parseInt(response.getContent());
					}
					catch (ArithmeticException e) {
					}
					ACLMessage replyToResponse = response.createReply();
					replyToResponse.setPerformative(ACLMessage.REJECT_PROPOSAL);
					replyToResponse.setContent(brand);
					if (price != 0 && price <= maxPrice && (bestOfferReply == null || price < bestPrice)) {
						bestOfferReply = replyToResponse;
						bestPrice = price;
					}
					acceptances.add(replyToResponse);
				}
			}
			
			if (bestOfferReply != null) {
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Accepting best offer: " + bestOfferReply.getSender().getLocalName() + " / " + bestPrice + " SEK");
				bestOfferReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
			}
		}

		protected void handleAllResultNotifications(Vector resultNotifications) {
			ACLMessage msg = (ACLMessage) resultNotifications.get(0);
			if (msg.getPerformative() == ACLMessage.INFORM) {
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Bought item: " + brand + " / " + msg.getSender().getLocalName() + " / " + msg.getContent() + " SEK");
			}
			else {
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Failed to buy item: " + brand + " / " + msg.getSender().getLocalName() + " / " + msg.getContent() + " SEK");
			}
		}
	}
	
	
	//a class for keeping the AID of the pricing and the inventory/monitoring agent of a company
	private class Company {
		AID pricingAgent = null;
		int price = -1;
		
		
		public int getPrice() {
			return price;
		}
		public void setPrice(int price) {
			this.price = price;
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


