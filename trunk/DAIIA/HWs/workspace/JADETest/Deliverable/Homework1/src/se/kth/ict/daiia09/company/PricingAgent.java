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
			//inventoryMonitoringAgent = new AID("c1IMA", AID.ISLOCALNAME);
			laptopBrandArrayList.add(new LaptopBrand("LG", 8000));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 11000));
			laptopBrandArrayList.add(new LaptopBrand("Mac", 13000));
			laptopBrandArrayList.add(new LaptopBrand("Lenovo", 8000));
		}
		else {
		//	inventoryMonitoringAgent = new AID("c2IMA", AID.ISLOCALNAME);
			laptopBrandArrayList.add(new LaptopBrand("LG", 9000));
			laptopBrandArrayList.add(new LaptopBrand("Sony", 10000));
			laptopBrandArrayList.add(new LaptopBrand("Dell", 12000));
			laptopBrandArrayList.add(new LaptopBrand("Siemens", 10000));
		}
		
		//finding the corresponding i/m agent 
		String imString = getLocalName().substring(0, 2)+ "IMA";
		inventoryMonitoringAgent = new AID(imString, AID.ISLOCALNAME);
		
		//register the service as an "inventory-monitoring" service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("pricing");
		//the corresponding inventory/monitoring agent
		sd.setName(imString);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		//pricingAgent calculates the price of the laptops every 1 min
		addBehaviour(new TickerBehaviour(this, 60000) {

			protected void onTick() {
				//it follow a sequential behaviour
				SequentialBehaviour calculatePriceBehavior = new SequentialBehaviour();
				//1st it sends requests for the availability of the products to
				//the inv/mon agent
				calculatePriceBehavior.addSubBehaviour(new OneShotBehaviour() {
					
					//for every item it sends an availability request to the i/m agent
					public void action() {
						Iterator<LaptopBrand> lbIterator = laptopBrandArrayList.iterator();
						while (lbIterator.hasNext()) {
							LaptopBrand lb = lbIterator.next();
							ACLMessage requestAvailability = new ACLMessage(ACLMessage.QUERY_IF);
							requestAvailability.addReceiver(inventoryMonitoringAgent);
							requestAvailability.setContent(lb.getName());
							myAgent.send(requestAvailability);
						}
					}
				});
				
				//and then it receives the availabilities and updates the price
				calculatePriceBehavior.addSubBehaviour(new Behaviour() {
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM_IF);
					int received = 0;
					
					public void action() {
						ACLMessage incomingMsg = myAgent.receive(mt);
						if (incomingMsg != null) {
							received++;
							int availability = new Integer(incomingMsg.getContent());
							String brand = incomingMsg.getLanguage();
							
							//finds the correct laptopBrand and updates the price according to its
							//previus availability
							Iterator<LaptopBrand> lbIterator = laptopBrandArrayList.iterator();
							while (lbIterator.hasNext()) {
								LaptopBrand lb = lbIterator.next();
								if (lb.getName().equalsIgnoreCase(brand)) {
									int previusAvailability = lb.getAvailability();
									int previusPrice = lb.getPrice();
									double percentage = 0;
									
									//it updates the price of the product according to the previous value of the
									//availability. If the availability increases, the price falls and the opposite.
									if (previusAvailability != availability) {
										if (!(previusAvailability == 0 && availability > 0)) {
											percentage = (previusAvailability - availability) / (10 * Math.max(availability, previusAvailability));
											lb.setPrice((int) (previusPrice + (percentage * previusPrice)));
										}
										lb.setAvailability(availability);
									}
								}
							}
						} 
						else {
							block();
						}
						
					}
					
					//we expect as many responses as the laptops are
					public boolean done() {
						return (received == laptopBrandArrayList.size());
					}
					
				});
				
				//the sequential behaviour is added the agent
				addBehaviour(calculatePriceBehavior);
				
				//accept requests about the price of product behaviour
				addBehaviour(new CyclicBehaviour() {
					//we accept only ACLMessage.INFORM msgs
					MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
					
					public void action() {
						ACLMessage incomingMsg = myAgent.receive(mt);
						if (incomingMsg != null) {
							String target = incomingMsg.getContent();
							
							//finding the correct product and responding with its value
							Iterator<LaptopBrand> lbIterator = laptopBrandArrayList.iterator();
							while (lbIterator.hasNext()) {
								LaptopBrand lb = lbIterator.next();
								if (lb.getName().equalsIgnoreCase(target)) {
									ACLMessage response = incomingMsg.createReply();
									response.setContent(lb.getPrice() + "");
									response.setPerformative(ACLMessage.PROPOSE);
									myAgent.send(response);
								}
							}
						} 
						else {
							block();
						}
					}
					
				});
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
}