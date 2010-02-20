//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.02.14 at 09:04:38 PM CET 
//


package jaxbgenerated.profile;

import java.math.BigInteger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the jaxbgenerated.profile package. 
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

    private final static QName _Name_QNAME = new QName("http://www.kth.se/profile", "name");
    private final static QName _Subject_QNAME = new QName("http://www.kth.se/profile", "subject");
    private final static QName _StartYear_QNAME = new QName("http://www.kth.se/profile", "startYear");
    private final static QName _CourseID_QNAME = new QName("http://www.kth.se/profile", "courseID");
    private final static QName _University_QNAME = new QName("http://www.kth.se/profile", "university");
    private final static QName _EndYear_QNAME = new QName("http://www.kth.se/profile", "endYear");
    private final static QName _GradeVal_QNAME = new QName("http://www.kth.se/profile", "gradeVal");
    private final static QName _BirthDate_QNAME = new QName("http://www.kth.se/profile", "birthDate");
    private final static QName _Surname_QNAME = new QName("http://www.kth.se/profile", "surname");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: jaxbgenerated.profile
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Grade }
     * 
     */
    public Grade createGrade() {
        return new Grade();
    }

    /**
     * Create an instance of {@link Degrees }
     * 
     */
    public Degrees createDegrees() {
        return new Degrees();
    }

    /**
     * Create an instance of {@link Grades }
     * 
     */
    public Grades createGrades() {
        return new Grades();
    }

    /**
     * Create an instance of {@link Records }
     * 
     */
    public Records createRecords() {
        return new Records();
    }

    /**
     * Create an instance of {@link Degree }
     * 
     */
    public Degree createDegree() {
        return new Degree();
    }

    /**
     * Create an instance of {@link Records.Record }
     * 
     */
    public Records.Record createRecordsRecord() {
        return new Records.Record();
    }

    /**
     * Create an instance of {@link Profile }
     * 
     */
    public Profile createProfile() {
        return new Profile();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "subject")
    public JAXBElement<String> createSubject(String value) {
        return new JAXBElement<String>(_Subject_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "startYear")
    public JAXBElement<XMLGregorianCalendar> createStartYear(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_StartYear_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigInteger }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "courseID")
    public JAXBElement<BigInteger> createCourseID(BigInteger value) {
        return new JAXBElement<BigInteger>(_CourseID_QNAME, BigInteger.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "university")
    public JAXBElement<String> createUniversity(String value) {
        return new JAXBElement<String>(_University_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "endYear")
    public JAXBElement<XMLGregorianCalendar> createEndYear(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_EndYear_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "gradeVal")
    public JAXBElement<Integer> createGradeVal(Integer value) {
        return new JAXBElement<Integer>(_GradeVal_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "birthDate")
    public JAXBElement<XMLGregorianCalendar> createBirthDate(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_BirthDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.kth.se/profile", name = "surname")
    public JAXBElement<String> createSurname(String value) {
        return new JAXBElement<String>(_Surname_QNAME, String.class, null, value);
    }

}
