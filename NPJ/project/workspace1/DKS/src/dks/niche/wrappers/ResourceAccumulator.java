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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import dks.niche.ids.ResourceId;

/**
 * The <code>ResourceAccumulator</code> class
 *
 * @author Joel
 * @version $Id: ResourceAccumulator.java 294 2006-05-05 17:14:14Z joel $
 */


public class ResourceAccumulator implements Serializable {
	
	/**
	 * @serialVersionUID - 
	 */
	private static final long serialVersionUID = -58521325796572250L;
	int numberOfRequirements; //possibly not needed, keep it 4 now.
	int numberOfFulfilledRequirements;
	String preferences;
	HashMap<Integer, Integer[]> resourceDescriptions; //: Key - Value : for now, Integer : [Integer : Integer]
	//very primitive bigger than-constraint checking, to be replaced in due time... 
		
	/* general comment: even though the system should handle (very) dynamic situations,
	 * it might be ok for these operations to have some loops...
	 */
	
	public int requiredSize;;
	public int totalSize = 0;
	/*
	 * Style of 
	 */
	public ResourceAccumulator(int req) {
		this.requiredSize = req;
	}
	
	public ResourceAccumulator(BundleDescription bd, String preferences) {
		this(bd);
		this.preferences = preferences;
	}
	
	public ResourceAccumulator(BundleDescription bd) {
		numberOfRequirements = 0;
		numberOfFulfilledRequirements = 0;
		
	    Set<Map.Entry<Integer, Integer>> set = bd.getResourceDescriptions();

	    resourceDescriptions = new HashMap(set.size());
	    
	    for (Map.Entry<Integer, Integer> me : set) {
	    	resourceDescriptions.put(me.getKey(), new Integer[]{0, me.getValue()});
	    	numberOfRequirements++;
	    }
		
	}
	
	public boolean add(int newChunk) {
		return (totalSize += newChunk) >= requiredSize; 
	}
	public boolean add(BundleDescription bd) {

		//Ok, the very simple matching for testing-algorithm: remove the item, once matched!
		
	    Set<Map.Entry<Integer, Integer>> set = bd.getResourceDescriptions();

	    for (Map.Entry<Integer, Integer> me : set) {
	    	
	    	Integer currentKey = me.getKey();
	    	Integer[] currentResource = resourceDescriptions.get(currentKey);
	    	if(currentResource != null) {
	    		
	    		currentResource[0] = (Integer)currentResource[0] + me.getValue();
	    		//System.out.println("currentAllocatedResource is being updated, new value = "+ currentAllocatedResource[0]);
	    	
	    	//switch(evaluationFunction) {
	    	//case BIGGER_THAN:
	    		if(currentResource[0] > currentResource[1]) {
	    			numberOfFulfilledRequirements++;
	    			resourceDescriptions.put(currentKey, null);
	    		}
	    		//else {	resourceDescriptions.put(currentKey, currentAllocatedResource); //do we need?
	    	//break; } 
	    	}
	    }
		return numberOfFulfilledRequirements >= numberOfRequirements;
		
	}
	
	public boolean totalResourceRequirementsMet() {
		System.out.println("numberOfRequirements == numberOfFulfilledRequirements " + numberOfRequirements + " " + numberOfFulfilledRequirements +" "+ (numberOfRequirements == numberOfFulfilledRequirements));
		return numberOfRequirements == numberOfFulfilledRequirements;
	}
	
	public boolean totalResourceRequirementsMet(boolean flag) {
		return totalSize >= requiredSize; 
	}
	//TESTING ONLY * TESTING ONLY * TESTING ONLY * TESTING ONLY * TESTING ONLY * TESTING ONLY * TESTING ONLY * 

	//If preferences are given at time of invokation :
	public Object[] createAllocationSpecifications(ArrayList<ResourceId> ids, Object preferences) {
		return new Object[ids.size()];
	}
	
	//If preferences are already set:
	public Object createAllocationSpecifications(Object id) {
		if (id instanceof ArrayList) {
			return new Object[((ArrayList)id).size()];
		}
		return null;
		
	}
	/*
	HashMap<String, Double> hm = new HashMap<String, Double>();

    hm.put("A", new Double(3434.34));
    hm.put("B", new Double(123.22));
    hm.put("C", new Double(1378.00));
    hm.put("D", new Double(99.22));
    hm.put("E", new Double(-19.08));

    Set<Map.Entry<String, Double>> set = hm.entrySet();

    for (Map.Entry<String, Double> me : set) {
      System.out.print(me.getKey() + ": ");
      System.out.println(me.getValue());
    }
    
    */

}
