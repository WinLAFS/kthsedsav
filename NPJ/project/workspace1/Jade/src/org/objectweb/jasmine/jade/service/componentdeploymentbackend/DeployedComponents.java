package org.objectweb.jasmine.jade.service.componentdeploymentbackend;

import org.objectweb.fractal.api.Component;

import dks.niche.ids.ComponentId;
import dks.niche.ids.ResourceId;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos Parlavantzas
 * 
 */
public interface DeployedComponents {

//	Maps local component ID (token created by backend and passed to Niche) to Component

	Component getDeployedComponent(String localCID);
}
