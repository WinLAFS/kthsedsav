/*
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.messages;

import java.io.Serializable;
import java.util.ArrayList;

import dks.messages.Message;
import dks.niche.ids.NicheId;

/**
 * The <code>CreateGroupRequest</code> class
 *
 * @author Joel
 * @version $Id: InstantiateSNRRequestMessage.java 294 2006-05-05 17:14:14Z joel $
 */
public class InstantiateSNRRequestMessage extends Message implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 943055798177204234L;

	NicheId id;
	int type;
	ArrayList things;
	Serializable[] parameters;
	
	public InstantiateSNRRequestMessage() {
		
	}

//	public InstantiateSNRRequestMessage(NicheId id, int type, ArrayList things) {
//		this.id = id;
//		this.type = type;
//		this.things = things;
//	}
//	
//	public InstantiateSNRRequestMessage(NicheId id, int type, Object [] parameters) {
//		this.id = id;
//		this.type = type;
//		this.parameters = parameters;
//	}
	
//	@Override
//	public List<DirectByteBuffer> marshall(MarshallComponent marshaler) { //,	List<DirectByteBuffer> buffers)  {
//		super.initMarshall(buffers, messageType, marshaler);
//				
//		try {
//			marshaler.addObject(buffers, id);
//			marshaler.addObject(buffers, things);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return buffers;
//	}
//
//	@Override
//	public void unmarshall(MarshallComponent marshaler, List<DirectByteBuffer> buffers) {
//		
//		
//		try {
//			id = (NicheId) marshaler.remObject(buffers);
//			things = (ArrayList) marshaler.remObject(buffers);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
//
//
//	@Override
//	public int getMessageType() {
//		return messageType;
//	}
//	
//	public static int getStaticMessageType() {
//		return messageType;
//	}
//
	/**
	 * @return Returns the id.
	 */
	public NicheId getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(NicheId id) {
		this.id = id;
	}

	/**
	 * @return Returns the things.
	 */
	public ArrayList getReferences() {
		return things;
	}

	/**
	 * @param things The things to set.
	 */
	public void setReferences(ArrayList things) {
		this.things = things;
	}

	public int getType() {
		return type;
	}
	
	public Serializable[] getParameters() {
		return parameters;
	}
	
}
