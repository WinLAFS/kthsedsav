package companies;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import com.sun.jersey.api.NotFoundException;
import companies.beans.Company;
import companies.storage.CompanyStore;

import contacts.util.ParamUtil;

public class CompanyResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String company;
	
	public CompanyResource(UriInfo uriInfo, Request request, String contact) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.company = contact;
	}
	
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Company getCompany() {
		Company comp = CompanyStore.getStore().get(company);
		if(comp == null)
			throw new NotFoundException("No such Company.");
		return comp;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response putCompany(JAXBElement<Company> jaxbContact) {
		Company c = jaxbContact.getValue();
		return putAndGetResponse(c);
	}
	
	@PUT
	public Response putContact(@Context HttpHeaders headers, byte[] in) {
		Map<String,String> params = ParamUtil.parse(new String(in));
		Company c = new Company(params.get("name"), params.get("site"), params.get("address"), params.get("phone"));
		return putAndGetResponse(c);
	}
	
	private Response putAndGetResponse(Company c) {
		Response res;
		if(CompanyStore.getStore().containsKey(c.getName())) {
			res = Response.noContent().build();
		} else {
			res = Response.created(uriInfo.getAbsolutePath()).build();
		}
		CompanyStore.getStore().put(c.getName(), c);
		return res;
	}
	
	@DELETE
	public void deleteContact() {
		Company c = CompanyStore.getStore().remove(company);
		if(c==null)
			throw new NotFoundException("No such Company.");
	}
}
