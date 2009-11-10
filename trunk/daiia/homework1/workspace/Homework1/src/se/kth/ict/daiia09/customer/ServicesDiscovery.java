package se.kth.ict.daiia09.customer;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/*
 * 
 */
public class ServicesDiscovery extends Agent {
	
	protected void setup() {
		DFAgentDescription tempate = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		tempate.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, tempate);
			String a = "a";
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}	
	}
}
