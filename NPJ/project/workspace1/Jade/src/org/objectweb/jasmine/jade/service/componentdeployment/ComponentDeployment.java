package org.objectweb.jasmine.jade.service.componentdeployment;

public interface ComponentDeployment {

	Object deployComponent(Object type, String name, String definition, Object controllerDesc, Object contentDesc, Object[] packageDesc, Object context);

}
