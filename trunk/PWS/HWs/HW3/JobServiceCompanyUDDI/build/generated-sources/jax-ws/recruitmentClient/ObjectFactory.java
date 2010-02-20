
package recruitmentClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the recruitmentClient package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _FindJobs_QNAME = new QName("http://recruitercompany/", "findJobs");
    private final static QName _FindJobsResponse_QNAME = new QName("http://recruitercompany/", "findJobsResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: recruitmentClient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link FindJobsResponse }
     * 
     */
    public FindJobsResponse createFindJobsResponse() {
        return new FindJobsResponse();
    }

    /**
     * Create an instance of {@link FindJobs }
     * 
     */
    public FindJobs createFindJobs() {
        return new FindJobs();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindJobs }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://recruitercompany/", name = "findJobs")
    public JAXBElement<FindJobs> createFindJobs(FindJobs value) {
        return new JAXBElement<FindJobs>(_FindJobs_QNAME, FindJobs.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindJobsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://recruitercompany/", name = "findJobsResponse")
    public JAXBElement<FindJobsResponse> createFindJobsResponse(FindJobsResponse value) {
        return new JAXBElement<FindJobsResponse>(_FindJobsResponse_QNAME, FindJobsResponse.class, null, value);
    }

}
