
package employmentClient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the employmentClient package. 
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

    private final static QName _GetEmploymentRecord_QNAME = new QName("http://employmentrecords/", "getEmploymentRecord");
    private final static QName _GetEmploymentRecordResponse_QNAME = new QName("http://employmentrecords/", "getEmploymentRecordResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: employmentClient
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetEmploymentRecord }
     * 
     */
    public GetEmploymentRecord createGetEmploymentRecord() {
        return new GetEmploymentRecord();
    }

    /**
     * Create an instance of {@link GetEmploymentRecordResponse }
     * 
     */
    public GetEmploymentRecordResponse createGetEmploymentRecordResponse() {
        return new GetEmploymentRecordResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEmploymentRecord }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://employmentrecords/", name = "getEmploymentRecord")
    public JAXBElement<GetEmploymentRecord> createGetEmploymentRecord(GetEmploymentRecord value) {
        return new JAXBElement<GetEmploymentRecord>(_GetEmploymentRecord_QNAME, GetEmploymentRecord.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetEmploymentRecordResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://employmentrecords/", name = "getEmploymentRecordResponse")
    public JAXBElement<GetEmploymentRecordResponse> createGetEmploymentRecordResponse(GetEmploymentRecordResponse value) {
        return new JAXBElement<GetEmploymentRecordResponse>(_GetEmploymentRecordResponse_QNAME, GetEmploymentRecordResponse.class, null, value);
    }

}
