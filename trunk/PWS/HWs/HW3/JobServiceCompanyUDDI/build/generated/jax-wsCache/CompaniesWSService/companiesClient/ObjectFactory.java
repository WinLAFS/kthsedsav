
package companiesClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the companiesClient package. 
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

    private final static QName _GetCompanyInfoResponse_QNAME = new QName("http://companies/", "getCompanyInfoResponse");
    private final static QName _GetCompanyInfo_QNAME = new QName("http://companies/", "getCompanyInfo");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: companiesClient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCompanyInfo }
     * 
     */
    public GetCompanyInfo createGetCompanyInfo() {
        return new GetCompanyInfo();
    }

    /**
     * Create an instance of {@link GetCompanyInfoResponse }
     * 
     */
    public GetCompanyInfoResponse createGetCompanyInfoResponse() {
        return new GetCompanyInfoResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCompanyInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://companies/", name = "getCompanyInfoResponse")
    public JAXBElement<GetCompanyInfoResponse> createGetCompanyInfoResponse(GetCompanyInfoResponse value) {
        return new JAXBElement<GetCompanyInfoResponse>(_GetCompanyInfoResponse_QNAME, GetCompanyInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCompanyInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://companies/", name = "getCompanyInfo")
    public JAXBElement<GetCompanyInfo> createGetCompanyInfo(GetCompanyInfo value) {
        return new JAXBElement<GetCompanyInfo>(_GetCompanyInfo_QNAME, GetCompanyInfo.class, null, value);
    }

}
