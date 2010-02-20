
package universityClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the universityClient package. 
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

    private final static QName _GetDegreeResponse_QNAME = new QName("http://university/", "getDegreeResponse");
    private final static QName _GetDegree_QNAME = new QName("http://university/", "getDegree");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: universityClient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetDegree }
     * 
     */
    public GetDegree createGetDegree() {
        return new GetDegree();
    }

    /**
     * Create an instance of {@link GetDegreeResponse }
     * 
     */
    public GetDegreeResponse createGetDegreeResponse() {
        return new GetDegreeResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDegreeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://university/", name = "getDegreeResponse")
    public JAXBElement<GetDegreeResponse> createGetDegreeResponse(GetDegreeResponse value) {
        return new JAXBElement<GetDegreeResponse>(_GetDegreeResponse_QNAME, GetDegreeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetDegree }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://university/", name = "getDegree")
    public JAXBElement<GetDegree> createGetDegree(GetDegree value) {
        return new JAXBElement<GetDegree>(_GetDegree_QNAME, GetDegree.class, null, value);
    }

}
