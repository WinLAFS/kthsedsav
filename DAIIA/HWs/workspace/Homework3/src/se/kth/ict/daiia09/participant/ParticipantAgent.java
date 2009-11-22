package se.kth.ict.daiia09.participant;

import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
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

import se.kth.ict.daiia09.ontologies.Asus;
import se.kth.ict.daiia09.ontologies.Costs;
import se.kth.ict.daiia09.ontologies.Dell;
import se.kth.ict.daiia09.ontologies.Netbook;
import se.kth.ict.daiia09.ontologies.NetbookOntology;

public class ParticipantAgent extends Agent {
	private ArrayList<Netbook> netbooks = new ArrayList<Netbook>();

	public void setup() {
		System.out.println("[LOG PARTICIPANT] participant agent "
				+ getAID().getName() + " started.");

		//hard-coded item prices
		if (getAID().getLocalName().indexOf("1") >= 0) {
			Dell dellNetbook = new Dell();
			dellNetbook.setPrice(10000);
			netbooks.add(dellNetbook);
			Asus asusNetbook = new Asus();
			asusNetbook.setPrice(9000);
			netbooks.add(asusNetbook);
		}
		else {
			Dell dellNetbook = new Dell();
			dellNetbook.setPrice(9000);
			netbooks.add(dellNetbook);
			Asus asusNetbook = new Asus();
			asusNetbook.setPrice(10000);
			netbooks.add(asusNetbook);
		}

		// register the service as an "inventory-monitoring" service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("participant");
		// the corresponding inventory/monitoring agent
		sd.setName(getAID().getLocalName());
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		getContentManager().registerOntology(NetbookOntology.getInstance());
		getContentManager().registerLanguage(new SLCodec());

		final MessageTemplate template1 = MessageTemplate.and(MessageTemplate
				.MatchProtocol("fipa-dutch-auction"), MessageTemplate
				.MatchPerformative(ACLMessage.INFORM));

		final MessageTemplate template2 = MessageTemplate.and(MessageTemplate
				.MatchProtocol("fipa-dutch-auction"), MessageTemplate.or(
				MessageTemplate.MatchPerformative(ACLMessage.CFP), MessageTemplate.MatchPerformative(ACLMessage.INFORM)));

		SequentialBehaviour participateInAuction = new SequentialBehaviour();

		participateInAuction.addSubBehaviour(new OneShotBehaviour() {
			public void action() {
				System.out.println("[LOG PARTICIPANT] Start participating in auction");
				ACLMessage incomingMsg = myAgent.blockingReceive(template1);
				if(incomingMsg!=null){
					System.out.println("[LOG PARTICIPANT] Starting new auction.");
				}
			}
		});

		participateInAuction.addSubBehaviour(new Behaviour() {
			
			boolean auctionEnd = false;
			
			public void action() {
				ACLMessage cfp = myAgent.receive(template2);

				if (cfp != null && cfp.getPerformative()==ACLMessage.CFP) {
					myAgent.addBehaviour(new SSContractNetResponder(myAgent,cfp) {
						
						protected ACLMessage handleCfp(ACLMessage cfp)throws RefuseException, FailureException,NotUnderstoodException {
							ACLMessage reply = cfp.createReply();

							Costs costs = null;
							try {
								costs = (Costs) getContentManager().extractContent(cfp);
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							System.out.println("[LOG PARTICIPANT] Received price "+costs.getPrice());
							
							Netbook nbForSale = costs.getItem();
							
							if (costs != null) {
								Iterator<Netbook> ni = netbooks.iterator();
								while (ni.hasNext()){
									Netbook nb = ni.next();
									costs.getItem();
									if(nb instanceof Asus && nbForSale instanceof Asus && costs.getPrice()<=nb.getPrice()){
										System.out.println("[LOG PARTICIPANT] Price accepted, sending answer.");
										reply.setPerformative(ACLMessage.PROPOSE);
										reply.setContent(costs.getPrice() + "");
										return reply;
									} else if (nb instanceof Dell && nbForSale instanceof Dell && costs.getPrice()<=nb.getPrice()){
										System.out.println("[LOG PARTICIPANT] Price accepted, sending answer.");
										reply.setPerformative(ACLMessage.PROPOSE);
										reply.setContent(costs.getPrice() + "");
										return reply;
									} 
								}
								return null;
							} else {
								System.out.println("[LOG PARTICIPANT] Price rejected, no messages sent.");
								return null;
							}
						}

						protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
							System.out.println("[LOG PARTICIPANT] My proposal was accepted.");
							return null;
						}

						protected void handleRejectProposal(ACLMessage cfp,ACLMessage propose, ACLMessage reject) {
							System.out.println("[LOG PARTICIPANT] My proposal was rejected.");
						}

					});
				} else if (cfp != null && cfp.getPerformative()==ACLMessage.INFORM) {
					if (cfp.getContent().equals("end")){
						System.out.println("[LOG PARTICIPANT] Auction end.");
						auctionEnd=true;
					}
				}
			}

			public boolean done() {
				return auctionEnd;
			}
		});

		addBehaviour(participateInAuction);

	}

	protected void takeDown() {
		// try to deregister the services when the agent goes down
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
}
