//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.02.06 at 02:39:08 PM CET 
//


package JAXB.CV;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the JAXB.CV package. 
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

    private final static QName _CV_QNAME = new QName("http://www.kth.se/CV", "CV");
    private final static QName _Name_QNAME = new QName("http://www.kth.se/CV", "name");
    private final static QName _Surname_QNAME = new QName("http://www.kth.se/CV", "surname");
    private final static QName _BirthDate_QNAME = new QName("http://www.kth.se/CV", "birthDate");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: JAXB.CV
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BaseData }
     * 
     */
    public BaseData createBaseData() {
        return new BaseData();
    }

    /**
     * Create an instance of {@link BaseDataExtra }
     * 
     */
    public BaseDataExtra createBaseDataExtra() {
        return new BaseDataExtra();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BaseDataExtra }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CV", name = "CV")
    public JAXBElement<BaseDataExtra> createCV(BaseDataExtra value) {
        return new JAXBElement<BaseDataExtra>(_CV_QNAME, BaseDataExtra.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CV", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CV", name = "surname")
    public JAXBElement<String> createSurname(String value) {
        return new JAXBElement<String>(_Surname_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CV", name = "birthDate")
    public JAXBElement<XMLGregorianCalendar> createBirthDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_BirthDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

}