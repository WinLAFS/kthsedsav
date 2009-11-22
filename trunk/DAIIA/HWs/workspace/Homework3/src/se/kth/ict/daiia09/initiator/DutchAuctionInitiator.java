package se.kth.ict.daiia09.initiator;

import jade.content.ContentElementList;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import se.kth.ict.daiia09.ontologies.Asus;
import se.kth.ict.daiia09.ontologies.Costs;
import se.kth.ict.daiia09.ontologies.Dell;
import se.kth.ict.daiia09.ontologies.Netbook;
import se.kth.ict.daiia09.ontologies.NetbookOntology;

public class DutchAuctionInitiator extends Agent {
	private int startPrice = 15000;
	private int priceStep = 500;
	private int lowerPrice = 5000;
	private int currentPrice = startPrice;
	private boolean sold = false;
	Netbook netbook = null;
	Costs costs = null;
	
	String brand = "Asus";
	private ArrayList<AID> participantList = new ArrayList<AID>();
	
	public void setup() {
		System.out.println("Initiator agent " + getAID().getName() + " started.");
		
		//get our parameters from arguments
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			try {
				startPrice = new Integer((String) args[1]);
			} 
			catch (Exception e) {
			}
			try {
				priceStep = new Integer((String) args[2]);
			} 
			catch (Exception e) {
			}
			if (args[0].toString().equalsIgnoreCase("asus")) {
				brand = "Asus";
			}
			else {
				brand = "Dell";
			}
		}
		if (brand.equalsIgnoreCase("asus")) {
			netbook = new Asus();
			netbook.setPrice(currentPrice);
			netbook.setScreenSize(11);
			netbook.setWeight(1100);
		}
		else {
			netbook = new Dell();
			netbook.setPrice(currentPrice);
			netbook.setScreenSize(10);
			netbook.setWeight(1000);
		}
		costs = new Costs();
		costs.setItem(netbook);
		costs.setPrice(currentPrice);
		
		//codec
		getContentManager().registerOntology(NetbookOntology.getInstance());
		getContentManager().registerLanguage(new SLCodec());

		System.out.println("[LOG: " +getAID().getLocalName() + "] Trying to shell laptop with start price: " + startPrice + ", step: " + priceStep);
		
		
		DFAgentDescription tempate = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("participant");
		tempate.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, tempate);
			for (int i = 0; i < result.length; i++) {
				participantList.add(result[i].getName());
				System.out.println(":"+ getAID().getLocalName() + ": New participant: " + result[i].getName().getLocalName());
			}		
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		//Informing participants about the auction
		ACLMessage auctionInform = new ACLMessage(ACLMessage.INFORM);
		auctionInform.setProtocol("fipa-dutch-auction");
		Iterator<AID> pIterator = participantList.iterator();
		while (pIterator.hasNext()) {
			auctionInform.addReceiver(pIterator.next());
		}
		
		send(auctionInform);
		
		addBehaviour(new DucthAuctionIteration());
		
		ACLMessage start = new ACLMessage(ACLMessage.PROPAGATE);
		start.setProtocol("new-round");
		start.addReceiver(getAID());
		send(start);
	}
	
	
	private class DucthAuctionIteration extends Behaviour {
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol("new-round"),
				MessageTemplate.MatchPerformative(ACLMessage.PROPAGATE) );
		
		public void action() {
			ACLMessage start = myAgent.receive(template);
			if (start != null) {
				System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] New Iteration STARTING. Price: " + currentPrice + " -------------");
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				Date replyDate = new Date(System.currentTimeMillis() + 3000);
				cfp.setReplyByDate(replyDate);
				cfp.setOntology(NetbookOntology.ONTOLOGY_NAME);
				
				netbook.setPrice(currentPrice);
				costs.setPrice(currentPrice);
				try {
					getContentManager().fillContent(cfp, costs);
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				cfp.setProtocol("fipa-dutch-auction");
				Iterator<AID> pIterator = participantList.iterator();
				while (pIterator.hasNext()) {
					cfp.addReceiver(pIterator.next());
				}

				myAgent.addBehaviour(new DutchAuctionNetInitiator(myAgent, cfp));
				currentPrice -= priceStep;
			}
			else {
				block();
			}
		}

		public boolean done() {
			return (currentPrice < lowerPrice) || sold;
		}

		@Override
		public int onEnd() {
			if (!sold) {
				ACLMessage informEnd = new ACLMessage(ACLMessage.INFORM);
				informEnd.setProtocol("fipa-dutch-auction");
				informEnd.setContent("end");
				Iterator<AID> pIterator = participantList.iterator();
				while (pIterator.hasNext()) {
					informEnd.addReceiver(pIterator.next());
				}
				
				myAgent.send(informEnd);
			}
			
			return super.onEnd();
		}
		
		
	}
	
	private class DutchAuctionNetInitiator extends ContractNetInitiator {

		public DutchAuctionNetInitiator(Agent a, ACLMessage cfp) {
			super(a, cfp);
		}
		
		protected Vector prepareCfps(ACLMessage cfp) {
			System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] sending cfp messages. Protocol: " + cfp.getProtocol());
			return super.prepareCfps(cfp);
		}
		
		protected void handleAllResponses(Vector responses, Vector acceptances) {
			
			sold = (responses.size() > 0);
			
			long accept = 0;
			if (sold) {
				 accept = (System.currentTimeMillis()) % (responses.size());
			}
			for (int i = 0; i < responses.size(); i++) {
				ACLMessage response = (ACLMessage) responses.get(i);
				if (response.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = response.createReply();
					if (i == accept) {
						System.out.println("[LOG: " + myAgent.getAID().getLocalName() +"] Item sold to: " + response.getSender().getLocalName() +
								". Price: " + currentPrice);
					}
					int performative = (i == accept) ? ACLMessage.ACCEPT_PROPOSAL : ACLMessage.REJECT_PROPOSAL;
					reply.setPerformative(performative);
					acceptances.add(reply);
				}
			}
				
			ACLMessage start = new ACLMessage(ACLMessage.PROPAGATE);
			start.setProtocol("new-round");
			start.addReceiver(myAgent.getAID());
			myAgent.send(start);
		}
		
	}
}
