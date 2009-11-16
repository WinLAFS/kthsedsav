package org.objectweb.jasmine.jade.util;

import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.jasmine.jade.util.UtilityComponentFactory.Marker;

import fr.jade.fractal.api.control.GenericAttributeController;
import fr.jade.fractal.api.control.NoSuchAttributeException;

public class HolderImpl implements ValueAttribute{

	private Object value;

	public Object getValue() {
		return value;
	}

	public void setValue(Object val) {
		this.value = val;
	}


}
