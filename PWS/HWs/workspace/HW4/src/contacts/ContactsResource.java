package contacts;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import contacts.bean.Contact;
import contacts.storage.ContactStore;

@Path("/contacts")
public class ContactsResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	// Reading All objects in the Collection
	@GET
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<Contact> getContacts() {
		List<Contact> contacts = new ArrayList<Contact>();
		contacts.addAll(ContactStore.getStore().values());
		return contacts;
	}

	// Reading a Specific Contract {contact} from Collection
	@Path("{contact}")
	public ContactResource getContact(@PathParam("contact") String contact) {
		return new ContactResource(uriInfo, request, contact);
	}
}