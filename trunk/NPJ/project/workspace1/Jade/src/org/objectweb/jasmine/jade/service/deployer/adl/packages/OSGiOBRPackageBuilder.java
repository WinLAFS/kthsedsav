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

import java.util.Iterator;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.deployment.local.api.Installer;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.Logger;

/**
 * A OSGi/OBR-based implementation of the {@link PackageBuilder} interface. This
 * implementation works with OSGi bundles as packaging system and an OBR as
 * package repository.
 * 
 * @author <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>, <a
 *         href="mailto:julien.legrand@inrialpes.fr">Julien Legrand</a>
 * 
 */
public class OSGiOBRPackageBuilder implements PackageBuilder {

    // private Map installedBundle = new TreeMap();

    // --------------------------------------------------------------------------
    // Implementation of the PackageBuilder interface
    // --------------------------------------------------------------------------

    public void installPackage(Component node, String packageName, Map context)
            throws Exception {

//        /*
//         * physicalNodeName is fixed in NodeLauncher, at the creation of the
//         * node
//         */
//        String physicalNodeName = Fractal.getNameController(node).getFcName();
//        /*
//         * Display information
//         */
//        Logger.println(DebugService.info,
//                "****************************************");
//        Logger.println(DebugService.on, "[ResourceDeployer] ");
//        Logger.println(DebugService.info, "install package \"" + packageName
//                + "\"" + " on node \"" + physicalNodeName + "\""
//                + " with context :");
//
//        Iterator it = context.keySet().iterator();
//        while (it.hasNext()) {
//            String key = it.next().toString();
//            Logger.println(DebugService.info, "    " + key + " : "
//                    + context.get(key).toString());
//        }

        /*
         * Install package
         */
        // TODO: remove
        // Installer installer = (Installer) node.getFcInterface("installer");
        // PackageDescription pkgDesc = new PackageDescriptionImpl(packageName,
        // context);
        // installer.install(pkgDesc);
        // TODO: remove
        // long id = installer.install(packageName, context);
        // installedBundle.put(id, packageName);
        // if(context.containsKey("bundleIds")){
        // String bundleIds = (String)context.get("bundleIds");
        // bundleIds += ":" + id;
        // }
        // else{
        // context.put("bundleIds", id);
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.jasmine.jade.adl.packages.PackageBuilder#uninstallPackage(org.objectweb.fractal.api.Component,
     *      java.lang.String)
     */
    public void uninstallPackage(Component node, String packageName)
            throws Exception {
//
//        /*
//         * physicalNodeName is fixed in NodeLauncher, at the creation of the
//         * node
//         */
//        String physicalNodeName = Fractal.getNameController(node).getFcName();
//
//        /*
//         * Display information
//         */
//        Logger.println(DebugService.info,
//                "****************************************");
//        Logger.println(DebugService.on, "[ResourceDeployer] ");
//        Logger.println(DebugService.info, "Uninstall package \"" + packageName
//                + "\"" + " on node \"" + physicalNodeName + "\"");
//
//        /*
//         * Uninstall package
//         */
//        Installer installer = (Installer) node.getFcInterface("installer");
//        // TODO: remove
//        // installer.uninstall(packageName);

    }
}