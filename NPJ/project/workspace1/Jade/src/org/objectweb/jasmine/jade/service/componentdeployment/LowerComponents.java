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
public interface LowerComponents {

	// Maps (Fractal-level) Components to Niche-level component Ids
	// Also for groups
	SNR getLowerComponent(Component comp);

	// Maps Component to local component ID (token created by backend and passed
	// to
	// Niche)
	Object getLocalComponentID(Component comp);

}