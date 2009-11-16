package org.objectweb.jasmine.jade.util;

import org.objectweb.fractal.api.control.AttributeController;



public interface ValueAttribute extends AttributeController {

	public void setValue(Object value);
	public Object getValue();
}
