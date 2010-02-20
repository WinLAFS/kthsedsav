/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package companies;

import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;
import javax.xml.registry.JAXRResponse;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

/**
 *
 * @author Shum
 */
public class CompaniesPublish {

    String httpProxyHost = "";
    String httpProxyPort = "";
    String httpsProxyHost = "";
    String httpsProxyPort = "";
    String regUrli = "";
    String regUrlp = "";
    Boolean ret = false;
//    String username = "";
//    String password = "";
    private static final String QUERY_URL = "query.url";
    private static final String PUBLISH_URL = "publish.url";
    private static final String USER_NAME = "user.name";
    private static final String USER_PASSWORD = "user.password";
    private static final String PROXY_HOST = "http.proxy.host";
    private static final String PROXY_PORT = "http.proxy.port";

    public boolean publish() {
        try {
            /*Properties properties = new Properties();
            properties.setProperty("query.url", "http://localhost:8080/RegistryServer/");
            properties.setProperty("publish.url", "http://localhost:8080/RegistryServer/");
            properties.setProperty("user.name", "testuser");
            properties.setProperty("user.password", "testuser");*/
            Properties connProps = new Properties();
            connProps.setProperty("javax.xml.registry.queryManagerURL",
                    "http://localhost:8080/RegistryServer/");
            connProps.setProperty("javax.xml.registry.lifeCycleManagerURL",
                    "http://localhost:8080/RegistryServer/");
            connProps.setProperty("javax.xml.registry.factoryClass",
                    "com.sun.xml.registry.uddi.ConnectionFactoryImpl");



            // ------------------- Set Connection Factory -------------------

            ConnectionFactory factory = ConnectionFactory.newInstance();
            factory.setProperties(connProps);
            Connection conn = factory.createConnection();

// ---------------- Getting Registry service Object ---------------
            RegistryService rs = conn.getRegistryService();
            BusinessQueryManager bqm = rs.getBusinessQueryManager();
            BusinessLifeCycleManager blcm = rs.getBusinessLifeCycleManager();

            String username = "testuser";
            String password = "testuser";
// Get authorization from the registry

            PasswordAuthentication passwdAuth =
                    new PasswordAuthentication(username, password.toCharArray());
            HashSet<PasswordAuthentication> creds =
                    new HashSet<PasswordAuthentication>();
            creds.add(passwdAuth);
            conn.setCredentials(creds);

            //------- Define Name and Description

            InternationalString s = blcm.createInternationalString("CompaniesDatabase");
            Organization org = blcm.createOrganization(s);
            s = blcm.createInternationalString("Provides companies info");
            org.setDescription(s);
//------- Create primary contact, set name
            User primaryContact = blcm.createUser();
            PersonName pName = blcm.createPersonName("Shum");
            primaryContact.setPersonName(pName);
//-------- Set primary contact phone number
            TelephoneNumber tNum = blcm.createTelephoneNumber();
            tNum.setNumber("(08) 111-1111");
            Collection<TelephoneNumber> phoneNums = new ArrayList<TelephoneNumber>();
            phoneNums.add(tNum);
            primaryContact.setTelephoneNumbers(phoneNums);

            PostalAddress address = blcm.createPostalAddress("546789", "One USA Place", "Washington", "DC", "USA", "02140", "");
            Collection postalAddresses = new ArrayList();
            postalAddresses.add(address);

            Collection emailAddresses = new ArrayList();
            EmailAddress emailAddress = blcm.createEmailAddress("companies@database.org");
            emailAddresses.add(emailAddress);


            primaryContact.setPersonName(pName);
            primaryContact.setPostalAddresses(postalAddresses);
            primaryContact.setEmailAddresses(emailAddresses);
            primaryContact.setTelephoneNumbers(phoneNums);

            org.setPrimaryContact(primaryContact);

            // Set classification scheme (Taxonomy) to NAICS
            ClassificationScheme cScheme =
                    bqm.findClassificationSchemeByName(null, "ntis-gov:naics:1997");
// Create and add classification
            InternationalString sn =
                    blcm.createInternationalString("Simple Companies info services");
            String sv = "514211";
            Classification classification =
                    blcm.createClassification(cScheme, sn, sv);
            Collection<Classification> classifications = new ArrayList<Classification>();
            classifications.add(classification);
// Set organization's classification
            org.addClassifications(classifications);

            Collection<Service> services = new ArrayList<Service>();
            InternationalString istr = blcm.createInternationalString("Company info service");
            Service service = blcm.createService(istr);
            istr = blcm.createInternationalString("Company info service description");
            service.setDescription(istr);

            //set providing organization
            service.setProvidingOrganization(org);

            // Create service bindings
            Collection<ServiceBinding> serviceBindings = new ArrayList<ServiceBinding>();
            ServiceBinding binding = blcm.createServiceBinding();
            istr = blcm.createInternationalString("Company info service binding "
                    + "Description");
            binding.setDescription(istr);
// allow us to publish a fictitious URI without an error
            binding.setValidateURI(false);
            binding.setAccessURI("http://localhost:11983/JobServiceCompany/CompaniesWSService?wsdl");
            serviceBindings.add(binding);
// Add service bindings to service
            service.addServiceBindings(serviceBindings);
// Add service to services, then add services to organization
            services.add(service);

            //--------- Create Concept and as an External Link ----------------
            Concept specConcept;
            specConcept = blcm.createConcept(null, "CompanyDataConcept", "");
            InternationalString str = blcm.createInternationalString("Concept description for company info service");
            specConcept.setDescription(str);
            ExternalLink wsdlLink = blcm.createExternalLink("http://localhost:11983/JobServiceCompany/CompaniesWSService?wsdl",
                    "RectuiyerWSDL document");

            specConcept.addExternalLink(wsdlLink);

            /*--- Find the uddi-org:types classification scheme define by
            the UDDI specification, using well-known key id.*/
            String uuid_types = "uuid:C1ACF26D-9672-4404-9D70-39B756E62AB4";
            ClassificationScheme uddiOrgTypes = (ClassificationScheme) bqm.getRegistryObject(uuid_types, LifeCycleManager.CLASSIFICATION_SCHEME);
            /*---Create a classification, specifying the scheme and the
            taxonomy name and value defined for WSDL documents by the UDDI
            specification.*/
            Classification wsdlSpecClassification =
                    blcm.createClassification(uddiOrgTypes, "wsdlSpec", "wsdlSpec");
            specConcept.addClassification(wsdlSpecClassification);

// Define classifications
            Collection<Concept> concepts = new ArrayList<Concept>();
            concepts.add(specConcept);
// Save Concept
            BulkResponse concResponse = blcm.saveConcepts(concepts);

            String conceptKeyId = null;
            Collection concExceptions = concResponse.getExceptions();
            //   Retrieve the (assigned ) Key from save concept
            Key concKey = null;
            if (concExceptions == null) {
                System.out.println("WSDL Specification Concept saved");
                Collection<Key> keys = concResponse.getCollection();
                Iterator<Key> keyIter = keys.iterator();
                if (keyIter.hasNext()) {
                    concKey = keyIter.next();
                    conceptKeyId = concKey.getId();
                    System.out.println("Concept key is " + conceptKeyId);
                }
            }
// Retrieve the concept from Registry
            Concept retSpecConcept =
                    (Concept) bqm.getRegistryObject(conceptKeyId, LifeCycleManager.CONCEPT);

            // Associate     concept to Binding object
            SpecificationLink retSpeclLink =
                    blcm.createSpecificationLink();
            retSpeclLink.setSpecificationObject(retSpecConcept);
            binding.addSpecificationLink(retSpeclLink);


            org.addServices(services);

            Collection<Organization> orgs = new HashSet<Organization>();
            orgs.add(org);

            BulkResponse br = blcm.saveOrganizations(orgs);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                System.out.println("Organization Saved");
                return true;
//                ret = true;
            } else {
                System.err.println("One or more JAXRExceptions "
                        + "occurred during the save operation:");
                Collection exceptions = br.getExceptions();
                Iterator iter = exceptions.iterator();
                while (iter.hasNext()) {
                    Exception e = (Exception) iter.next();
                    System.err.println(e.toString());
                }
                return false;
            }

        } catch (JAXRException ex) {
            System.err.println(ex.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        CompaniesPublish cp = new CompaniesPublish();
        System.out.println("ok? :" + cp.publish());
    }
}
