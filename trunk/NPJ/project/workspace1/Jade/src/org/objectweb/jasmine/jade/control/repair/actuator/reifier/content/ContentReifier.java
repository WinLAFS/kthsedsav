/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier.content
 * ContentReifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier.content;

import java.util.Arrays;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.util.Fractal;

import fr.jade.reflex.api.control.ContentNotificationController;
import fr.jade.reflex.util.Reflex;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.AbstractReifier;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.ReifierException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class ContentReifier extends AbstractReifier {

    /*
     * (non-Javadoc)
     * 
     * @see fr.jade.service.repair.actuator.reifier.Reifier#propagate(org.objectweb.fractal.api.Component)
     */
    public void propagate(Component metaComponent) throws ReifierException {

        clientReifier.propagate(metaComponent);

        System.out.println("[Content reifier] start reifying");

        try {
            ContentController cc = Fractal.getContentController(metaComponent);

            for (Component sub_M : cc.getFcSubComponents()) {
                propagateContent(sub_M);
            }
        } catch (Exception e) {
            throw new ReifierException(e);
        }

        System.out.println("[Content reifier] finish to reify");

    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    /**
     * @param cmp_M
     * @throws Exception
     */
    private void propagateContent(Component cmp_M) throws Exception {
        updateSuperComponent(cmp_M);
        updateSubComponent(cmp_M);
    }

    /**
     * @param cmp_M
     * @throws Exception
     */
    private void updateSuperComponent(Component cmp_M) throws Exception {

        Component repairedCmp_E = Reflex.getReflexController(cmp_M).getCmpRef();

        System.out.println("[Content reifier] updating super component(s) of "
                + Fractal.getNameController(repairedCmp_E).getFcName());

        Component[] superCmp_M = Fractal.getSuperController(cmp_M)
                .getFcSuperComponents();

        Component superCmp_E = null;

        if (superCmp_M != null) {

            for (int i = 0; i < superCmp_M.length; i++) {

                /*
                 * super component should have a content controller
                 */
                try {
                    superCmp_E = Reflex.getReflexController(superCmp_M[i])
                            .getCmpRef();

                    ContentNotificationController superCCN_E = Reflex
                            .getContentNotificationController(superCmp_E);
                    
                    System.out.println("updating "
                            + Fractal.getNameController(superCmp_E).getFcName()
                            + " ("
                            + Fractal.getNameController(superCmp_M[i])
                                    .getFcName() + ")");

                    Component subsSuperCmp_E[] =  Fractal.getContentController(superCmp_E).getFcSubComponents();
                    
                    if (!Arrays.asList(subsSuperCmp_E).contains(repairedCmp_E)) {
                        
                        superCCN_E.addFcSubComponentNotification(repairedCmp_E);
                    }

                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * @param cmp_M
     * @throws Exception
     */
    private void updateSubComponent(Component cmp_M) throws Exception {

        Component repairedCmp_E = Reflex.getReflexController(cmp_M).getCmpRef();

        System.out.println("[Content reifier] updating sub component(s) of "
                + Fractal.getNameController(repairedCmp_E).getFcName());

        try {

            ContentNotificationController ccn_E = Reflex
                    .getContentNotificationController(repairedCmp_E);

            ContentController cc_E = Fractal
                    .getContentController(repairedCmp_E);

            Component[] subCmp_M = Fractal.getContentController(cmp_M)
                    .getFcSubComponents();

            for (int i = 0; i < subCmp_M.length; i++) {

                Component subCmp_E = Reflex.getReflexController(subCmp_M[i])
                        .getCmpRef();

                System.out.println("updating "
                        + Fractal.getNameController(subCmp_E).getFcName()
                        + " ("
                        + Fractal.getNameController(subCmp_M[i]).getFcName()
                        + ")");

                if (!Arrays.asList(cc_E.getFcSubComponents())
                        .contains(subCmp_E)) {

                    System.out.println("\t\t ADD");
                    ccn_E.addFcSubComponentNotification(subCmp_E);

                } else {

                    System.out.println("\t\t DON'T ADD");
                }
            }
        } catch (Exception e) {
            System.out.println("Not a composite");
        }
    }

}
