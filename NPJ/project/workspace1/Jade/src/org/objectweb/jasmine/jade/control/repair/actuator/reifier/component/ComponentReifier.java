/**
 * Jade_self-healing
 * fr.jade.service.repair.actuator.reifier.component
 * ComponentReifier.java
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * Nov 28, 2006
 */
package org.objectweb.jasmine.jade.control.repair.actuator.reifier.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.rmi.registry.NamingService;
import org.objectweb.fractal.util.Fractal;

import fr.jade.reflex.api.control.ReflexController;
import fr.jade.reflex.api.factory.GenericFactoryNotification;
import fr.jade.reflex.util.Reflex;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.AbstractReifier;
import org.objectweb.jasmine.jade.control.repair.actuator.reifier.ReifierException;

/**
 * @author <a href="mailto:julien.legrand@inrialpes.fr">Julien Legrand
 * 
 */
public class ComponentReifier extends AbstractReifier {
    
    /**
     * 
     */
    private NamingService ns;
    
//  ------------------------------------------------------------------------
    // Implementation of BindingController interface
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#bindFc(java.lang.String, java.lang.Object)
     */
    public void bindFc(String clientItfName, Object serverItf)
            throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {

        if (clientItfName.equals("registry"))
            ns = (NamingService) serverItf;
        else
            super.bindFc(clientItfName, serverItf);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        List<String> bindings = new ArrayList<String>(Arrays.asList(super.listFc()));
        bindings.add("registry");
        return bindings.toArray(new String[0]);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if (clientItfName.equals("registry"))
            return ns;
        else
            return super.lookupFc(clientItfName);
    }

    /* (non-Javadoc)
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {

        if (clientItfName.equals("registry"))
            ns = null;
        else
            super.unbindFc(clientItfName);
    }
    
    // ------------------------------------------------------------------------
    // Implementation of Reifier interface
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see fr.jade.service.repair.actuator.reifier.Reifier#propagate(org.objectweb.fractal.api.Component)
     */
    public void propagate(Component metaComponent) throws ReifierException {

        try {
            
            System.out.println("[Component reifier] starting ...");

            GenericFactoryNotification gfNot_E = null;
            
            String metaComponentName = Fractal.getNameController(metaComponent).getFcName();
            metaComponentName += "_factory";
            
            
            System.out.println(metaComponentName);
            Component factory = ns.lookup(metaComponentName);
            gfNot_E = Reflex.getGenericFactoryNotification(factory);
            
            // gfNot_E = (GenericFactoryNotification) _this_weaveableMC
            // .getCmpRef().getFcInterface("generic-notification-factory");
            // } catch (NoSuchInterfaceException nsie) {
            // nsie.printStackTrace();
            
            /*
             * FIXME : content du composant managed_resources
             */

            ContentController cc = Fractal.getContentController(metaComponent);

            for (Component cmp_M : cc.getFcSubComponents()) {
                try {
                    ReflexController rc_M = Reflex.getReflexController(cmp_M);

                    /*
                     * get component type 
                     */
                    Type cmpType_E = cmp_M.getFcType();
                    String cmpCtrlDesc_E = rc_M.getReflexAttribute("controllerDesc");
                    String cmpContent_E = rc_M.getReflexAttribute("contentDesc");

                    System.out.println("contentDesc : " + cmpContent_E);
                    System.out.println("controllerDesc : " + cmpCtrlDesc_E);
                    System.out.println("cmpType_E : ");
                    
                    for (InterfaceType it : ((ComponentType) cmpType_E)
                            .getFcInterfaceTypes()) {
                        System.out.println("\t - " + it.getFcItfName() + "("
                                + it.getFcItfSignature() + ")");
                    }

                    /*
                     * instanciate component
                     */
                    Component cmp_E = gfNot_E.newFcInstanceNotification(
                            extractCmpType((ComponentType) cmpType_E),
                            cmpCtrlDesc_E, cmpContent_E);

                    /*
                     * set the name
                     */
                    Fractal.getNameController(cmp_E).setFcName(
                            Fractal.getNameController(cmp_M).getFcName());

                    /*
                     * re set manually the dual ref
                     */
                    rc_M.setCmpRef(cmp_E);
                    Reflex.getReflexController(cmp_E).setCmpRef(cmp_M);

                } catch (NoSuchInterfaceException e) {
                    throw new ReifierException("Missing required interface ("
                            + e.getMessage() + ")");
                } catch (Exception e) {
                    throw new ReifierException(e);
                }
            }
        } catch (Exception e) {
            throw new ReifierException(e);
        }

    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    /**
     * extract from the meta cmp type the type used to create the component for
     * self-healing
     * 
     * @param t
     * @return
     * @throws Exception
     */
    private ComponentType extractCmpType(ComponentType t) throws Exception {

        String itfName = null;
        InterfaceType[] itRes = null;

        TypeFactory tf = Fractal
                .getTypeFactory(Fractal.getBootstrapComponent());

        InterfaceType[] it = t.getFcInterfaceTypes();

        int j = 0;
        for (int i = 0; i < it.length; i++) {
            itfName = it[i].getFcItfName();

            if (!((itfName.equals("component")) || (itfName
                    .endsWith("-controller")))) {
                j++;
            }
        }

        itRes = new InterfaceType[j];
        j = 0;
        for (int i = 0; i < it.length; i++) {
            itfName = it[i].getFcItfName();

            if (!((itfName.equals("component")) || (itfName
                    .endsWith("-controller")))) {
                itRes[j] = it[i];
                j++;
            }
        }

        return tf.createFcType(itRes);

    }

}
