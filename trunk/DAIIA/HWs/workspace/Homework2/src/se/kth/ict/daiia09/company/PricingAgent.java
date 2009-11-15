package se.kth.ict.daiia09.company;

//se.kth.ict.daiia09.company.PricingAgent
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSContractNetResponder;

import java.util.ArrayList;
import java.util.Iterator;

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
		if (getAID().getLocalName().indexOf("1") >= 0) {
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
		
		
		final MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol("fipa-contract-net"),
				MessageTemplate.MatchPerformative(ACLMessage.CFP) );
		
		addBehaviour(new CyclicBehaviour(this){
			public void action(){
				ACLMessage cfp = myAgent.receive(template);
				if(cfp!=null){
					myAgent.addBehaviour(new SSContractNetResponder(myAgent, cfp){
						protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException,  FailureException, NotUnderstoodException{
							ACLMessage reply = cfp.createReply();
							
							String brand = cfp.getContent();
							int price = searchAvailability(brand);
							String senderName = cfp.getSender().getLocalName();
							
							System.out.println("[LOG PricingAgent] Received CFP for brand "+ brand+" from "+senderName+".");
							
							if(price>0){
								reply.setPerformative(ACLMessage.PROPOSE);
								reply.setContent(price+"");
								System.out.println("[LOG PricingAgent] brand "+brand+" found with price "+ price+". Sending ACLMessage.PROPOSE message to "+senderName+".");
							} else {
								reply.setPerformative(ACLMessage.REFUSE);
								System.out.println("[LOG PricingAgent] brand "+brand+" was not found. Sending ACLMessage.REFUSE message to "+senderName+".");
							}
							
							return reply;
						}
						
						protected ACLMessage handleAcceptProposal(ACLMessage cfp,  ACLMessage propose, ACLMessage accept)throws FailureException{
							
							String brand = cfp.getContent();
							String senderName = cfp.getSender().getLocalName();
							int price = searchAvailability(brand);
							
							propose.setPerformative(ACLMessage.INFORM);
							propose.setContent(price+"");
							
							System.out.println("[LOG PricingAgent] received accept proposal message for brand "+brand+". Sending ACLMessage.INFORM message to "+senderName+".");
							
							return propose;
						}
						
						protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject){
							String senderName = reject.getSender().getLocalName();
							System.out.println("[LOG PricingAgent] received reject message from "+senderName+".");
						}
						
					});
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
				return laptopBrand.getPrice();
		}
		
		return availability;
	}
}
