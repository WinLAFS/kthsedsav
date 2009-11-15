package se.kth.ict.daiia09.customer;

import java.awt.Choice;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.Scanner;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

/**
 * An agent responsible for searching all the available services,
 * asks user to choose one of them and then prints the requirements for
 * calling this service.
 */
public class ServicesDiscovery extends Agent {
	ArrayList<String> servicesList = new ArrayList<String>(); //the list of the available services
	
	protected void setup() {
		DFAgentDescription tempate = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		tempate.addServices(sd);
		try {
			//asking for alla available agent descriptions (for all available services)
			DFAgentDescription[] result = DFService.search(this, tempate);
			//iterating the search results and keeping only the services met for first time
			for (int i = 0; i < result.length; i++) {
				AID serviceProvider = result[i].getName();
				Iterator<ServiceDescription> servicesIterator = result[i].getAllServices();
				while (servicesIterator.hasNext()) {
					ServiceDescription sede = servicesIterator.next();
					if (!servicesList.contains(sede.getType())) {
						servicesList.add(sede.getType());
					}
					
				}
			}
			
			int numServices = servicesList.size();
			int i = 1;
			if (numServices == 0) { //if no available services found
				System.out.println(":" + getAID().getLocalName() + ": NO Available Services found.");
			}
			else {
				//print the available choices to user
				Iterator<String> sIterator = servicesList.iterator();
				while (sIterator.hasNext()) {
					String service = (String) sIterator.next();
					System.out.println(":" + getAID().getLocalName() + ": Service \"" + service + "\" found [" + (i++) + "]");
				}
				System.out.println(":" + getAID().getLocalName() + ": Choice [1 - " + numServices + "]: ");
				
				//and read user's input
				boolean reading = true;
				int choice = 0;
				while (reading) {
					Scanner in = new Scanner(System.in);
					try {
						choice = in.nextInt();
						if (choice < 1 || choice > numServices) {
							throw new InputMismatchException();
						}
						reading = false;
					}
					catch (InputMismatchException e) {
						System.out.println(":" + getAID().getLocalName() + ": Choice [1 - " + numServices + "]: ");
					}
				}
				
				//print the requirements for using the service depending on the user choice
				String choiceString = servicesList.get(choice - 1);
				System.out.println(":" + getAID().getLocalName() + ": Selected \"" + choiceString + "\" service");
				System.out.println(":" + getAID().getLocalName() + ": Parameters needed: ");
				System.out.println(":" + getAID().getLocalName() + ": The brand name of the laptop as content");
				if (choiceString.equals("pricing")) {
					System.out.println(":" + getAID().getLocalName() + ": ACLMessage.QUERY_IF perfomative for the request msg");
				}
				else if (choiceString.equals("im-availability")) {
					System.out.println(":" + getAID().getLocalName() + ": ACLMessage.QUERY_IF perfomative for the request msg");
				}
				else if (choiceString.equals("im-shippingDays")) {
					System.out.println(":" + getAID().getLocalName() + ": ACLMessage.QUERY_REF perfomative for the request msg");
				}
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}	
	}
}
