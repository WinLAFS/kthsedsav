
package shipmentcallback;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the shipmentcallback package. 
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

    private final static QName _ShipmentDone_QNAME = new QName("http://shipmentcallback/", "shipmentDone");
    private final static QName _ShipmentDoneResponse_QNAME = new QName("http://shipmentcallback/", "shipmentDoneResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: shipmentcallback
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ShipmentDoneResponse }
     * 
     */
    public ShipmentDoneResponse createShipmentDoneResponse() {
        return new ShipmentDoneResponse();
    }

    /**
     * Create an instance of {@link ShipmentDone }
     * 
     */
    public ShipmentDone createShipmentDone() {
        return new ShipmentDone();
    }

    /**
     * Create an instance of {@link InvoiceBean }
     * 
     */
    public InvoiceBean createInvoiceBean() {
        return new InvoiceBean();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ShipmentDone }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://shipmentcallback/", name = "shipmentDone")
    public JAXBElement<ShipmentDone> createShipmentDone(ShipmentDone value) {
        return new JAXBElement<ShipmentDone>(_ShipmentDone_QNAME, ShipmentDone.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ShipmentDoneResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://shipmentcallback/", name = "shipmentDoneResponse")
    public JAXBElement<ShipmentDoneResponse> createShipmentDoneResponse(ShipmentDoneResponse value) {
        return new JAXBElement<ShipmentDoneResponse>(_ShipmentDoneResponse_QNAME, ShipmentDoneResponse.class, null, value);
    }

}
