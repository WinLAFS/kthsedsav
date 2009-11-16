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

import java.util.ArrayList;

import org.objectweb.jasmine.jade.service.componentdeployment.DeploymentParams;

/**
 * The <code>ScriptInfo</code> class
 *
 * @author Joel
 * @version $Id: ScriptInfo.java 294 2006-05-05 17:14:14Z joel $
 */
public class ScriptInfo {

	ArrayList<ArrayList<Object []>>  myComponents;
	DeploymentParams dp;
	Object currentRequirements;
	Object componentRequirements;
	
	public ScriptInfo() {
		myComponents = new ArrayList();
	}
	
	public void setDeployParams(DeploymentParams dp) {
		dp.name = "storage10";
		this.dp = dp;
	}
	
	public DeploymentParams getDeploymentParams() {
		return dp;
	}
	
	public void addToComponentList(ArrayList<Object []> o) {
		myComponents.add(o);
	}
	
	public ArrayList<ArrayList<Object []>>  getComponents() {
		return myComponents;
	}
	
	public void setRequirements(Object r) {
		currentRequirements = r;
	}
	public void setRequirements() {
		componentRequirements = currentRequirements; 
	}
	
	public Object getComponentRequirements() {
		return componentRequirements;
	}
	
	
}
