package org.objectweb.jasmine.jade.service.nodemanager;

import java.io.Serializable;

final class Allocation {

	private int storageShare;

	private String allocationRef;

	Allocation(int storageShare) {
		this.storageShare = storageShare;
		this.allocationRef = String.valueOf(allocationId++);
	}

	int getStorageShare() {
		return storageShare;
	}
	
	String getAllocationRef() {
		return allocationRef;
	}
	private static int allocationId = 0;
}
