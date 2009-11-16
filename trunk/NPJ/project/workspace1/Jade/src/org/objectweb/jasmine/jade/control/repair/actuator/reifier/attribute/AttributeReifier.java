/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier.attribute
 * AttributeReifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier.attribute;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.util.Fractal;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.util.FractalUtil;
import fr.jade.reflex.api.control.GenericAttributeNotificationController;
import fr.jade.reflex.util.Reflex;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.AbstractReifier;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.ReifierException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class AttributeReifier extends AbstractReifier {

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

        System.out.println("[Attribute reifier] start reifying");

        try {

            ContentController cc = Fractal.getContentController(metaComponent);

            for (Component sub_M : cc.getFcSubComponents()) {
                propagateAttributes(sub_M);
            }

        } catch (Exception e) {
            throw new ReifierException(e);
        }

        System.out.println("[Attribute reifier] finish to reify");
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    /**
     * @param meta
     * @param repairedCmp
     * @throws Exception
     */
    private void propagateAttributes(Component meta) throws Exception {

        Component repairedCmp = Reflex.getDualComponent(meta);

        try {

            GenericAttributeController gac_M = FractalUtil
                    .getGenericAttributeController(meta);
            GenericAttributeNotificationController gac = Reflex
                    .getGenericAttributeNotificationController(repairedCmp);

            String metaName = null;
            try{
                metaName = Fractal.getNameController(meta).getFcName();
            } catch (Exception ignored) {
                metaName = "Unnamed component";
            }

            String[] attributes = gac_M.listFcAtt();
            String attribute;
            String value;

            if (attributes != null) {

                for (int i = 0; i < attributes.length; i++) {

                    attribute = attributes[i];

                    value = gac_M.getAttribute(attribute);
                    gac.setAttributeNotification(attribute, value);

                    System.out.println("[" + metaName + "] " + attribute
                            + " = " + value);

                }
            }
        } catch (Exception ignored) {/* ignored, the cmp is not parametric */
        }
    }
}
