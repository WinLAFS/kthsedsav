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

package org.objectweb.jasmine.jade.resource.start;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.jasmine.jade.resource.Resource;
import org.objectweb.jasmine.jade.service.Service;
import org.objectweb.jasmine.jade.util.DebugResources;
import org.objectweb.jasmine.jade.util.JadeException;
import org.objectweb.jasmine.jade.util.Logger;

/**
 * @author <a href="mailto:Daniel.Hagimont@imag.fr">Daniel Hagimont
 * 
 */
public class Start implements Service, BindingController {

    final static String RSRC_PREFIX = "rsrc";

    private LinkedHashMap<String, Object> rsrcRef = new LinkedHashMap<String, Object>();

    private Component myself;

    // ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    @SuppressWarnings("unchecked")
    public String[] listFc() {
        Set keys = new HashSet(rsrcRef.keySet());
        return (String[]) keys.toArray(new String[keys.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String itfName) throws NoSuchInterfaceException {
        if (itfName.startsWith(RSRC_PREFIX))
            return rsrcRef.get(itfName);
        if (itfName.equals("component"))
            return myself;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String,
     *      java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void bindFc(String itfName, Object itfValue)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (itfName.startsWith(RSRC_PREFIX))
            rsrcRef.put(itfName, itfValue);
        else if (itfName.equals("component"))
            myself = (Component) itfValue;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String itfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (itfName.startsWith(RSRC_PREFIX))
            rsrcRef.remove(itfName);
        else if (itfName.equals("component"))
            myself = null;
        else
            throw new NoSuchInterfaceException(itfName);
    }

    // ------------------------------------------------------------------------
    // Implementation of Service interface
    // ------------------------------------------------------------------------

    /*
     * start the wrapped j2ee cmp
     * 
     * @see org.objectweb.jasmine.jade.test.j2ee.J2EEService#start()
     */
    public void start() throws JadeException {
        /*
         * Get Resource name. Just for the fun
         */
        String resourceName = "";
        try {
            Component supers[] = Fractal.getSuperController(myself)
                    .getFcSuperComponents();
            resourceName = Fractal.getNameController(supers[0]).getFcName();

        } catch (NoSuchInterfaceException ignored) {
        }

        /*
         * Configure, load ans start each resource under control of Start
         * component.
         */
        Logger.println(DebugResources.info, "[Start] Resource \""
                + resourceName + "\" starting ...");
        try {

            Iterator<String> it = rsrcRef.keySet().iterator();
            while (it.hasNext()) {
                String rname = (String) it.next();// .nextElement();
                Logger.println(DebugResources.info, "[Start] resource : "
                        + rname.replaceAll(RSRC_PREFIX + "_", ""));
                Resource r = (Resource) rsrcRef.get(rname);
                Logger.println(DebugResources.info, "[Start] configure ...");
                r.configure();
                Logger.println(DebugResources.info, "[Start] loadApp ...");
                r.loadApp();
                Logger.println(DebugResources.info, "[Start] start ...");
                r.start();
            }
            Logger.println(DebugResources.info, "[Start] Resource started");
        } catch (Exception e) {
            throw new JadeException("[Start] Can't start resource \""
                    + resourceName + "\"", e);
        }
    }

    /*
     * stop the wrapped j2ee cmp
     * 
     * @see org.objectweb.jasmine.jade.test.j2ee.J2EEService#stop()
     */
    public void stop() throws JadeException {
        /*
         * Get Resource name. To use in log messages
         */
        String resourceName = "";
        try {
            Component supers[] = Fractal.getSuperController(myself)
                    .getFcSuperComponents();
            resourceName = Fractal.getNameController(supers[0]).getFcName();

        } catch (NoSuchInterfaceException ignored) {
        }

        /*
         * Stop each resource under control of Start component.
         */
        Logger.println(DebugResources.info, "[Start] Resource \""
                + resourceName + "\" stopping ...");
        try {
            Iterator<String> it = rsrcRef.keySet().iterator();
            while (it.hasNext()) {
                String rname = (String) it.next();// .nextElement();
                Logger.println(DebugResources.info, "[Start] resource : "
                        + rname);
                Resource r = (Resource) rsrcRef.get(rname);
                Logger.println(DebugResources.info, "[Start] stop ...");
                r.stop();
            }
            Logger.println(DebugResources.info, "[Start] Resource stopped");
        } catch (Exception e) {
            throw new JadeException("[Start] Can't stop resource \""
                    + resourceName + "\"", e);
        }
    }
}