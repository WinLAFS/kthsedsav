package companies.client;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

import companies.beans.Company;

import contacts.bean.Contact;

public class CompaniesClient {
	public static void main(String[] args) {
		Client c = Client.create();
		WebResource r = c.resource("http://localhost:8080/HW4/rest/companies");
		
		System.out.println("===== Get IBM =====");
		getOneCompany(r, "IBM");

		System.out.println("===== Create HP =====");
		postForm(r, "hp.com", "HP", "bla bla", "62317468");
//		

		Company company = new Company("Microsoft", "Microsoft.com", "address here", "123123");
//		
		System.out.println("===== Create Microsoft=====");
		putOneCompany(r, company);
//		
		System.out.println("===== All Companies =====");
		getCompanies(r);
		
		
//		
		System.out.println("===== Delete Microsoft =====");
		deleteOneCompany(r, "Microsoft");
//		
		System.out.println("===== All Companies =====");
		getCompanies(r);
	}
	
	public static void getCompanies(WebResource r) {
		
		// 1, get response as plain text
		String jsonRes = r.accept(MediaType.APPLICATION_JSON).get(String.class);
//		System.out.println(jsonRes);
		
		String xmlRes = r.accept(MediaType.APPLICATION_XML).get(String.class);
//		System.out.println(xmlRes);
		
		// 2, get response and headers etc, wrapped in ClientResponse
		ClientResponse response = r.get(ClientResponse.class);
//		System.out.println( response.getStatus() );
//		System.out.println( response.getHeaders().get("Content-Type") );
		String entity = response.getEntity(String.class);
//		System.out.println(entity);
		
		// 3, get JAXB response
		GenericType<List<Company>> genericType = new GenericType<List<Company>>() {};
		List<Company> companies = r.accept(MediaType.APPLICATION_XML).get(genericType);
		System.out.println("No. of Contacts: " + companies.size());
		for(Company c: companies){
			System.out.println(c.getSite() + ": " + c.getName());
		}
		
	}
	
	public static void getOneCompany(WebResource r, String name) {
		GenericType<JAXBElement<Company>> generic = new GenericType<JAXBElement<Company>>() {};
		JAXBElement<Company> jaxbContact = r.path(name).accept(MediaType.APPLICATION_XML).get(generic);
		Company contact = jaxbContact.getValue();
		System.out.println(contact.getSite() + ": " + contact.getName());
	}
	
	public static void postForm(WebResource r, String site, String name, String address, String phone) {
		Form form = new Form();
		form.add("name", name);
		form.add("site", site);
		form.add("address", address);
		form.add("phone", phone);
		ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED)
								   .post(ClientResponse.class, form);
		System.out.println(response.getEntity(String.class));
	}
	
	public static void putOneCompany(WebResource r, Company c) {
		ClientResponse response = r.path(c.getName()).accept(MediaType.APPLICATION_XML)
								   .put(ClientResponse.class, c);
//		System.out.println(response.getStatus());
	}
	
	public static void deleteOneCompany(WebResource r, String name) {
		ClientResponse response = r.path(name).delete(ClientResponse.class);
	}
}
