///*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package dks.niche.tests;
//
//import dks.dht.DHTComponent.getFlavor;
//import dks.dht.DHTComponent.putFlavor;
//import dks.niche.Niche;
//
///**
// * The <code>HelloWorld</code> class
// *
// * @author Ahmad Al-Shishtawy
// * @version $Id: HelloWorld.java 294 2006-05-05 17:14:14Z alshishtawy $
// */
//public class HelloWorld {
//
//    public static void main(String[] args) {
//
//	System.out.println("Starting Niche...");
//
//	int mode = args[0].equals("create") ? Niche.BOOT : Niche.JOINING;
//	String id = args[1];
//	int port = Integer.parseInt(args[2]);
//	
//	Niche myNicheInterface = new Niche(id, mode, port);
//
//	if(mode == Niche.BOOT) {
//	    myNicheInterface.boot();
//	    myNicheInterface.put("Niche", "Hello World", putFlavor.PUT_OVERWRITE);
//	}
//	else {
//	    myNicheInterface.join();
//	    Object value = myNicheInterface.get("Niche", getFlavor.GET_LAST);
//	    System.out.println("Value of key Niche is " + value);
//	}
//    }
//}
