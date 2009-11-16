package org.objectweb.jasmine.jade.service.componentdeployment;

import org.objectweb.fractal.api.Component;

import dks.niche.ids.ComponentId;
import dks.niche.ids.GroupId;
import dks.niche.ids.ResourceId;
import dks.niche.ids.SNR;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos
 *         Parlavantzas
 * 
 */
public interface NicheIdRegistry {

	
	
	/**
	 * Looks up Niche ID that corresponds to element name
	 * The names have form: ADLFileName_deploymentId/ElementName  
	 * @param name 
	 * @return
	 */
	SNR lookup(String name);

}