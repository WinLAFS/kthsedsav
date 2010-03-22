
package bookpublisherusacallback;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for sellReturnObj complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sellReturnObj">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bookInfo" type="{http://bookpublisherUSAcallback/}book" minOccurs="0"/>
 *         &lt;element name="invoice" type="{http://bookpublisherUSAcallback/}invoice" minOccurs="0"/>
 *         &lt;element name="location" type="{http://bookpublisherUSAcallback/}location" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sellReturnObj", propOrder = {
    "bookInfo",
    "invoice",
    "location"
})
public class SellReturnObj {

    protected Book bookInfo;
    protected Invoice invoice;
    protected Location location;

    /**
     * Gets the value of the bookInfo property.
     * 
     * @return
     *     possible object is
     *     {@link Book }
     *     
     */
    public Book getBookInfo() {
        return bookInfo;
    }

    /**
     * Sets the value of the bookInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Book }
     *     
     */
    public void setBookInfo(Book value) {
        this.bookInfo = value;
    }

    /**
     * Gets the value of the invoice property.
     * 
     * @return
     *     possible object is
     *     {@link Invoice }
     *     
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Sets the value of the invoice property.
     * 
     * @param value
     *     allowed object is
     *     {@link Invoice }
     *     
     */
    public void setInvoice(Invoice value) {
        this.invoice = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link Location }
     *     
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link Location }
     *     
     */
    public void setLocation(Location value) {
        this.location = value;
    }

}
