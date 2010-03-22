
package bookpublisherusacallback;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for invoice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="invoice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bookISBN" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="buyer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sum" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "invoice", propOrder = {
    "bookISBN",
    "buyer",
    "sum"
})
public class Invoice {

    protected String bookISBN;
    protected String buyer;
    protected double sum;

    /**
     * Gets the value of the bookISBN property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookISBN() {
        return bookISBN;
    }

    /**
     * Sets the value of the bookISBN property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookISBN(String value) {
        this.bookISBN = value;
    }

    /**
     * Gets the value of the buyer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBuyer() {
        return buyer;
    }

    /**
     * Sets the value of the buyer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBuyer(String value) {
        this.buyer = value;
    }

    /**
     * Gets the value of the sum property.
     * 
     */
    public double getSum() {
        return sum;
    }

    /**
     * Sets the value of the sum property.
     * 
     */
    public void setSum(double value) {
        this.sum = value;
    }

}
