/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jobservice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.BusinessLifeCycleManager;
import javax.xml.registry.BusinessQueryManager;
import javax.xml.registry.Connection;
import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.FindQualifier;
import javax.xml.registry.JAXRException;
import javax.xml.registry.LifeCycleManager;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;

/**
 *
 * @author saibbot
 */
public class ServicesFetch {

    BusinessQueryManager bqm = null;
    BusinessLifeCycleManager blcm = null;

    public ServicesFetch() {
        try {
            Properties connProps = new Properties();
            connProps.setProperty("javax.xml.registry.queryManagerURL", "http://localhost:8080/RegistryServer/");
            connProps.setProperty("javax.xml.registry.lifeCycleManagerURL", "http://localhost:8080/RegistryServer/");
            connProps.setProperty("javax.xml.registry.factoryClass", "com.sun.xml.registry.uddi.ConnectionFactoryImpl");
            // ------------------- Set Connection Factory -------------------
            ConnectionFactory factory = ConnectionFactory.newInstance();
            factory.setProperties(connProps);
            Connection conn = factory.createConnection();
            // ---------------- Getting Registry service Object ---------------
            RegistryService rs = conn.getRegistryService();
            bqm = rs.getBusinessQueryManager();
            blcm = rs.getBusinessLifeCycleManager();
        } catch (JAXRException ex) {
            Logger.getLogger(ServicesFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String fetchRecruiterService() {
        String ret = null;
        try {
            // Define find qualifiers and name patterns
            Collection<String> findQualifiers = new ArrayList<String>();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection<String> namePatterns = new ArrayList<String>();
            // qString refers to the organization name we are looking for
            namePatterns.add("%" + "RecruiterCompany" + "%");
            // Find orgs with names that matches qString
            BulkResponse response = bqm.findOrganizations(findQualifiers, namePatterns, null, null, null, null);

            //Iterate over discovered organizations and collect their service binding
            Collection orgs = response.getCollection();
            Iterator orgIter = orgs.iterator();
            while (orgIter.hasNext()) {
                Organization org = (Organization) orgIter.next();
                System.out.println("Found org: " + org.getDescription() + "\n\t with services:");
                Collection services = org.getServices();
                Iterator svcIter = services.iterator();
                while (svcIter.hasNext()) {
                    Service svc = (Service) svcIter.next();
                    Collection serviceBindings = svc.getServiceBindings();
                    Iterator sbIter = serviceBindings.iterator();
                    while (sbIter.hasNext()) {
                        ServiceBinding sb = (ServiceBinding) sbIter.next();
                        System.out.println("    : " + sb.getAccessURI());
                        ret = sb.getAccessURI();
//                        System.out.println("        : " +

                    }

                }
            }
            return ret;

        } catch (JAXRException ex) {
            Logger.getLogger(ServicesFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public String fetchUniversityService() {
        String ret = null;
        try {
            // Define find qualifiers and name patterns
            Collection<String> findQualifiers = new ArrayList<String>();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection<String> namePatterns = new ArrayList<String>();
            // qString refers to the organization name we are looking for
            namePatterns.add("%" + "University" + "%");
            // Find orgs with names that matches qString
            BulkResponse response = bqm.findOrganizations(findQualifiers, namePatterns, null, null, null, null);

            //Iterate over discovered organizations and collect their service binding
            Collection orgs = response.getCollection();
            Iterator orgIter = orgs.iterator();
            while (orgIter.hasNext()) {
                Organization org = (Organization) orgIter.next();
                System.out.println("Found org: " + org.getDescription() + "\n\t with services:");
                Collection services = org.getServices();
                Iterator svcIter = services.iterator();
                while (svcIter.hasNext()) {
                    Service svc = (Service) svcIter.next();
                    Collection serviceBindings = svc.getServiceBindings();
                    Iterator sbIter = serviceBindings.iterator();
                    while (sbIter.hasNext()) {
                        ServiceBinding sb = (ServiceBinding) sbIter.next();
                        System.out.println("    : " + sb.getAccessURI());
                        ret = sb.getAccessURI();
//                        System.out.println("        : " +

                    }

                }
            }
            return ret;

        } catch (JAXRException ex) {
            Logger.getLogger(ServicesFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public String fetchEmploymentOfficeService() {
        String ret = null;
        try {
            // NAICS classification(taxonomy) uuid
            String uuid_naics = "uuid:C0B9FE13-179F-413D-8A5B-5004DB8E5BB2";
            ClassificationScheme cScheme = (ClassificationScheme) bqm.getRegistryObject(uuid_naics,
                    LifeCycleManager.CLASSIFICATION_SCHEME);
            /** we are looking for Simple Data Processing Services    with
            514211 identifier in NAICS taxonomy
             */
            InternationalString sn = blcm.createInternationalString("Simple Data Processing Services");

            String sv = "514211";
            Classification classification = blcm.createClassification(cScheme, sn, sv);

            Collection<Classification> classifications = new ArrayList<Classification>();
            classifications.add(classification);

            // Define find qualifiers and name patterns
            Collection<String> findQualifiers = new ArrayList<String>();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection<String> namePatterns = new ArrayList<String>();
            // qString refers to the organization name we are looking for
            namePatterns.add("%" + "EmploymentOffice" + "%");
            // Find orgs with names that matches qString
            BulkResponse response = bqm.findOrganizations(findQualifiers, namePatterns, classifications, null, null, null);

            //Iterate over discovered organizations and collect their service binding
            Collection orgs = response.getCollection();
            Iterator orgIter = orgs.iterator();
            while (orgIter.hasNext()) {
                Organization org = (Organization) orgIter.next();
                System.out.println("Found org: " + org.getDescription() + "\n\t with services:");
                Collection services = org.getServices();
                Iterator svcIter = services.iterator();
                while (svcIter.hasNext()) {
                    Service svc = (Service) svcIter.next();
                    Collection serviceBindings = svc.getServiceBindings();
                    Iterator sbIter = serviceBindings.iterator();
                    while (sbIter.hasNext()) {
                        ServiceBinding sb = (ServiceBinding) sbIter.next();
                        System.out.println("    : " + sb.getAccessURI());
                        ret = sb.getAccessURI();
//                        System.out.println("        : " +

                    }

                }
            }
            return ret;

        } catch (JAXRException ex) {
            Logger.getLogger(ServicesFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

     public String fetchCompaniesService() {
        String ret = null;
        try {
            // Define find qualifiers and name patterns
            Collection<String> findQualifiers = new ArrayList<String>();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection<String> namePatterns = new ArrayList<String>();
            // qString refers to the organization name we are looking for
            namePatterns.add("%" + "CompaniesDatabase" + "%");
            // Find orgs with names that matches qString
            BulkResponse response = bqm.findOrganizations(findQualifiers, namePatterns, null, null, null, null);

            //Iterate over discovered organizations and collect their service binding
            Collection orgs = response.getCollection();
            Iterator orgIter = orgs.iterator();
            while (orgIter.hasNext()) {
                Organization org = (Organization) orgIter.next();
                System.out.println("Found org: " + org.getDescription() + "\n\t with services:");
                Collection services = org.getServices();
                Iterator svcIter = services.iterator();
                while (svcIter.hasNext()) {
                    Service svc = (Service) svcIter.next();
                    Collection serviceBindings = svc.getServiceBindings();
                    Iterator sbIter = serviceBindings.iterator();
                    while (sbIter.hasNext()) {
                        ServiceBinding sb = (ServiceBinding) sbIter.next();
                        System.out.println("    : " + sb.getAccessURI());
                        ret = sb.getAccessURI();
//                        System.out.println("        : " +

                    }

                }
            }
            return ret;

        } catch (JAXRException ex) {
            Logger.getLogger(ServicesFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

      public String fetchJobService() {
        String ret = null;
        try {
            // Define find qualifiers and name patterns
            Collection<String> findQualifiers = new ArrayList<String>();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection<String> namePatterns = new ArrayList<String>();
            // qString refers to the organization name we are looking for
            namePatterns.add("%" + "JobService" + "%");
            // Find orgs with names that matches qString
            BulkResponse response = bqm.findOrganizations(findQualifiers, namePatterns, null, null, null, null);

            //Iterate over discovered organizations and collect their service binding
            Collection orgs = response.getCollection();
            Iterator orgIter = orgs.iterator();
            while (orgIter.hasNext()) {
                Organization org = (Organization) orgIter.next();
                System.out.println("Found org: " + org.getDescription() + "\n\t with services:");
                Collection services = org.getServices();
                Iterator svcIter = services.iterator();
                while (svcIter.hasNext()) {
                    Service svc = (Service) svcIter.next();
                    Collection serviceBindings = svc.getServiceBindings();
                    Iterator sbIter = serviceBindings.iterator();
                    while (sbIter.hasNext()) {
                        ServiceBinding sb = (ServiceBinding) sbIter.next();
                        System.out.println("    : " + sb.getAccessURI());
                        ret = sb.getAccessURI();
//                        System.out.println("        : " +

                    }

                }
            }
            return ret;

        } catch (JAXRException ex) {
            Logger.getLogger(ServicesFetch.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static void main(String[] args) {
        ServicesFetch sf = new ServicesFetch();
        sf.fetchUniversityService();
        System.out.println("==========");
        sf.fetchRecruiterService();
        System.out.println("==========");
        sf.fetchEmploymentOfficeService();
        System.out.println("==========");
        sf.fetchCompaniesService();
        System.out.println("==========");
        sf.fetchJobService();
    }
}
