// /*
// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved
// * Royal Institute of Technology (KTH)
// * Swedish Institute of Computer Science (SICS)
// *
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
// package dks.comm.test;
//
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.math.BigInteger;
// import java.net.InetAddress;
// import java.net.MalformedURLException;
// import java.net.UnknownHostException;
//
// import org.apache.log4j.Logger;
// import org.apache.log4j.PropertyConfigurator;
//
// import dks.DKSParameters;
// import dks.addr.DKSRef;
// import dks.boot.DKSNode;
// import dks.boot.DKSPropertyLoader;
// import dks.boot.DKSWebCacheManager;
// import dks.comm.events.CommMakeConnPermanentEvent;
// import dks.comm.events.CommMakeConnTemporaryEvent;
// import dks.messages.StabGetSuccListReqMessage;
// import dks.router.Router.LookupStrategy;
// import dks.router.events.ReliableLookupRequestEvent;
//
// /**
// * The <code>RingBootAndJoinTest</code> class
// *
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: GarbageCollectConnectionsTest.java 390 2007-07-27 15:07:09Z
// Roberto $
// */
// public class GarbageCollectConnectionsTest {
//
// private static Logger log = Logger
// .getLogger(GarbageCollectConnectionsTest.class);
//
// /**
// *
// */
// public GarbageCollectConnectionsTest(String[] n) {
//
// PropertyConfigurator.configure(System
// .getProperty("org.apache.log4j.config.file"));
//
// DKSPropertyLoader propertyLoader = new DKSPropertyLoader();
//
// DKSParameters dksParameters = (propertyLoader).getDKSParameters();
//
// InputStreamReader cin = new InputStreamReader(System.in);
//
// if (n == null) {
// InetAddress ip = null;
// try {
// ip = propertyLoader.getIP();
// } catch (UnknownHostException e) {
// // TODO Handle the exception
// }
// // ip=AddressResolution.getNonLoopbackInet4Address();
// int port = propertyLoader.getPort();
//
// // byte[] hash = AddressHashing.hash(ip, port);
//
// BigInteger bigInt2 = new BigInteger("1023");
//
// DKSRef ref2 = new DKSRef(ip, port, bigInt2.abs());
//
// DKSNode node1 = new DKSNode(ref2, dksParameters, propertyLoader
// .getWebcacheAddress());
//
// node1.getDksImplementation().create();
//
// // try {
// // Thread.currentThread().sleep(13000);
// // } catch (InterruptedException e) {
// // // TODO Auto-generated catch block
// // e.printStackTrace();
// // }
// // UnreliableLookupRequestEvent event = new
// // UnreliableLookupRequestEvent(
// // BigInteger.valueOf(12), LookupStrategy.TRANSITIVE,
// // LookupOperationType.GET_RESPONSIBLE_DKSREF);
// // node1.getScheduler().dispatch(event);
//
// // UnreliableLookupRequestEvent event = new
// // UnreliableLookupRequestEvent(
// // BigInteger.valueOf(1022), LookupStrategy.TRANSITIVE,
// // (Message) (new StabGetSuccListReqMessage()));
//
// // StabGetSuccListReqMessage stabGetSuccListReqMessage = new
// // StabGetSuccListReqMessage();
// //
// // DeliverMessage deliverMessage = new DeliverMessage(
// // stabGetSuccListReqMessage);
// //
// // UnreliableLookupRequestEvent event = new
// // UnreliableLookupRequestEvent(
// // BigInteger.valueOf(12), LookupStrategy.TRANSITIVE,
// // deliverMessage);
//
// // node1.getDksImplementation().leave();
//
// } else {
// if (n[0].equalsIgnoreCase("10")) {
// InetAddress ip = null;
// try {
// ip = propertyLoader.getIP();
// } catch (UnknownHostException e) {
// // TODO Handle the exception
// }
// // int port = propertyLoader.getPort();
//
// // byte[] hash = AddressHashing.hash(ip, port);
//
// String webCacheAddres = propertyLoader.getWebcacheAddress();
//
// DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
// webCacheAddres);
//
// BigInteger bigInt2 = new BigInteger(n[0]);
//
// DKSRef ref2 = new DKSRef(ip, Integer.parseInt(n[1]), bigInt2
// .abs());
//
// DKSNode node1 = new DKSNode(ref2, dksParameters, webCacheAddres);
//
// String rawDKSRef = dksCacheManager.getFirstDKSRef();
// DKSRef dksRef = null;
//
// try {
// dksRef = new DKSRef(rawDKSRef);
// } catch (MalformedURLException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (UnknownHostException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// // node1.getDksImplementation().join(dksRef);
//
// try {
// Thread.currentThread().sleep(3000);
// } catch (InterruptedException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// // node1.getDksImplementation().leave();
//
// } else {
// InetAddress ip = null;
// try {
// ip = propertyLoader.getIP();
// } catch (UnknownHostException e) {
// // TODO Handle the exception
// }
// // int port = propertyLoader.getPort();
//
// // byte[] hash = AddressHashing.hash(ip, port);
//
// String webCacheAddres = propertyLoader.getWebcacheAddress();
//
// DKSWebCacheManager dksCacheManager = new DKSWebCacheManager(
// webCacheAddres);
//
// BigInteger bigInt2 = new BigInteger(n[0]);
//
// DKSRef ref2 = new DKSRef(ip, Integer.parseInt(n[1]), bigInt2
// .abs());
//
// DKSNode node1 = new DKSNode(ref2, dksParameters, webCacheAddres);
//
// String rawDKSRef = dksCacheManager.getFirstDKSRef();
//
// DKSRef dksRef = null;
//
// try {
// dksRef = new DKSRef(rawDKSRef);
// } catch (MalformedURLException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// } catch (UnknownHostException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// // CommMakeConnTemporaryEvent event = new
// // CommMakeConnTemporaryEvent(
// // dksRef);
// //
// // node1.getComponentRegistry().getScheduler().dispatch(event);
// node1.getDksImplementation().join(dksRef);
//
// // CommMakeConnPermanentEvent event1 = new
// // CommMakeConnPermanentEvent(
// // dksRef);
// //
// // node1.getComponentRegistry().getScheduler().dispatch(event1);
//
// try {
// Thread.currentThread().sleep(3000);
// } catch (InterruptedException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// // node1.getDksImplementation().leave();
//
// }
//
// }
//
// /*
// * The property "dks.propFile" must be set for the loader to function
// */
//
// }
//
// /**
// * @param args
// * @throws InterruptedException
// */
// public static void main(String[] args) throws InterruptedException {
//
// if (args.length == 0) {
// new GarbageCollectConnectionsTest(null);
// } else {
// if (args.length == 2) {
// new GarbageCollectConnectionsTest(args);
// } else {
//
// // String[] param = new String[2];
// //
// // new RingBootAndJoinTest(null);
// //
// // Thread.currentThread().sleep(2000);
// //
// // param[0] = "40";
// // param[1] = "12348";
// // new RingBootAndJoinTest(param);
//
// // Thread.currentThread().sleep(1000);
// //
// // param[0] = "11";
// // param[1] = "12342";
// // new RingBootAndJoinTest(param);
// //
// // Thread.currentThread().sleep(1000);
// //
// // param[0] = "14";
// // param[1] = "12343";
// // new RingBootAndJoinTest(param);
// //
// // Thread.currentThread().sleep(1000);
// //
// // param[0] = "5";
// // param[1] = "12344";
// // new RingBootAndJoinTest(param);
// }
//
// }
// }
//
// }
