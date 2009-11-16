package org.objectweb.jasmine.jade.service.nodemanager;

import junit.framework.TestCase;

public class NodeManagerImplTest extends TestCase {

	NodeManagerImpl nm;

	@Override
	protected void setUp() throws Exception {
		nm = new NodeManagerImpl();
		nm.freeStorage = 1000;
	}

	public void testAllocDealloc() {

		printProperties();
		String a = nm.allocate("storageShare=100");
		assertEquals(900, nm.freeStorage);
		printProperties();
		nm.deallocate(a);
		assertEquals(1000, nm.freeStorage);
	}

	public void testOverflow() {

		String a = nm.allocate("storageShare=2000");
		assertNull(a);
		assertEquals(1000, nm.freeStorage);
	}

	public void testMultiple() {

		String a = nm.allocate("storageShare=100");
		String a1 = nm.allocate("storageShare=500");
		assertEquals(400, nm.freeStorage);
		nm.deallocate(a1);
		assertEquals(900, nm.freeStorage);
		nm.deallocate(a);
		assertEquals(1000, nm.freeStorage);
	}
	
	public void testMultipleDealloc() {

		String a = nm.allocate("storageShare=100");
		assertEquals(900, nm.freeStorage);
		nm.deallocate(a);
		assertEquals(1000, nm.freeStorage);
		nm.deallocate(a);
		assertEquals(1000, nm.freeStorage);
	}

	public void printProperties() {
		System.out.println(nm.getPropertiesAsString());
	}
}
