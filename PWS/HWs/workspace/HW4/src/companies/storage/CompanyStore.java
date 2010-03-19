package companies.storage;

import java.util.HashMap;
import java.util.Map;

import companies.beans.Company;

public class CompanyStore {
	private static Map<String, Company> store;
	private static CompanyStore instance = null;
	
	private CompanyStore() {
		store = new HashMap<String, Company>();
		initOneContact();
	}
	
	public static Map<String, Company> getStore() {
		if(instance==null) {
			instance = new CompanyStore();
		}
		return store;
	}
	
	private static void initOneContact() {
		
		Company c1 = new Company("IBM", "www.ibm.com", "Kistagrossen 2", "00123003123");
		Company c2 = new Company("Oracle", "www.oracle.com", "Kistagrossen 3", "00123213123");
		Company c3 = new Company("Sun", "www.oracle.com", "Kistagrossen 4", "00123123399");
		
		store.put(c1.getName(), c1);
		store.put(c2.getName(), c2);
		store.put(c3.getName(), c3);
	}
}
