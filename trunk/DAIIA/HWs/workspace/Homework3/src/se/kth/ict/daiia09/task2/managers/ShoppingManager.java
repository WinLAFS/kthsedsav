package se.kth.ict.daiia09.task2.managers;

import jade.core.AID;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.util.leap.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import se.kth.ict.daiia09.task2.Constants;

public class ShoppingManager extends Agent {
	public static final int INITILIAZE1 = 0;
	public static final int INITILIAZE2 = 1;
	public static final int CLONED = 2;
	public static final int RUNNING_PROTOCOL = 3;
	public static final int RETURNED_TO_MAIN = 4;	
	public static final int WAITING_FOR_CLONES = 5;
	public static final int DIE = 6;
	
	private int _state = INITILIAZE1;
	
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
	int bestPriceFound = 0;

	String mainContainer = null;
	AID mainManager = null;
	String sellerDetails = "";
	
	protected void setup() {
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
		
		//search for the available service providers and add them
		//these are the initial service providers
		DFAgentDescription tempate = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("contractor");
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
		
		mainContainer = here().getName();
		mainManager = this.getAID();
		
		System.out.println("[LOG:" + getAID().getLocalName() + "] Trying to buy laptop of brand: " + laptopBrand + ". Max Price : " + maxPrice);
		
		
		addBehaviour(new CyclicBehaviour() {

			public void action() {
				switch(_state){
					case INITILIAZE1:
						ContainerID where1 = new ContainerID();
						where1.setName(Constants.EBAY_CONTAINER);
						doClone(where1, Constants.MANAGER_EBAY); 
						System.out.println("[LOG:" + getAID().getLocalName() + "] Cloning : " + Constants.MANAGER_EBAY + " to container: " + Constants.EBAY_CONTAINER);
						_state = INITILIAZE2;
						break;
					case INITILIAZE2:
						ContainerID where2 = new ContainerID();
						where2.setName(Constants.AMAZON_CONTAINER);
						doClone(where2, Constants.MANAGER_AMAZON); 
						System.out.println("[LOG:" + getAID().getLocalName() + "] Cloning : " + Constants.MANAGER_AMAZON + " to container: " + Constants.AMAZON_CONTAINER);
						_state = WAITING_FOR_CLONES;
						break;
					case CLONED:
						String prefix = (getAID().getLocalName().equals(Constants.MANAGER_EBAY)) ? "ebay" : "amazon";
						Iterator<Company> cIterator = companies.iterator();
						ArrayList<Company> myCompanies = new ArrayList<Company>();
						while (cIterator.hasNext()) {
							ShoppingManager.Company company = (ShoppingManager.Company) cIterator.next();
							if (company.getPricingAgent().getLocalName().toLowerCase().indexOf(prefix.toLowerCase()) > 0) {
								myCompanies.add(company);
							}
						}
						companies = myCompanies;
						
						ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
						cfp.setContent(laptopBrand);
						cfp.setProtocol("fipa-contract-net");
						Iterator<Company> cIterator2 = companies.iterator();
						while (cIterator2.hasNext()) {
							Company company = (Company) cIterator2.next();
							cfp.addReceiver(company.getPricingAgent());
						}
						
						addBehaviour(new ContractNetInitiatorIterations());
						
						ACLMessage start = new ACLMessage(ACLMessage.PROPAGATE);
						start.setProtocol("new-round");
						start.addReceiver(getAID());
						start.setContent(laptopBrand + ",0,null");
						send(start);
						
						_state = RUNNING_PROTOCOL;
						break;
					case RUNNING_PROTOCOL:
						MessageTemplate template = MessageTemplate.and(
								MessageTemplate.MatchProtocol("finished-contract-net"),
								MessageTemplate.MatchPerformative(ACLMessage.INFORM) );
						ACLMessage finish = myAgent.receive(template);
						if (finish != null) {
							System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Finished the protocol. Going back!");
							ContainerID mainContainerID = new ContainerID();
							mainContainerID.setName(mainContainer);
							doMove(mainContainerID);
							_state = RETURNED_TO_MAIN;
						}
						else {
							block();
						}
						break;
					case RETURNED_TO_MAIN:
						ACLMessage result = new ACLMessage(ACLMessage.INFORM);
						result.setProtocol("gone-and-back");
						result.setContent(bestPriceFound + "," + sellerDetails);
						result.addReceiver(mainManager);
						myAgent.send(result);
						System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Informed main manager! " + bestPriceFound + " / " + sellerDetails);
						_state = DIE;
						break;
					case WAITING_FOR_CLONES:
						MessageTemplate template2 = MessageTemplate.and(
								MessageTemplate.MatchProtocol("gone-and-back"),
								MessageTemplate.MatchPerformative(ACLMessage.INFORM) );
						ACLMessage finish2 = myAgent.blockingReceive(template2);
						ACLMessage finish3 = myAgent.blockingReceive(template2);
						StringTokenizer st1 = new StringTokenizer(finish2.getContent(), ",");
						StringTokenizer st2 = new StringTokenizer(finish3.getContent(), ",");
						int price1 = new Integer(st1.nextToken());
						String whereIsIt1 = (price1 == 0) ? "" : st1.nextToken();
						int price2 = new Integer(st2.nextToken());
						String whereIsIt2 = (price2 == 0) ? "" : st2.nextToken();
						
						if (price1 == 0 && price2 == 0) {
							System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] NO ITEM FOUND --");
						}
						else {
							if (price2 == 0) {
								System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Item: " + price1 + " / " + whereIsIt1);
							}
							else if (price1 == 0) {
								System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Item: " + price2 + " / " + whereIsIt2);
							}
							else if (price1 < price2) {
								System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Item: " + price1 + " / " + whereIsIt1);
							}
							else {
								System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Item: " + price2 + " / " + whereIsIt2);
							}
						}
					case DIE:
						doDelete();
				}
			}
			
		});
	}
	

	protected void afterClone() {
		_state = CLONED;
		System.out.println("[LOG:" + getAID().getLocalName() + "] Cloned.");
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
				Iterator<Company> cIterator = companies.iterator();
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
					bestPriceFound = new Integer(0);
					ACLMessage toMySelf = new ACLMessage(ACLMessage.INFORM);
					toMySelf.addReceiver(myAgent.getAID());
					toMySelf.setProtocol("finished-contract-net");
					myAgent.send(toMySelf);
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
				bestPriceFound = new Integer(msg.getContent());
				sellerDetails = msg.getSender().getName();
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Bought item: " + brand + " / " + msg.getSender().getLocalName() + " / " + msg.getContent() + " SEK");
				ACLMessage toMySelf = new ACLMessage(ACLMessage.INFORM);
				toMySelf.addReceiver(myAgent.getAID());
				toMySelf.setProtocol("finished-contract-net");
				myAgent.send(toMySelf);
			}
			else {
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Failed to buy item: " + brand + " / " + msg.getSender().getLocalName() + " / " + msg.getContent() + " SEK");
			}
		}
	}


	//a class for keeping the AID of the pricing and the inventory/monitoring agent of a company
	private class Company implements Serializable {
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
