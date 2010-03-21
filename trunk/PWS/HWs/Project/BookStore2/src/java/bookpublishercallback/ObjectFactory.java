
package bookpublishercallback;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the bookpublishercallback package. 
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

    private final static QName _BookOrderDone_QNAME = new QName("http://bookpublishercallback/", "bookOrderDone");
    private final static QName _BookOrderDoneResponse_QNAME = new QName("http://bookpublishercallback/", "bookOrderDoneResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: bookpublishercallback
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Book }
     * 
     */
    public Book createBook() {
        return new Book();
    }

    /**
     * Create an instance of {@link Invoice }
     * 
     */
    public Invoice createInvoice() {
        return new Invoice();
    }

    /**
     * Create an instance of {@link Location }
     * 
     */
    public Location createLocation() {
        return new Location();
    }

    /**
     * Create an instance of {@link BookOrderDoneResponse }
     * 
     */
    public BookOrderDoneResponse createBookOrderDoneResponse() {
        return new BookOrderDoneResponse();
    }

    /**
     * Create an instance of {@link SellReturnObj }
     * 
     */
    public SellReturnObj createSellReturnObj() {
        return new SellReturnObj();
    }

    /**
     * Create an instance of {@link BookOrderDone }
     * 
     */
    public BookOrderDone createBookOrderDone() {
        return new BookOrderDone();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BookOrderDone }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bookpublishercallback/", name = "bookOrderDone")
    public JAXBElement<BookOrderDone> createBookOrderDone(BookOrderDone value) {
        return new JAXBElement<BookOrderDone>(_BookOrderDone_QNAME, BookOrderDone.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BookOrderDoneResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://bookpublishercallback/", name = "bookOrderDoneResponse")
    public JAXBElement<BookOrderDoneResponse> createBookOrderDoneResponse(BookOrderDoneResponse value) {
        return new JAXBElement<BookOrderDoneResponse>(_BookOrderDoneResponse_QNAME, BookOrderDoneResponse.class, null, value);
    }

}
