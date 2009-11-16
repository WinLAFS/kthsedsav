package org.objectweb.jasmine.jade.util;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.rmi.io.Ref;
import org.objectweb.fractal.util.Fractal;

/**
 * @author Nikos parlavantzas
 * 
 */
public class UtilityComponentFactory {

	public interface Marker{}
	/**
	 * Creates a marker component that has a server interface of a specific name
	 * 
	 * @param name
	 * @return the marker component
	 */
	public static Component createMarkerComponent(String name) {
		
		
		
		Component result = null;

		try {

			Component boot = Fractal.getBootstrapComponent();

			TypeFactory tf = (TypeFactory) boot.getFcInterface("type-factory");

			ComponentType cType = tf.createFcType(new InterfaceType[] { tf
					.createFcItfType(name, Marker.class.getName(), false, false, false) });

			GenericFactory gf = (GenericFactory) boot
					.getFcInterface("generic-factory");
			result = gf.newFcInstance(cType, "primitive", null);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Creates a component with a "value" attribute (interface ValueAttribute) initialised a given object.
	 * 
	 * @param value
	 * @return the holder component
	 */
	public static Component createHolderComponent(Object value) {
		Component result = null;
		try {

			Component boot = Fractal.getBootstrapComponent();

			TypeFactory tf = (TypeFactory) boot.getFcInterface("type-factory");

			ComponentType cType = tf.createFcType(new InterfaceType[] { 
					tf.createFcItfType("attribute-controller", ValueAttribute.class.getName(), false, false, false)
			});
			
			GenericFactory gf = (GenericFactory) boot
					.getFcInterface("generic-factory");

			result = gf.newFcInstance(cType, "parametricprimitive", HolderImpl.class.getName() );
			((ValueAttribute) Fractal.getAttributeController(result)).setValue(value);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		return result;
	}
}

