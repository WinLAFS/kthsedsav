package companies;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import companies.beans.Company;
import companies.storage.CompanyStore;

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
//	@Path("{company}")
//	public ContactResource getCompany(@PathParam("company") String contact) {
//		return new ContactResource(uriInfo, request, company);
//	}	
}
