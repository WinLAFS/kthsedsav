package org.objectweb.jasmine.jade.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.util.Fractal;

import dks.niche.ids.SNR;

public class TestSimpleFractalComponentFactory {

	Component holder;
	Component marker;
	Object initial;
	Component mapHolder;
	
	@Before
	public void setUp() throws Exception {
		initial= (Object) "hello";
		holder= UtilityComponentFactory.createHolderComponent(initial);
		marker= UtilityComponentFactory.createMarkerComponent("aMarker");
		Map map = new HashMap(); 
		map.put("name", "value");
		mapHolder = UtilityComponentFactory.createHolderComponent(map);
		
	}

	
	@Test
	public void testCreateMarkerComponent() {
		
		try {
			Object itf = marker.getFcInterface("aMarker");
		} catch (NoSuchInterfaceException e) {
			fail();
		}
	}
	
	@Test
	public void testCreateHolderComponentRead() {
		
		
		Object content = null;
		Object content2 = null;
		
		try {
			content = ((ValueAttribute) Fractal.getAttributeController(holder)).getValue();
			content2 = ((ValueAttribute) Fractal.getAttributeController(mapHolder)).getValue();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		assertEquals((String)content, "hello");
		assertEquals(((Map) content2).get("name"), "value");
	}
	
	@Test
	public void testCreateHolderComponentWrite() {
		
		Object content = null;
		try {
			((ValueAttribute) Fractal.getAttributeController(holder)).setValue((Object)"test");
			content = ((ValueAttribute) (Fractal.getAttributeController(holder))).getValue();
		} catch (NoSuchInterfaceException e) {
			e.printStackTrace();
		}
		assertEquals((String)content, "test");
	}
}

	