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

package org.objectweb.jasmine.jade.util;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.control.SuperController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class FractalUtil {

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * Search, in the composite component "sup", the sub-component defined by
     * the expression "sub" defining the path delimited with '/'.
     * 
     * In [1[2,3[4]]], getSubComponent(1, "3/4") returns 4, getSubComponent(1,
     * "2/4") throws an exception.
     * 
     * @param sup
     * @param sub
     * @return
     * @throws NoSuchComponentException
     */
    public static Component getSubComponentByPath(Component sup, String sub)
            throws NoSuchComponentException {

        Component res = sup;

        while (sub != null) {
            String subsub;
            int i = sub.indexOf("/");
            if (i != -1) {
                subsub = sub.substring(0, i);
                sub = sub.substring(i + 1);
            } else {
                subsub = sub;
                sub = null;
            }

            res = getDirectSubComponentByName(res, subsub);

        }

        return res;
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * 
     * 
     * @param comp
     * @param name
     * @return
     * @throws NoSuchInterfaceException
     * @throws
     */
    public static Component getDirectSubComponentByName(Component comp,
            String name) throws NoSuchComponentException {

        ContentController cc = null;
        try {
            cc = Fractal.getContentController(comp);
        } catch (NoSuchInterfaceException e) {
            throw new NoSuchComponentException(e.getMessage());
        }
        for (Component sc : cc.getFcSubComponents()) {
            try {
                NameController nc = Fractal.getNameController(sc);
                if (name.equals(nc.getFcName())) {
                    return sc;
                }
            } catch (NoSuchInterfaceException ignored) {
            }

        }

        String compName = "";
        try {
            compName = Fractal.getNameController(comp).getFcName();
        } catch (NoSuchInterfaceException ignored) {
        }

        throw new NoSuchComponentException("The component named \"" + name
                + "\" was not found in the component \"" + compName + "\"");
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * Search, in the composite component "sub", the super-component defined by
     * the expression "sup" defining the path delimited with '/'.
     * 
     * In [1[2,3[4]]], getSupComponent(4, "3/2") returns 2, getSubComponent(4,
     * "2/1") throws an exception.
     * 
     * @param sup
     * @param sub
     * @return
     * @throws NoSuchComponentException
     */
    public static Component getSuperComponentByPath(Component sub, String sup)
            throws NoSuchComponentException {

        Component res = sub;

        while (sup != null) {
            String supsup;
            int i = sup.indexOf("/");
            if (i != -1) {
                supsup = sup.substring(0, i);
                sup = sup.substring(i + 1);
            } else {
                supsup = sup;
                sup = null;
            }

            res = getDirectSuperComponentByName(res, supsup);

        }

        return res;
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * @param comp
     * @param name
     * @return
     * @throws NoSuchComponentException
     */
    public static Component getDirectSuperComponentByName(Component comp,
            String name) throws NoSuchComponentException {

        SuperController sc = null;
        try {
            sc = Fractal.getSuperController(comp);
        } catch (NoSuchInterfaceException e) {
            throw new NoSuchComponentException(e.getMessage());
        }
        for (Component c : sc.getFcSuperComponents()) {
            try {
                NameController nc = Fractal.getNameController(c);
                if (name.equals(nc.getFcName())) {
                    return c;
                }
            } catch (NoSuchInterfaceException ignored) {
            }

        }

        String compName = "";
        try {
            compName = Fractal.getNameController(comp).getFcName();
        } catch (NoSuchInterfaceException ignored) {
        }

        throw new NoSuchComponentException("The component named \"" + compName
                + "\" hasn't super component named \"" + name + "\"");
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * @param comp
     * @param name
     * @return
     * @throws NoSuchComponentException
     */
    public static Component getFirstFoundSuperComponentByName(Component comp,
            String name) throws NoSuchComponentException {

        try {
            String nameComp = Fractal.getNameController(comp).getFcName();
            if (name.equals(nameComp)) {
                return comp;
            }
        } catch (NoSuchInterfaceException ignored) {
        }

        SuperController sc = null;
        try {
            sc = Fractal.getSuperController(comp);
        } catch (NoSuchInterfaceException e) {
            throw new NoSuchComponentException(e.getMessage());
        }

        Component cmps[] = sc.getFcSuperComponents();
        if (cmps.length != 0) {
            for (Component c : cmps) {
                try {
                    Component res = getFirstFoundSuperComponentByName(c, name);
                    return res;
                } catch (NoSuchComponentException e) {
                }
            }
        }

        String compName = "";
        try {
            compName = Fractal.getNameController(comp).getFcName();
        } catch (NoSuchInterfaceException ignored) {
        }

        throw new NoSuchComponentException("The component named \"" + compName
                + "\" hasn't super component named \"" + name + "\"");
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * @param comp
     * @param name
     * @return
     * @throws NoSuchComponentException
     */
    public static Component getFirstFoundSubComponentByName(Component comp,
            String name) throws NoSuchComponentException {

        try {
            String nameComp = Fractal.getNameController(comp).getFcName();
            if (name.equals(nameComp)) {
                return comp;
            }
        } catch (NoSuchInterfaceException ignored) {
        }

        ContentController sc = null;
        try {
            sc = Fractal.getContentController(comp);
        } catch (NoSuchInterfaceException e) {
            throw new NoSuchComponentException(e.getMessage());
        }

        Component cmps[] = sc.getFcSubComponents();
        if (cmps.length != 0) {
            for (Component c : cmps) {
                try {
                    Component res = getFirstFoundSubComponentByName(c, name);
                    return res;
                } catch (NoSuchComponentException e) {
                }
            }
        }

        String compName = "";
        try {
            compName = Fractal.getNameController(comp).getFcName();
        } catch (NoSuchInterfaceException ignored) {
        }

        throw new NoSuchComponentException("The component named \"" + compName
                + "\" hasn't sub component named \"" + name + "\"");
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    public static Component getFirstFoundSuperComponentByServerInterfaceSignature(
            Component comp, String signature) throws NoSuchComponentException {

        if (exposeServerInterfaceBySignature(comp, signature)) {
            return comp;
        }

        SuperController sc = null;
        try {
            sc = Fractal.getSuperController(comp);
        } catch (NoSuchInterfaceException e) {
            throw new NoSuchComponentException(e.getMessage());
        }

        Component cmps[] = sc.getFcSuperComponents();
        if (cmps.length != 0) {
            for (Component c : cmps) {
                try {
                    Component res = getFirstFoundSuperComponentByServerInterfaceSignature(
                            c, signature);
                    return res;
                } catch (NoSuchComponentException e) {
                }
            }
        }

        String compName = "";
        try {
            compName = Fractal.getNameController(comp).getFcName();
        } catch (NoSuchInterfaceException ignored) {
        }

        throw new NoSuchComponentException(
                "The component named \""
                        + compName
                        + "\" hasn't super component exposing a server interface with signature \""
                        + signature + "\"");
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * Test if the component passed as parameter has a client itf whose
     * signature correspond to the signature passed as parameter.
     * 
     * @param cmp
     *            the component to be tested
     * @param signature
     *            the signature of the itf
     * @return an array of interface object having this signature (these
     *         interfaces are client/external) or null.
     */
    public static Interface[] getClientInterfaceBySignature(Component cmp,
            String signature) {

        List<Object> res = new ArrayList<Object>();

        Object[] itf = cmp.getFcInterfaces();

        if (itf != null) {
            InterfaceType itfType = null;
            for (int i = 0; i < itf.length; i++) {
                itfType = (InterfaceType) (((Interface) itf[i]).getFcItfType());
                if (itfType.isFcClientItf()
                        && itfType.getFcItfSignature().equals(signature)) {
                    res.add(itf[i]);
                }
            }
        }
        if (!res.isEmpty())
            return (Interface[]) res.toArray(new Interface[0]);
        else
            return null;
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * Test if the component passed as parameter has a client itf whose
     * signature correspond to the signature passed as parameter.
     * 
     * @param cmp
     *            the component to be tested
     * @param signature
     *            the signature of the itf
     * @return an array of interface object having this signature (these
     *         interfaces are client/external) or null.
     */
    public static Interface[] getServerInterfaceBySignature(Component cmp,
            String signature) {

        List<Object> res = new ArrayList<Object>();

        Object[] itf = cmp.getFcInterfaces();

        if (itf != null) {
            InterfaceType itfType = null;
            for (int i = 0; i < itf.length; i++) {
                itfType = (InterfaceType) (((Interface) itf[i]).getFcItfType());
                if (!itfType.isFcClientItf()
                        && itfType.getFcItfSignature().equals(signature)) {
                    res.add(itf[i]);
                }
            }
        }
        if (!res.isEmpty())
            return (Interface[]) res.toArray(new Interface[0]);
        else
            return null;
    }

    // ------------------------------------------------------------------------
    // 
    // ------------------------------------------------------------------------

    /**
     * @param cmp
     * @param signature
     * @return
     */
    public static boolean exposeServerInterfaceBySignature(Component cmp,
            String signature) {

        Object[] itf = cmp.getFcInterfaces();

        if (itf.length != 0) {
            InterfaceType itfType = null;
            for (int i = 0; i < itf.length; i++) {
                itfType = (InterfaceType) (((Interface) itf[i]).getFcItfType());
                if (!itfType.isFcClientItf()
                        && itfType.getFcItfSignature().equals(signature)) {
                    return true;
                }
            }
        }

        return false;

    }
}
