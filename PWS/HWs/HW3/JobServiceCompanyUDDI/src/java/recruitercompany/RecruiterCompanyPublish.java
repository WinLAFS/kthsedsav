/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recruitercompany;

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
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

/**
 *
 * @author saibbot
 */
public class RecruiterCompanyPublish {

    String httpProxyHost = "";
    String httpProxyPort = "";
    String httpsProxyHost = "";
    String httpsProxyPort = "";
    String regUrli = "";
    String regUrlp = "";
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

            InternationalString s = blcm.createInternationalString("RecruiterCompany");
            Organization org = blcm.createOrganization(s);
            s = blcm.createInternationalString("Find job opportunities");
            org.setDescription(s);
//------- Create primary contact, set name
            User primaryContact = blcm.createUser();
            PersonName pName = blcm.createPersonName("Vasilis");
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
            EmailAddress emailAddress = blcm.createEmailAddress("jobs@recruiter.org");
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
                    blcm.createInternationalString("Simple Data Processing Services");
            String sv = "514211";
            Classification classification =
                    blcm.createClassification(cScheme, sn, sv);
            Collection<Classification> classifications = new ArrayList<Classification>();
            classifications.add(classification);
// Set organization's classification
            org.addClassifications(classifications);

            Collection<Organization> orgs = new HashSet<Organization>();
            orgs.add(org);

            BulkResponse br = blcm.saveOrganizations(orgs);
            if (br.getStatus() == JAXRResponse.STATUS_SUCCESS) {
                System.out.println("Organization Saved");
                return true;
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
        RecruiterCompanyPublish rc = new RecruiterCompanyPublish();
        System.out.println("ok? :" + rc.publish());
    }
}
