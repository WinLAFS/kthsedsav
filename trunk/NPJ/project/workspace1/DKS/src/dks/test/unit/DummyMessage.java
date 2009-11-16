///*
//// * Distributed K-ary System (DKS)
// * A Peer-to-Peer Middleware
// * Copyright (c) 2003-2007, all rights reserved 
// * 		Royal Institute of Technology (KTH)
// * 		Swedish Institute of Computer Science (SICS)
// * 
// * See the file DKSLICENSE.TXT included in this distribution for details.
// */
//package dks.test.unit;
//
//import static dks.messages.MessageTypeTable.MSG_TYPE_DUMMY;
//
//import java.io.IOException;
//import java.util.List;
//
//import dks.addr.DKSRef;
//import dks.comm.DirectByteBuffer;
//import dks.marshall.MarshallComponent;
//import dks.messages.Message;
//
///**
// * The <code>DummyMessage</code> class
// * 
// * @author Roberto Roverso
// * @author Cosmin Arad
// * @version $Id: DummyMessage.java 496 2007-12-20 15:39:02Z roberto $
// */
//public class DummyMessage extends Message {
//
//	private static final int messageType = MSG_TYPE_DUMMY;
//
//	private long longNum;
//
//	private int intNum;
//
//	private short shortNum;
//
//	private double doubleNum;
//
//	private String string;
//
//	private DKSRef ref;
//
//	private boolean bool;
//
//	private Object object;
//
//	/**
//	 * 
//	 */
//	public DummyMessage() {
//
//	}
//
//	public DummyMessage(long longNum, int intNum, short shortNum,
//			double doubleNum, String string, DKSRef ref, boolean b,
//			Object object) {
//		this.longNum = longNum;
//		this.intNum = intNum;
//		this.shortNum = shortNum;
//		this.bool = b;
//		this.doubleNum = doubleNum;
//		System.out.println("Double " + doubleNum);
//		this.string = string;
//		this.ref = ref;
//		this.object = object;
//	}
//
//	/**
//	 * Marshals the Message
//	 * 
//	 * @param The
//	 *            Marshaler
//	 */
//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler) {
//		super.initMarshall(this.buffers);
//		// log.debug(buffers.size());
//		marshaler.addInt(buffers, messageType);
//		marshaler.addLong(buffers, longNum);
//		marshaler.addInt(buffers, intNum);
//		marshaler.addBool(buffers, bool);
//		marshaler.addShort(buffers, shortNum);
//		marshaler.addDouble(buffers, doubleNum);
//		marshaler.addString(buffers, string);
//		marshaler.addDKSRef(buffers, ref);
//		try {
//			marshaler.addObject(buffers, object);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return buffers;
//	}
//
//	/**
//	 * Unmarshals the Message
//	 * 
//	 * @param The
//	 *            Marshaler
//	 * @param The
//	 *            List of buffers to be unmarshaled
//	 */
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler,
//			List<DirectByteBuffer> buffers) {
//		longNum = marshaler.remLong(buffers);
//		System.out.println("Long " + longNum);
//		intNum = marshaler.remInt(buffers);
//		System.out.println("Int " + intNum);
//		bool = marshaler.remBool(buffers);
//		System.out.println("Bool " + bool);
//		shortNum = marshaler.remShort(buffers);
//		System.out.println("Short " + shortNum);
//		doubleNum = marshaler.remDouble(buffers);
//		System.out.println("Double " + doubleNum);
//		string = marshaler.remString(buffers);
//		ref = marshaler.remDKSRef(buffers);
//		try {
//			object = marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public int getMessageType() {
//		return messageType;
//	}
//
//	public static int getStaticMessageType() {
//		return messageType;
//	}
//
//	/**
//	 * @return Returns the doubleNum.
//	 */
//	public double getDoubleNum() {
//		return doubleNum;
//	}
//
//	/**
//	 * @return Returns the intNum.
//	 */
//	public int getIntNum() {
//		return intNum;
//	}
//
//	/**
//	 * @return Returns the longNum.
//	 */
//	public long getLongNum() {
//		return longNum;
//	}
//
//	/**
//	 * @return Returns the ref.
//	 */
//	public DKSRef getRef() {
//		return ref;
//	}
//
//	/**
//	 * @return Returns the shortNum.
//	 */
//	public short getShortNum() {
//		return shortNum;
//	}
//
//	/**
//	 * @return Returns the string.
//	 */
//	public String getString() {
//		return string;
//	}
//
//	/**
//	 * @return Returns the bool.
//	 */
//	public boolean getBool() {
//		return bool;
//	}
//
//	/**
//	 * @return Returns the object.
//	 */
//	public Object getObject() {
//		return object;
//	}
//}