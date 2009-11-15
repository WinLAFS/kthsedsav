package se.kth.ict.daiia09.customer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

public class ShoppingAgentNegotiate extends Agent {
	//the List keeping the AID of pricing agents
	private ArrayList<Company> companies = new ArrayList<Company>();
	private ArrayList<Company> competingCompanies = new ArrayList<Company>();
	//the laptop brand that we want to buy
	private String laptopBrand = null;
	//the maximum price that we would accept
	int maxPrice = 15000;
	//the price that the agent is currently negotiating
	int currentPrice;
	int iterations = 3;
	
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
					iterations = new Integer((String) args[2]);
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
				competingCompanies.add(c);
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
		
		addBehaviour(new ContractNetInitiatorIterations());
		
		ACLMessage start = new ACLMessage(ACLMessage.PROPAGATE);
		start.setProtocol("new-round");
		start.addReceiver(getAID());
		start.setContent(laptopBrand + ",0,null");
		send(start);
		
		
	}

	private class ContractNetInitiatorIterations extends Behaviour {
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol("new-round"),
				MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE) );
		
		public void action() {
			ACLMessage start = myAgent.receive(template);
			if (start != null) {
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Iteration " + iterations + " STARTING. -------------");
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				cfp.setContent(start.getContent());
				cfp.setProtocol("fipa-contract-net");
				Iterator<Company> cIterator = competingCompanies.iterator();
				while (cIterator.hasNext()) {
					Company company = (Company) cIterator.next();
					cfp.addReceiver(company.getPricingAgent());
				}
				myAgent.addBehaviour(new ShoppingContractNetInitiator(myAgent, cfp, maxPrice, (iterations == 1)));
				iterations--;
			}
			else {
				block();
			}
		}

		public boolean done() {
			return (iterations == 0);
		}
		
	}
	
	private class ShoppingContractNetInitiator extends ContractNetInitiator {
		String brand = null;
		int maxPrice = 0;
		boolean isLastIterateration;
		
		public ShoppingContractNetInitiator(Agent a, ACLMessage cfp, int maxPrice, boolean ili) {
			super(a, cfp);
			StringTokenizer st = new StringTokenizer(cfp.getContent(), ",");
			brand = st.nextToken();
			this.maxPrice = maxPrice;
			this.isLastIterateration = ili;
		}

		protected Vector prepareCfps(ACLMessage cfp) {
			System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] sending cfp messages. Protocol: " + cfp.getProtocol());
			return super.prepareCfps(cfp);
		}
		
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			int bestPrice = -1;
			ACLMessage bestOfferReply = null;
			String nextMsg = null;
			
			if (isLastIterateration) {
				for (int i = 0; i < responses.size(); i++) {
					ACLMessage response = (ACLMessage) responses.get(i);
					if (response.getPerformative() == ACLMessage.PROPOSE) {
						int price = -1;
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
					System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Accepting best offer: " + ((AID) bestOfferReply.getAllReceiver().next()).getLocalName() + " / " + bestPrice + " SEK");
					bestOfferReply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					bestOfferReply.setContent(brand + "," + bestPrice);
				}
				else {
					System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Cannot buy " + brand +". No available items found.");
				}
			}
			else {
				competingCompanies = new ArrayList<Company>();
				for (int i = 0; i < responses.size(); i++) {
					ACLMessage response = (ACLMessage) responses.get(i);
					if (response.getPerformative() == ACLMessage.PROPOSE) {
						int price = -1;
						try {
							price = Integer.parseInt(response.getContent());
						}
						catch (ArithmeticException e) {
						}
						if ((price != -1) && ((price < bestPrice) || bestPrice == -1)) {
							bestPrice = price;
							nextMsg = brand + "," + price + "," + response.getSender().getName();
						}
						Company c = new Company();
						c.setPricingAgent(response.getSender());
						competingCompanies.add(c);
					}
				}
				
				if (nextMsg != null) {
					ACLMessage start = new ACLMessage(ACLMessage.PROPAGATE);
					start.setProtocol("new-round");
					start.addReceiver(myAgent.getAID());
					start.setContent(nextMsg);
					myAgent.send(start);
					System.out.println("[LOG: " + myAgent.getAID().getLocalName() + "] Finished round. Current price: " + bestPrice);
				}
				else {
					System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Cannot buy " + brand +". No shopping agent continued.");
				}
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
