/**
 * Copyright (C) : INRIA - Domaine de Voluceau, Rocquencourt, B.P. 105, 
 * 78153 Le Chesnay Cedex - France 
 * 
 * contributor(s) : SARDES project - http://sardes.inrialpes.fr
 *
 * Contact : jade@inrialpes.fr
 *
 * This software is a computer program whose purpose is to provide a framework
 * to build autonomic systems, following an architecture-based approach.
 *
 * This software is governed by the CeCILL-C license under French law and 
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. 
 *
 * In this respect, the user's attention is drawn to the risks associated with 
 * loading,  using,  modifying and/or developing or reproducing the software by 
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate,  and  that  also therefore means  that it is
 * reserved for developers  and  experienced professionals having in-depth 
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling 
 * the security of their systems and/or data to be ensured and,  more generally,
 * to use and operate it in the same conditions as regards security. 
 *
 * The fact that you are presently reading this means that you have had 
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package org.objectweb.jasmine.jade.service.deployer.adl.packages;

import java.util.Map;

import org.objectweb.fractal.deployment.local.api.PackageDescription;

/**
 * 
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas
 *
 */
public class PackageDescriptionImpl implements PackageDescription {

	/**
	 * 
	 */
	private static final long serialVersionUID = -112886113801764786L;

	private String id = null;

	private Map properties = null;
	
	private boolean marked = false;

	public PackageDescriptionImpl(String id, Map properties) {
		this.id = id;
		this.properties = properties;
	}

	public String getPackageID() {
		return this.id;
	}

	public Object getPackageProperties() {
		return this.properties;
	}

	public boolean equals(Object o) {
		if (!(o instanceof PackageDescriptionImpl)) {
			return false;
		}

		PackageDescriptionImpl otherPackage = (PackageDescriptionImpl) o;
		return (otherPackage.id.equals(this.id));
	}
	
	public int hashCode() {
		return this.id.hashCode();
	}

	public void mark() {
		this.marked = true;
	}

	public void unMark() {
		this.marked = false;
	}

	public boolean isMarked() {
		return marked;
	}

}
