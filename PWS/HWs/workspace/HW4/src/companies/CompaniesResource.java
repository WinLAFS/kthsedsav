package companies;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import companies.beans.Company;
import companies.storage.CompanyStore;
import contacts.bean.Address;
import contacts.bean.Contact;
import contacts.storage.ContactStore;

@Path("/companies")
public class CompaniesResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	
	// Reading All objects in the Collection
	@GET
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Company> getCompanies() {
		List<Company> companies = new ArrayList<Company>();
		companies.addAll(CompanyStore.getStore().values());
		return companies;
	}

	// Reading a Specific Contract {contact} from Collection
	//TODO
	@Path("{company}")
	public CompanyResource getCompany(@PathParam("company") String company) {
		return new CompanyResource(uriInfo, request, company);
	}	
	
	@GET
	@Path("count")
	@Produces(MediaType.TEXT_PLAIN)
	public String getCount() {
		int count = CompanyStore.getStore().size();
		return String.valueOf(count);
	}
	
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void newContact(
			@FormParam("name") String name,
			@FormParam("site") String site,
			@FormParam("address") String address,
			@FormParam("phone") String phone,
			@Context HttpServletResponse servletResponse
	) throws IOException {
		Company c = new Company(name, site, address, phone);
		CompanyStore.getStore().put(name, c);
		
		URI uri = uriInfo.getAbsolutePathBuilder().path(name).build();
		Response.created(uri).build();
		
		servletResponse.sendRedirect("/HW4/ok.html");
	}
}
