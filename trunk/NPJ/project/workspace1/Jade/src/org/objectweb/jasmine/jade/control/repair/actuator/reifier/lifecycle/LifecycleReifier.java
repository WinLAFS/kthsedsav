/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier.lifecycle
 * LifecycleReifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier.lifecycle;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.util.Fractal;

import fr.jade.fractal.api.control.ReverseBindingController;
import fr.jade.reflex.util.Reflex;
import org.objectweb.jasmine.jade.resource.Resource;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.AbstractReifier;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.ReifierException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class LifecycleReifier extends AbstractReifier {

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
                startRessource(subCmp_M);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    /**
     * start the new wrapped ressource if necessary
     * 
     * @param cmp_M
     *            the meta component
     * @throws Exception
     */
    private void startRessource(Component cmp_M) throws Exception {

        Component cmp_E = Reflex.getDualComponent(cmp_M);

        Object[] extItf_E = cmp_E.getFcInterfaces();

        for (int i = 0; i < extItf_E.length; i++) {
            try {
                
                Interface itf_E = (Interface) (extItf_E[i]);
                boolean isClient = ((InterfaceType) ((itf_E).getFcItfType()))
                        .isFcClientItf();
                
                if (!(isClient)) {
                    
                    String itfSign = ((InterfaceType) ((itf_E).getFcItfType()))
                            .getFcItfSignature();
                    
                    if (itfSign.equals("fr.jade.resources.resource.Resource")) {
                        
                        System.out.println("   > restarting "
                                + Fractal.getNameController(cmp_E).getFcName());

                        ((Resource) itf_E).configure();
                        ((Resource) itf_E).loadApp();
                        ((Resource) itf_E).start();

                        // restart resources binded on meta
                        ReverseBindingController rbc_M = ((ReverseBindingController) (cmp_M
                                .getFcInterface("reverse-binding-controller")));
                        
                        Object[] bindingSource_M = rbc_M
                                .lookupBindingSource(itf_E.getFcItfName());

                        if (bindingSource_M != null) {
                            for (int j = 0; j < bindingSource_M.length; j++) {
                                try {
                                    Interface clientItf_M = (Interface) (bindingSource_M[j]);
                                    Component clientCmp_M = clientItf_M
                                            .getFcItfOwner();
                                    Component clientCmp_E = Reflex.getComponent(clientCmp_M);
                                    Object[] extItf2_E = clientCmp_E
                                            .getFcInterfaces();

                                    for (int k = 0; k < extItf2_E.length; k++) {
                                        Interface itf2_E = (Interface) (extItf2_E[k]);
                                        boolean isClient2 = ((InterfaceType) ((itf2_E)
                                                .getFcItfType()))
                                                .isFcClientItf();
                                        if (!(isClient2)) {
                                            String itfSign2 = ((InterfaceType) ((itf2_E)
                                                    .getFcItfType()))
                                                    .getFcItfSignature();
                                            if (itfSign2
                                                    .equals("fr.jade.resource.Resource")) {
                                                
                                                System.out
                                                        .println("     > restarting "
                                                                + Fractal
                                                                        .getNameController(
                                                                                clientCmp_E)
                                                                        .getFcName());

                                                // ((Resource) itf2_E).stop();

                                                // ((Resource)
                                                // itf2_E).configure();
                                                // ((Resource)
                                                // itf2_E).loadApp();
                                                // ((Resource) itf2_E).start();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("     > ERROR");
                                    System.out.println(e);
                                }
                            }
                        }// END restart source of binding
                    }
                }
            } catch (Exception e) {
                System.out.println("     > ERROR");
                System.out.println(e);
            }
        }
    }

}
