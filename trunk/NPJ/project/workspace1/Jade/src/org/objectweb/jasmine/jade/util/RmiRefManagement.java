package org.objectweb.jasmine.jade.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.jonathan.JContextFactory;
import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.rmi.registry.Registry;
import org.objectweb.jasmine.jade.service.componentdeployment.ComponentDeploymentImpl;
import org.objectweb.jasmine.jade.service.componentdeploymentbackend.ComponentDeploymentBackEndImpl;
import org.objectweb.jonathan.apis.binding.BindException;
import org.objectweb.jonathan.apis.binding.ForwardException;
import org.objectweb.jonathan.apis.binding.Identifier;
import org.objectweb.jonathan.apis.binding.NamingContext;
import org.objectweb.jonathan.apis.kernel.Context;
import org.objectweb.jonathan.apis.kernel.JonathanException;

/**
 * @author <a href="mailto:nikolaos.parlavantzas@inrialpes.fr">Nikos Parlavantzas
 * 
 */
public class RmiRefManagement {
	
	public static Component resolveRef(Ref ref) {
		
		ClassLoader cl = RmiRefManagement.class.getClassLoader();
        Map<String, ClassLoader> hints = new HashMap<String, ClassLoader>();
		hints.put("registry-classloader", cl);
		hints.put("component-classloader", cl);
        Component reg = null;
        NamingContext binder = null;
        Component comp=null;
        Identifier id = null;
        Context hints1 =null; 
        JContextFactory contextFactory=new JContextFactory();
        
		try {
			
			reg = Registry.getRegistryComponent("",Registry.DEFAULT_PORT, hints);
			binder = (NamingContext) reg.getFcInterface("context");	
			id = binder.decode(ref.id, 0, ref.id.length);
	        hints1 = contextFactory.newContext();
	        hints1.addElement("interface_type", String.class, ref.type, (char)0);
	        comp = (Component) id.bind(new Identifier[] {id}, hints1);
			hints1.release();
			
		} catch (Exception e1) {
		
			e1.printStackTrace();
			throw new RuntimeException("Failed to resolve Fractal RMI reference");
		}
        
		return comp;
	
	}

	public static Ref generateRef(Component comp) {
		
		
		Ref ref = new Ref();
		ref.type = comp.getClass().getInterfaces()[0].getName();
		ClassLoader cl = RmiRefManagement.class.getClassLoader();
        Map<String, ClassLoader> hints = new HashMap<String, ClassLoader>();
		hints.put("registry-classloader", cl);
		hints.put("component-classloader", cl);
        Component reg = null;
        NamingContext binder = null;
        try {
			reg = Registry.getRegistryComponent("",Registry.DEFAULT_PORT, hints);
			binder = (NamingContext) reg.getFcInterface("context");	
			ref.id = binder.export(comp, null).encode();		
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new RuntimeException("Failed to generate Fractal RMI reference");
		}
		return ref;
	
	}
}