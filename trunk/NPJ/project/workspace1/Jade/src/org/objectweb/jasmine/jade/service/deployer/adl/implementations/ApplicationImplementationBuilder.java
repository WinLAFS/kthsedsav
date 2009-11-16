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

package org.objectweb.jasmine.jade.service.deployer.adl.implementations;

import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.deployment.local.api.GenericInstallingFactory;
import org.objectweb.fractal.deployment.local.api.PackageDescription;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.service.componentdeployment.ComponentDeployment;
import org.objectweb.jasmine.jade.util.DebugAdl;
import org.objectweb.jasmine.jade.util.DebugService;
import org.objectweb.jasmine.jade.util.Logger;

import fr.jade.fractal.util.FractalUtil;

/**
 * This builder is used to create component of the application. If a component
 * have a virtual node : if a physical node is already allocated for this
 * virtual node : the component is deployed on this node. else a new physical
 * node is allocated for the virtual node the component is deployed on this
 * node. Else the component is deployed locally. This builder use an allocator
 * to allocate generic factory. Components are created remotely.
 * 
 * @author <a href="mailto:noel.depalma@inrialpes.fr">Noel de Palma</a>
 *         <p>
 *         Contributors : <a href="mailto:julien.legrand@inrialpes.fr">Julien
 *         Legrand</a>, <a href="mailto:jakub.kornas@inrialpes.fr">Jakub Kornas</a>
 * 
 */
public class ApplicationImplementationBuilder implements ImplementationBuilder,
        BindingController {

    /**
     * 
     */
    private NamingService registry = null;
    
    private ComponentDeployment componentDeployment;
    
    /**
     * 
     */
    private static String[] bindingList = { "registry", "componentDeployment"};

    
    
    
    // ------------------------------------------------------------------------
    // Implementation of ImplementationBuilder interface
    // ------------------------------------------------------------------------
    
    @SuppressWarnings("unchecked")
    public Object createComponent(final Object type, final String name,
            final String definition, final Object controllerDesc,
            final Object contentDesc, final Object[] packageDesc,
            final Object context) throws Exception {
				
    	return componentDeployment.deployComponent(type, name, definition, controllerDesc,
            contentDesc, packageDesc, context);     
    }

    
    
    @SuppressWarnings("unchecked")
    public Object createComponentOLD(final Object type, final String name,
            final String definition, final Object controllerDesc,
            final Object contentDesc, final Object[] packageDesc,
            final Object context) throws Exception {

        Component res = null;
        Type fType = (Type) type;

        String factoryName = (String) ((Map) context).get("factoryName");

        Component remoteBootstrap = registry.lookup(factoryName);

        Logger.println(DebugService.info,
                "****************************************");

        if (remoteBootstrap == null)
            throw new Exception("No Fractal Factory found");

        GenericInstallingFactory physicalNodeFactoryItf = FractalUtil
                .getGenericInstallingFactory(remoteBootstrap);

        /*
         * create the component TODO: treat also the the case of multiple
         * packages
         */

        // String[] obj = (String[]) ((Map) context).get("codeDesc");
        if (packageDesc == null) {
            res = physicalNodeFactoryItf.newFcInstance(fType, controllerDesc,
                    contentDesc);
        } else {
            res = physicalNodeFactoryItf.newFcInstance(fType, controllerDesc,
                    contentDesc, (PackageDescription) packageDesc[0]);
        }

        /*
         * Configure the component
         */
        try {
            Fractal.getNameController(res).setFcName(name);
        } catch (NoSuchInterfaceException ignored) {
        }

        // TODO: remove
        // factoryName = factoryName.replaceFirst("_factory", "");

        /*
         * DEBUG begin
         */

        /*
         * add the component created as a sub-component of the component
         * managed_resources of the jade node.
         */

        Component jadeNode = registry.lookup(factoryName);
        Component comps[] = Fractal.getContentController(jadeNode)
                .getFcSubComponents();
        for (Component c : comps) {
            if (Fractal.getNameController(c).getFcName().equals(
                    "managed_resources")) {
                Fractal.getContentController(c).addFcSubComponent(res);
            }
        }
        /*
         * end
         */

        /*
         * DEBUG begin 25 avr. 2006 jlegrand add the bundleIds to the component
         * attributes
         */

        // try {
        // GenericAttributeController gac = ((GenericAttributeController) res
        // .getFcInterface("attribute-controller"));
        //
        // if (Arrays.asList(gac.listFcAtt()).contains("bundleIds")
        // && ((Map) context).containsKey("bundleIds")) {
        // gac.setAttribute("bundleIds", (String) ((Map) context)
        // .get("bundleIds"));
        // }
        // } catch (NoSuchInterfaceException ignored) {
        // }
        /*
         * end
         */

        Logger.println(DebugService.on, "[ResourceDeployer] ");
        Logger.println(DebugAdl.info, "component \"" + name + "\" created on "
                + factoryName);

        // } else {
        // Logger.println(DebugService.on, "[ResourceDeployer] ");
        // Logger.println(DebugAdl.info, "create local component \"" + name
        // + "\"");
        // Component boot = Fractal.getBootstrapComponent();
        // GenericFactory cf = Fractal.getGenericFactory(boot);
        // res = cf.newFcInstance((Type) type, controllerDesc, contentDesc);
        // }

        return res;
    }

    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        return bindingList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName)
            throws NoSuchInterfaceException {
        if (clientItfName.equals("registry"))
            return registry;
        else if (clientItfName.equals("componentDeployment"))
            return componentDeployment;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
     *      java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (clientItfName.equals("registry")) {
            registry = (NamingService) serverItf;
        } 
        else if (clientItfName.equals("componentDeployment")) {
        	componentDeployment = (ComponentDeployment) serverItf;
        } else
            throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */

    public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("registry"))
            clientItfName = null;
        else if (clientItfName.equals("componentDeployment"))
        	componentDeployment = null;
        else
            throw new NoSuchInterfaceException(clientItfName);
    }
}