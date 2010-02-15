package se.kth.ict.id2203.lpb.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.BinaryRefAddr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.application.Flp2pMessage;
import se.kth.ict.id2203.flp2p.FairLossPointToPointLink;
import se.kth.ict.id2203.flp2p.Flp2pSend;
import se.kth.ict.id2203.lpb.LazyPBInit;
import se.kth.ict.id2203.lpb.beans.GossipMessage;
import se.kth.ict.id2203.lpb.beans.LPBMessage;
import se.kth.ict.id2203.lpb.beans.NeighbourInfo;
import se.kth.ict.id2203.lpb.events.GossipTimeoutEvent;
import se.kth.ict.id2203.lpb.events.pbBroadcast;
import se.kth.ict.id2203.lpb.events.pbDeliver;
import se.kth.ict.id2203.lpb.ports.ProbabilisticBroadcast;
import se.kth.ict.id2203.unb.components.SimpleUnreliableBroadcast;
import se.kth.ict.id2203.unb.events.unBroadcast;
import se.kth.ict.id2203.unb.events.unDeliver;
import se.kth.ict.id2203.unb.ports.UnreliableBroadcast;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.Timer;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class LazyPB extends ComponentDefinition {
	Positive<FairLossPointToPointLink> flp2p = positive(FairLossPointToPointLink.class);
	Positive<UnreliableBroadcast> ub = positive(UnreliableBroadcast.class);
	Positive<Timer> timer = positive(Timer.class);
	Negative<ProbabilisticBroadcast> pb = negative(ProbabilisticBroadcast.class);

	private static final Logger logger = LoggerFactory
			.getLogger(LazyPB.class);

	private Set<Address> neighborSet;
	private Address self;

	private Set<LPBMessage> stored;
	private double storetreshold;
	private int fanouts;
	private int ttl;
	private ArrayList<Address> neighborList;

	private HashMap<Address, NeighbourInfo> neighboursState = new HashMap<Address, NeighbourInfo>();

	private int lsn = 0;

	public LazyPB() {
		subscribe(handleInit, control);
		subscribe(handleStart, control);
		subscribe(handleFlp2pMessage, flp2p);
		subscribe(handleUNDeliver, ub);
		subscribe(handlePBMessage, pb);
		subscribe(gtHandler, timer);
	}

	Handler<LazyPBInit> handleInit = new Handler<LazyPBInit>() {
		public void handle(LazyPBInit event) {
			neighborSet = event.getNeighborSet();
			neighborList = new ArrayList<Address>();
			neighborList.addAll(neighborSet);

			for (int i = 0; i < neighborList.size(); i++) {
				neighboursState.put(neighborList.get(i), new NeighbourInfo());
			}

			self = event.getSelf();
			stored = new HashSet<LPBMessage>();
			storetreshold = event.getStoreTreshold();
			fanouts = event.getFanouts();
			ttl = event.getTtl();

			logger.debug("lazyPBroadcast :: started");
		}
	};

	Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
		}
	};

	Handler<GossipTimeoutEvent> gtHandler = new Handler<GossipTimeoutEvent>() {
		public void handle(GossipTimeoutEvent arg0) {
			// TODO ADD LOGIC
		}
	};

	Handler<unDeliver> handleUNDeliver = new Handler<unDeliver>() {
		public void handle(unDeliver arg0) {
			try{
	//			logger.info("2a - received message");
				LPBMessage message=(LPBMessage) fromString(arg0.getMessage());
	//			logger.info("2 - received message" +message);
				//15-16
				if(storetreshold > Math.random()){
					logger.info("2 - Storing message: "+message.getMessage()+";"+message.getSender().toString());
					stored.add(message);
				}
				
				if(message.getSender().equals(self)){
					trigger(new pbDeliver(message.getSender(), message.getMessage()), pb);
					return;
				}
				
				Address sender = message.getSender();
				NeighbourInfo neighbour = neighboursState.get(sender);
				
				if(message.getMessageNumber() >= (neighbour.getMaxDelivered()+1)){ //17
					trigger(new pbDeliver(message.getSender(), message.getMessage()), pb); //18
					
					//19-21
					for(Integer i = (neighbour.getMaxDelivered()+1);i<=(message.getMessageNumber()-1); i++){
						GossipMessage gossipMessage = new GossipMessage();
						gossipMessage.setMessageNumber(i);
						gossipMessage.setOriginalMessageSender(message.getSender());
						gossipMessage.setSender(self);
						gossipMessage.setTtl(ttl);
						gossipMessage.setMessageType("Request");
						
						String serializedGossipMessage = encodeToString(gossipMessage);
						gossip(serializedGossipMessage);
						
						neighbour.getMissing().add(i); //21
					}
					//22
					neighbour.setMaxDelivered(message.getMessageNumber());
				}
				//24-26
				else if (neighbour.getMissing().contains(message.getMessageNumber())) {
					neighbour.getMissing().remove(message.getMessageNumber());
					trigger(new pbDeliver(message.getSender(), message.getMessage()), pb);
				}
			} catch (ClassCastException e){
//				logger.info("5 - wrong message");
			}
		}
	};

	Handler<Flp2pMessage> handleFlp2pMessage = new Handler<Flp2pMessage>() {
		public void handle(Flp2pMessage event) {
			try{
				GossipMessage gMessage = (GossipMessage) fromString(event.getMessage());
				//TODO FIX messages
				if(gMessage.getMessageType().equalsIgnoreCase("Request")){
					logger.info("4 - received gossip request from "+gMessage.getSender());
					LPBMessage foundMessage = searchForStoredLPBMessage(gMessage.getOriginalMessageSender(), gMessage.getMessageNumber());
					if(foundMessage!=null){
						gMessage.setMessageData(foundMessage.getMessage());
						gMessage.setMessageType("Data");
						Address target = gMessage.getSender();
						gMessage.setSender(self);
						//trigger(new Flp2pSend(gMessage.getSender(), new Flp2pMessage(self, encodeToString(foundMessage))), flp2p);\
						trigger(new Flp2pSend(target, new Flp2pMessage(self, encodeToString(gMessage))), flp2p);
//						logger.info("6 - sending gossip found responce: "+ gMessage.getMessageData()+" from "+ gMessage.getSender());
					} else if (gMessage.getTtl()>0){
						gMessage.setTtl(gMessage.getTtl()-1);
						gossip(encodeToString(gMessage));
					}
					
				} else if(gMessage.getMessageType().equalsIgnoreCase("Data")){
					logger.info("5 - received gossip responce with data: "+ gMessage.getMessageData()+" from "+ gMessage.getSender());
					if(searchIfMissingLPBMessage(gMessage.getOriginalMessageSender(), gMessage.getMessageNumber())){
						removeMissingLPBMessage(gMessage.getOriginalMessageSender(), gMessage.getMessageNumber());
						trigger(new pbDeliver(gMessage.getOriginalMessageSender(), gMessage.getMessageData()), pb); 
					}
				}
			} catch (ClassCastException e){
//				logger.info("Cant handle: message");
			}
		}
	};

	Handler<pbBroadcast> handlePBMessage = new Handler<pbBroadcast>() {
		public void handle(pbBroadcast event) {
			lsn++;
			
			LPBMessage message = new LPBMessage();
			message.setMsgType("Data");
			message.setMessageNumber(lsn);
			message.setSender(event.getPbd().getSender());
			message.setMessage(event.getPbd().getMsg());
			logger.info("1 - sending pb");
			
			trigger(new unBroadcast(self, null, new unDeliver(self,encodeToString(message))), ub);
		}
	};

	private Set<Address> pickTargets() {
		if (fanouts >= neighborSet.size()) {
			return neighborSet;
		} else {
			HashSet<Address> returnSet = new HashSet<Address>();
			Collections.shuffle(neighborList);
			for (int i = 0; i < fanouts; i++) {
				returnSet.add(neighborList.get(i));
			}
			return returnSet;
		}
	}

	private void gossip(String msg) {
		
		Set<Address> selected = pickTargets();
		for (Address target : selected) {
			logger.info("3 - start gossip to "+target);
			trigger(new Flp2pSend(target, new Flp2pMessage(self, msg)), flp2p);
		}
	}

	public String encodeToString(Serializable o) {

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.close();
			BASE64Encoder encoder = new BASE64Encoder();

			return new String(encoder.encode(baos.toByteArray()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}

	public static Object fromString(String s){
		try {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] data = decoder.decodeBuffer(s);
			ObjectInputStream ois = new ObjectInputStream(
					new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			return o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public LPBMessage searchForStoredLPBMessage(Address sender, int number){
		for(LPBMessage message:stored){
			if(message.getMessageNumber()==number && message.getSender().toString().equalsIgnoreCase(sender.toString())){
				return message;
			}
		}
		return null;
	}
	
	public boolean searchIfMissingLPBMessage(Address sender, int number){
		NeighbourInfo ni = neighboursState.get(sender);
		
		return ni.getMissing().contains(number);
		
	}
	
	public boolean removeMissingLPBMessage(Address sender, int number){
		NeighbourInfo ni = neighboursState.get(sender);
		
		return ni.getMissing().remove(number);
		
	}

}
