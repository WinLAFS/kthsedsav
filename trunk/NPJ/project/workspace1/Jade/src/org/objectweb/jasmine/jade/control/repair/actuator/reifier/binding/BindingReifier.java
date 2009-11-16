/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier.binding
 * BindingReifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier.binding;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;

import fr.jade.fractal.api.control.ReverseBindingController;
import fr.jade.reflex.api.control.BindingNotificationController;
import fr.jade.reflex.util.Reflex;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.AbstractReifier;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.ReifierException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class BindingReifier extends AbstractReifier {

    // ------------------------------------------------------------------------
    // Implementation of Reifier interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see fr.jade.service.repair.actuator.reifier.Reifier#propagate(org.objectweb.fractal.api.Component)
     */
    public void propagate(Component metaComponent) throws ReifierException {

        clientReifier.propagate(metaComponent);

        try {

            ContentController cc = Fractal.getContentController(metaComponent);

            for (Component subCmp_M : cc.getFcSubComponents()) {
                recreateBindings(subCmp_M);
            }

        } catch (Exception e) {
            throw new ReifierException(e);
        }
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    /**
     * update all the binding involved in the repair process for the exec
     * component corresponding to the meta component.
     * 
     * @param cmp_M
     *            the meta component
     * @throws Exception
     */
    protected void recreateBindings(Component cmp_M) throws Exception {

        recreateExternalsBindings(cmp_M);
        // recreateInternalsBindings(cmp_M);
    }

    protected void recreateExternalsBindings(Component comp_M) throws Exception {

        Component cmp_E = Reflex.getDualComponent(comp_M);

        System.out.println("   > recreate externals Bindings for "
                + Fractal.getNameController(cmp_E).getFcName());

        BindingController bc_M = Fractal.getBindingController(comp_M);
        BindingNotificationController bcn_E = Reflex
                .getBindingNotificationController(cmp_E);

        Object[] extItf_M = comp_M.getFcInterfaces();
        if (extItf_M != null) {

            for (int i = 0; i < extItf_M.length; i++) {
                String itfName = ((Interface) (extItf_M[i])).getFcItfName();
                boolean isClient = ((InterfaceType) (((Interface) (extItf_M[i]))
                        .getFcItfType())).isFcClientItf();

                String msg = "     > " + itfName;

                if (isClient) { // itfName is a client external Interface
                    msg += " (client - ";

                    // ComponentPrinter.printCmp(cmp_M);
                    // ComponentPrinter.printCmpInterfaces(cmp_M);

                    Interface servItf_M = (Interface) bc_M.lookupFc(itfName);

                    // ! the interface can be not bound (contingency is
                    // optionnal)
                    // ie : metaServItf is null
                    if (servItf_M != null) {

                        // the itf is bound at the meta level
                        // => bound the exec level
                        Component servCmp_M = servItf_M.getFcItfOwner();
                        Component servCmp_E = Reflex
                                .getDualComponent(servCmp_M);

                        msg += "server is "
                                + Fractal.getNameController(servCmp_E)
                                        .getFcName() + ")";

                        boolean isInternServItf_M = servItf_M.isFcInternalItf();
                        try {
                            if (isInternServItf_M) {

                                ContentController servCmpCC_E = Fractal
                                        .getContentController(servCmp_E);
                                Interface servItf_E = (Interface) (servCmpCC_E
                                        .getFcInternalInterface(servItf_M
                                                .getFcItfName()));

                                // unbinding is not mandatory, repairedcmp is
                                // vierge
                                bcn_E.bindFcNotification(itfName, servItf_E);
                            } else { // the serv itf is extern

                                Interface execServItf = (Interface) (servCmp_E
                                        .getFcInterface(servItf_M
                                                .getFcItfName()));
                                // unbinding is not mandatory, repairedcmp is
                                // vierge
                                bcn_E.bindFcNotification(itfName, execServItf);
                            }
                        } catch (IllegalBindingException e) {
                            // IllegalBindingException already bound ? or cannot
                            // set short cut ?
                            System.out.println(msg + "error");
                            System.out.println("WARNING ALREADY BOUND ? ");
                            System.out.println(e);
                        }
                    } else {
                        msg += "not bound)";
                    }

                } else { // itfName is a server external Interface

                    msg += " (server)";
                    if (!((itfName.endsWith("-controller")) || (itfName
                            .equals("component")))) {

                        ReverseBindingController rbc_M = ((ReverseBindingController) (comp_M
                                .getFcInterface("reverse-binding-controller")));
                        Object[] bindingSource_M = rbc_M
                                .lookupBindingSource(itfName);

                        if (bindingSource_M != null) {

                            // update all client itf bound to this ext serveritf
                            for (int j = 0; j < bindingSource_M.length; j++) {
                                try {

                                    Interface clientItf_M = (Interface) (bindingSource_M[j]);
                                    Component clientCmp_M = clientItf_M
                                            .getFcItfOwner();
                                    Component clientCmp_E = Reflex
                                            .getDualComponent(clientCmp_M);

                                    msg += "\n       > "
                                            + Fractal.getNameController(
                                                    clientCmp_E).getFcName();

                                    BindingNotificationController clientBCN_E = Reflex
                                            .getBindingNotificationController(clientCmp_E);

                                    // execClientCmp may have a dirty binding
                                    // ref, unbind it

                                    // FOR EXP-MYSQL & SEQUOIA
                                    // if(Fractal.getNameController(clientCmp_E).getFcName().equalsIgnoreCase("balancer")){
                                    // Logger.println("Async binding for " +
                                    // Fractal.getNameController(clientCmp_E).getFcName());
                                    // ECAController ac =
                                    // (ECAController)clientCmp_E.getFcInterface("eca-controller");
                                    //                                          
                                    // ac.addRule(
                                    // "binding-notification-controller",
                                    // "bindFcNotification",
                                    // new Class[]{String.class, Object.class},
                                    // new Object[]
                                    // {clientItf_M.getFcItfName()+"_repared",
                                    // cmp_E.getFcInterface(itfName)},
                                    // new Event(cmp_E, "resource", "start")
                                    // );
                                    //                                          
                                    // Logger.println("done");
                                    // }else{
                                    try {
                                        System.out.println("Try to unbind "
                                                + Fractal.getNameController(
                                                        clientCmp_E)
                                                        .getFcName() + "/"
                                                + clientItf_M.getFcItfName());
                                        clientBCN_E
                                                .unbindFcNotification(clientItf_M
                                                        .getFcItfName());
                                        System.out.println("done");
                                    } catch (Exception e) {
                                        // ignore it the binding is vierge
                                        System.out.println("failed");
                                    }
                                    System.out.println("sync binding for "
                                            + Fractal.getNameController(
                                                    clientCmp_E).getFcName());
                                    clientBCN_E.bindFcNotification(clientItf_M
                                            .getFcItfName(), cmp_E
                                            .getFcInterface(itfName));
                                    System.out.println("done");
                                    // }
                                    // //
                                } catch (Exception e) { // IllegalBindingException
                                    // e){
                                    // IllegalBindingException already bound ?
                                    // or cannot set short cut ?
                                    System.out.println(msg + "error");
                                    System.out
                                            .println("binding src : "
                                                    + ((Interface) (bindingSource_M[j]))
                                                            .getFcItfName());
                                    System.out.println(e);
                                }

                            }
                        }
                    }
                }
                System.out.println(msg);
            }
        }

    }

    protected void recreateInternalsBindings(Component c_M) throws Exception {

        Component cmp_E = Reflex.getDualComponent(c_M);
        System.out.println("   > recreate internals Bindings for "
                + Fractal.getNameController(cmp_E).getFcName());

        BindingController bc_M = Fractal.getBindingController(c_M);
        BindingNotificationController bcn_E = Reflex
                .getBindingNotificationController(cmp_E);

        Object[] extItf_M = c_M.getFcInterfaces();

        // update internal interfaces if metaCmp is a composite
        try {
            ContentController cc_M = Fractal.getContentController(c_M);

            try {
                extItf_M = cc_M.getFcInternalInterfaces();
                if (extItf_M != null) {

                    for (int i = 0; i < extItf_M.length; i++) {

                        String itfName = ((Interface) (extItf_M[i]))
                                .getFcItfName();
                        // isIntern =
                        // ((Interface)(extItf[i])).isFcInternalItf();
                        boolean isClient = ((InterfaceType) (((Interface) (extItf_M[i]))
                                .getFcItfType())).isFcClientItf();
                        // Logger.println(itfName+" : client: "+isClient+"
                        // intern :"+isIntern);

                        if (isClient) {

                            Interface servItf_M = (Interface) bc_M
                                    .lookupFc(itfName);

                            // ! the interface can be not bound (contingency is
                            // optionnal)
                            // ie : metaServItf is null
                            // TO DO check contingency
                            if (servItf_M != null) {

                                // the itf is bound at the meta level
                                // => bound the exec level
                                Component servCmp_M = servItf_M.getFcItfOwner();
                                Component servCmp_E = Reflex
                                        .getDualComponent(servCmp_M);

                                boolean isInternServItf_M = servItf_M
                                        .isFcInternalItf();
                                try {

                                    if (isInternServItf_M) {

                                        ContentController servCmpCC_E = Fractal
                                                .getContentController(servCmp_E);
                                        Interface servItf_E = (Interface) (servCmpCC_E
                                                .getFcInternalInterface(servItf_M
                                                        .getFcItfName()));

                                        // unbinding is not mandatory,
                                        // repairedcmp
                                        // is vierge
                                        bcn_E.bindFcNotification(itfName,
                                                servItf_E);
                                    } else { // the serv itf is extern

                                        Interface servItf_E = (Interface) (servCmp_E
                                                .getFcInterface(servItf_M
                                                        .getFcItfName()));
                                        // unbinding is not mandatory,
                                        // repairedcmp
                                        // is vierge
                                        bcn_E.bindFcNotification(itfName,
                                                servItf_E);

                                    }
                                } catch (IllegalBindingException e) {
                                    // IllegalBindingException already bound ?
                                    // or
                                    // cannot set short cut ?
                                    System.out
                                            .println("     > WARNING ALREADY BOUND when client int itf update? ");
                                    System.out.println(e);
                                }
                            }

                        } else { // internal server itf

                            ReverseBindingController rbc_M = ((ReverseBindingController) (c_M
                                    .getFcInterface("reverse-binding-controller")));
                            Object[] bindingSource_M = rbc_M
                                    .lookupBindingSource(itfName);

                            if (bindingSource_M != null) {
                                // update all client itf bound to this ext
                                // serveritf
                                for (int j = 0; j < bindingSource_M.length; j++) {
                                    try {
                                        Interface clientItf_M = (Interface) (bindingSource_M[j]);
                                        Component clientCmp_M = clientItf_M
                                                .getFcItfOwner();
                                        Component clientCmp_E = Reflex
                                                .getDualComponent(clientCmp_M);
                                        BindingNotificationController clientBCN_E = Reflex
                                                .getBindingNotificationController(clientCmp_E);

                                        // execClientCmp may have a dirty
                                        // binding
                                        // ref, unbind it
                                        try {
                                            clientBCN_E
                                                    .unbindFcNotification(clientItf_M
                                                            .getFcItfName());
                                        } catch (Exception e) {
                                            // ignore it the binding is vierge
                                            // may be execClientCmp is also just
                                            // regenerated
                                        }
                                        clientBCN_E
                                                .bindFcNotification(
                                                        clientItf_M
                                                                .getFcItfName(),
                                                        Fractal
                                                                .getContentController(
                                                                        cmp_E)
                                                                .getFcInternalInterface(
                                                                        itfName));
                                    } catch (Exception e) {
                                        System.out
                                                .println("Error when rebind :");
                                        System.out
                                                .println("\tbinding src : "
                                                        + ((Interface) (bindingSource_M[j]))
                                                                .getFcItfName());
                                        System.out
                                                .println("\tbinding target : "
                                                        + itfName);
                                        System.out.println(e);
                                    }

                                }
                            }

                        }
                    }
                }
            } catch (Exception e1) {
                System.out.println(e1);
            }

        } catch (NoSuchInterfaceException e) {
            System.out.println("     > Not a composite");
        }
    }

}
