/*
 * Distributed K-ary System (DKS)
 * A Peer-to-Peer Middleware
 * Copyright (c) 2003-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 * 
 * See the file DKSLICENSE.TXT included in this distribution for details.
 */
package dks.niche.wrappers;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;

/**
 * The <code>BulkSendContent</code> class
 *
 * @author Joel
 * @version $Id: BulkSendContent.java 294 2006-05-05 17:14:14Z joel $
 */
public class BulkSendContent implements Serializable {

	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = 517218768695218298L;
	
	int operationId;
	String handlerId;;
	HashMap<BigInteger, DeployWrapper> destinationMap;
	HashMap<BigInteger, DeployWrapper> contentMap;
	
	
	/*
	 *  (content) [a, b, c] to (id) [10, 35, 69, 205] using (pos) [1, 1, 2, 3]
	 *  
	 */
	
	public BulkSendContent(int operationId, String handlerId, BigInteger[]ids, DeployWrapper[] destinationInfo, DeployWrapper[] content) { //, int[]positions) {
		
		this.operationId = operationId;
		this.handlerId = handlerId;
		destinationMap = new HashMap(destinationInfo.length);
		contentMap = new HashMap(destinationInfo.length);
		
		for(int i = 0; i < content.length; i++) {
			destinationMap.put(ids[i], destinationInfo[i]);
			//System.out.println("content is: " + content[positions[i]] + " for position "+positions[i]);
			contentMap.put(ids[i], content[i]); //[positions[i]]);
		}
		
	}
	
	public DeployWrapper getDestinationInfo(BigInteger key) {
		return destinationMap.get(key);
	}
	
	//public Object getContent(BigInteger key) {		return contentMap.get(key);	}
	
	public DeployWrapper getDeployWrapper(BigInteger key) {
		
		DeployWrapper res = destinationMap.get(key);
		res.setComponentInfo(contentMap.get(key), handlerId);
		return res;
	}
	
	public int getOperationId() {
		return operationId;
	}
	
	public String getHandlerId() {
		return handlerId;
	}
	
}
