
package jobServiceClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the jobServiceClient package. 
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

    private final static QName _CreateProfile_QNAME = new QName("http://jobservice/", "createProfile");
    private final static QName _CreateProfileResponse_QNAME = new QName("http://jobservice/", "createProfileResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: jobServiceClient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreateProfileResponse }
     * 
     */
    public CreateProfileResponse createCreateProfileResponse() {
        return new CreateProfileResponse();
    }

    /**
     * Create an instance of {@link CreateProfile }
     * 
     */
    public CreateProfile createCreateProfile() {
        return new CreateProfile();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateProfile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://jobservice/", name = "createProfile")
    public JAXBElement<CreateProfile> createCreateProfile(CreateProfile value) {
        return new JAXBElement<CreateProfile>(_CreateProfile_QNAME, CreateProfile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateProfileResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://jobservice/", name = "createProfileResponse")
    public JAXBElement<CreateProfileResponse> createCreateProfileResponse(CreateProfileResponse value) {
        return new JAXBElement<CreateProfileResponse>(_CreateProfileResponse_QNAME, CreateProfileResponse.class, null, value);
    }

}
