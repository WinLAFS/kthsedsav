//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.02.06 at 02:38:44 PM CET 
//


package JAXB.CompaniesInfo;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the JAXB.CompaniesInfo package. 
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

    private final static QName _NumberOfEmployees_QNAME = new QName("http://www.kth.se/CompaniesInfo", "numberOfEmployees");
    private final static QName _Line1_QNAME = new QName("http://www.kth.se/CompaniesInfo", "line1");
    private final static QName _Line2_QNAME = new QName("http://www.kth.se/CompaniesInfo", "line2");
    private final static QName _City_QNAME = new QName("http://www.kth.se/CompaniesInfo", "city");
    private final static QName _Country_QNAME = new QName("http://www.kth.se/CompaniesInfo", "country");
    private final static QName _Site_QNAME = new QName("http://www.kth.se/CompaniesInfo", "site");
    private final static QName _Index_QNAME = new QName("http://www.kth.se/CompaniesInfo", "index");
    private final static QName _FoundedYear_QNAME = new QName("http://www.kth.se/CompaniesInfo", "foundedYear");
    private final static QName _CEO_QNAME = new QName("http://www.kth.se/CompaniesInfo", "CEO");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: JAXB.CompaniesInfo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Address }
     * 
     */
    public Address createAddress() {
        return new Address();
    }

    /**
     * Create an instance of {@link Company }
     * 
     */
    public Company createCompany() {
        return new Company();
    }

    /**
     * Create an instance of {@link AddressExt }
     * 
     */
    public AddressExt createAddressExt() {
        return new AddressExt();
    }

    /**
     * Create an instance of {@link Companies }
     * 
     */
    public Companies createCompanies() {
        return new Companies();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "numberOfEmployees")
    public JAXBElement<BigInteger> createNumberOfEmployees(BigInteger value) {
        return new JAXBElement<BigInteger>(_NumberOfEmployees_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "line1")
    public JAXBElement<String> createLine1(String value) {
        return new JAXBElement<String>(_Line1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "line2")
    public JAXBElement<String> createLine2(String value) {
        return new JAXBElement<String>(_Line2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "city")
    public JAXBElement<String> createCity(String value) {
        return new JAXBElement<String>(_City_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "country")
    public JAXBElement<String> createCountry(String value) {
        return new JAXBElement<String>(_Country_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "site")
    public JAXBElement<String> createSite(String value) {
        return new JAXBElement<String>(_Site_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "index")
    public JAXBElement<String> createIndex(String value) {
        return new JAXBElement<String>(_Index_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "foundedYear")
    public JAXBElement<Integer> createFoundedYear(Integer value) {
        return new JAXBElement<Integer>(_FoundedYear_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/CompaniesInfo", name = "CEO")
    public JAXBElement<String> createCEO(String value) {
        return new JAXBElement<String>(_CEO_QNAME, String.class, null, value);
    }

}