
package bookpublisherUSA.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import bookpublisher.objects.CreditCard;

@XmlRootElement(name = "sellBook", namespace = "http://bookpublisherUSA/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sellBook", namespace = "http://bookpublisherUSA/", propOrder = {
    "arg0",
    "arg1"
})
public class SellBook {

    @XmlElement(name = "arg0", namespace = "")
    private String arg0;
    @XmlElement(name = "arg1", namespace = "")
    private CreditCard arg1;

    /**
     * 
     * @return
     *     returns String
     */
    public String getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

    /**
     * 
     * @return
     *     returns CreditCard
     */
    public CreditCard getArg1() {
        return this.arg1;
    }

    /**
     * 
     * @param arg1
     *     the value for the arg1 property
     */
    public void setArg1(CreditCard arg1) {
        this.arg1 = arg1;
    }

}
