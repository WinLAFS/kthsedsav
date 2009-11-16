package org.objectweb.jasmine.jade.service.componentdeployment;

import java.io.Serializable;

public class DeploymentParams implements Serializable{

	public Object type;
	public String name;
	public String definition;
	public Object controllerDesc;
	public Object contentDesc;
	public Object[] packageDesc;

}
