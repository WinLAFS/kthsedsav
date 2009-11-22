package se.kth.ict.daiia09.task2.contractors;

//se.kth.ict.daiia09.company.PricingAgent
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.UngroundedException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.mobility.CloneAction;
import jade.domain.mobility.MobileAgentDescription;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SSContractNetResponder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import se.kth.ict.daiia09.task2.Constants;

import com.sun.corba.se.impl.orbutil.closure.Constant;

/**
 *
 *Pricing agent is responsible for calculating and updating the price of
 *the products. It communicates with it inventory/monitoring agent in order
 *to get data about the availability of the products.
 *<p> It also provides a service for receiving price requests for products
 *and responding with the price of the product.
 */
public class Contractor extends Agent {
	AID inventoryMonitoringAgent = null;
	ArrayList<LaptopBrand> laptopBrandArrayList = new ArrayList<LaptopBrand>();
	private final int priceStep = 200;
	
	public void setup() {
		System.out.println("pricing agent " + getAID().getName() + " started.");
		
		//hard-coded item prices
		String newName = "";
		if (getAID().getLocalName().indexOf("Ebay") >= 0) {
			laptopBrandArrayList.add(new LaptopBrand("LG", 8000, 6000));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 11000, 9000));
			laptopBrandArrayList.add(new LaptopBrand("Mac", 13000, 10000));
			laptopBrandArrayList.add(new LaptopBrand("Lenovo", 8000,5500));
			newName = Constants.CONTRACTOR_EBAY_2;
		}
		else if (getAID().getLocalName().indexOf("Amazon") >= 0){
			laptopBrandArrayList.add(new LaptopBrand("LG", 9000, 5900));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 10000, 8600));
			laptopBrandArrayList.add(new LaptopBrand("Dell", 12000, 10500));
			laptopBrandArrayList.add(new LaptopBrand("Siemens", 10000, 7000));
			newName = Constants.CONTRACTOR_AMAZON_2;
		} else {
			System.err.println("[LOG] Bad name, exiting. I should be either Ebay or Amazon contractor");
			return;
		}
		
		//Registering mobility staff
		getContentManager().registerLanguage(new SLCodec());
		getContentManager().registerOntology(MobilityOntology.getInstance());
		
		
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
		
		String currentContainer=this.here().getName();
		Location myLocation = here();
		
		
		this.doClone(myLocation, newName);
		
		
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
							
							String requestStr = cfp.getContent();
							
							String brand = "";
							String curPrice = "";
							String seller = "";
							
							int curPriceInt =0;
							
							try{
								StringTokenizer st = new StringTokenizer(requestStr, ",");
								brand = st.nextToken();
								curPrice = st.nextToken();
								seller = st.nextToken();
								
								curPriceInt = Integer.parseInt(curPrice);
							} catch (Throwable t){
								System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] Error parsing request string!");
							}
							
							String senderName = cfp.getSender().getLocalName();
							System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] Received CFP for brand "+ brand+" from "+senderName+".");
							
							//First request
							if(seller.equalsIgnoreCase("null")){
								//Searching for normal price
								int normalPrice = searchNormalPrice(brand);
								//Brand found
								if(normalPrice>0){
									reply.setPerformative(ACLMessage.PROPOSE);
									reply.setContent(normalPrice+"");
									System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] brand "+brand+" found with price "+ normalPrice+". Sending ACLMessage.PROPOSE message to "+senderName+".");
								//brand not found
								}else{
									reply.setPerformative(ACLMessage.REFUSE);
									System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] brand "+brand+" was not found. Sending ACLMessage.REFUSE message to "+senderName+".");
								}
							//Not first request
							} else {
								//Searching for minimal price
								int minPrice = searchMinPrice(brand);
								//Brand found
								if(minPrice>0){
									//The previous price was from this agent
									if(seller.equalsIgnoreCase(getAID().getName())){
										reply.setPerformative(ACLMessage.PROPOSE);
										reply.setContent(curPriceInt+"");
										System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] request for brand "+brand+" received. The previous price was mine. Repeating price "+ curPriceInt+". Sending ACLMessage.PROPOSE message to "+senderName+".");
									//The previous price was from another agent
									}else{
										//We can't give smaller price
										if(minPrice>=curPriceInt){
											reply.setPerformative(ACLMessage.REFUSE);
											System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] Can't give smaller price for brand "+brand+". Current price is "+curPriceInt+". Sending ACLMessage.REFUSE message to "+senderName+".");
										//We can give smaller price
										} else{
											int canGivePrice = 0;
											
											//Calculating the price we can give
											if(curPriceInt-priceStep<minPrice){
												canGivePrice = minPrice;
											} else {
												canGivePrice = curPriceInt-priceStep;
											}
											
											reply.setPerformative(ACLMessage.PROPOSE);
											reply.setContent(canGivePrice+"");
											System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] Can give price "+canGivePrice+" for rand "+brand+". Sending ACLMessage.PROPOSE message to "+senderName+".");
										}
									}
								//Brand not found	
								}else{
									reply.setPerformative(ACLMessage.REFUSE);
									System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] brand "+brand+" was not found. Sending ACLMessage.REFUSE message to "+senderName+".");
								}
							}
							
							
							return reply;
						}
						
						protected ACLMessage handleAcceptProposal(ACLMessage cfp,  ACLMessage propose, ACLMessage accept)throws FailureException{
							
							String brand = "";
							String price = "";
							int priceInt = 0;
							String requestStr = accept.getContent();
							
							try{
								StringTokenizer st = new StringTokenizer(requestStr, ",");
								brand = st.nextToken();
								price = st.nextToken();
								
								priceInt = Integer.parseInt(price);
							} catch (Throwable t){
								System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] Error parsing request string!");
							}
							
							
							String senderName = cfp.getSender().getLocalName();
							
							propose.setPerformative(ACLMessage.INFORM);
							propose.setContent(price+"");
							
							System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] received accept proposal message for brand "+brand+". Sending ACLMessage.INFORM message to "+senderName+". Price: " + price);
							
							return propose;
						}
						
						protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject){
							String senderName = reject.getSender().getLocalName();
							String item = reject.getContent();
							System.out.println("<"+getAID().getLocalName()+">"+"[LOG PricingAgent] received reject message for item "+item+" from "+senderName+".");
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
	
	private class LaptopBrand implements Serializable{
		private String name = null;
		private int price = Integer.MAX_VALUE;
		private int minPrice = Integer.MAX_VALUE;
		
		public int getMinPrice() {
			return minPrice;
		}
		public void setMinPrice(int minPrice) {
			this.minPrice = minPrice;
		}
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
		public LaptopBrand(String name, int price, int minPrice) {
			this.minPrice=minPrice;
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
	 * Search for the normal price for the specified brand
	 * 
	 * @param brand the target brand
	 * @return the normal price for the brand
	 */
	private int searchNormalPrice(String brand) {
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
	
	/**
	 * Search for the minimal price for the specified brand
	 * 
	 * @param brand the target brand
	 * @return the minimal price for the brand
	 */
	private int searchMinPrice(String brand) {
		int availability = 0;
		
		Iterator<LaptopBrand> lbIterator= laptopBrandArrayList.iterator();
		//iterates through all items, and if the item of the specified
		//item is found, its availability is returned
		while (lbIterator.hasNext()) {
			LaptopBrand laptopBrand = lbIterator.next();
			if (laptopBrand.getName().equalsIgnoreCase(brand))
				return laptopBrand.getMinPrice();
		}
		
		return availability;
	}
	
	 protected void afterClone() {
		 laptopBrandArrayList = new ArrayList<LaptopBrand>();
		 System.out.println("<"+getAID().getLocalName()+">"+" I'm cloned!!!");
		 if (getAID().getLocalName().indexOf("Ebay") >= 0) {
				laptopBrandArrayList.add(new LaptopBrand("LG", 9000, 7000));
				laptopBrandArrayList.add(new LaptopBrand("Sony", 10000, 8000));
				laptopBrandArrayList.add(new LaptopBrand("Mac", 12000, 19000));
				laptopBrandArrayList.add(new LaptopBrand("Lenovo", 9000,6500));
			}
			else {
				laptopBrandArrayList.add(new LaptopBrand("LG", 10000, 4900));
				laptopBrandArrayList.add(new LaptopBrand("Sony", 12000, 9600));
				laptopBrandArrayList.add(new LaptopBrand("Dell", 11000, 7500));
				laptopBrandArrayList.add(new LaptopBrand("Siemens", 11000, 8000));
			}
	}

}
