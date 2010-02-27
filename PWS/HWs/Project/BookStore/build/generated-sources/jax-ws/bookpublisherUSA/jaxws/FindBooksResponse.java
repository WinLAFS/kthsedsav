
package bookpublisherUSA.jaxws;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import bookpublisher.objects.Book;

@XmlRootElement(name = "findBooksResponse", namespace = "http://bookpublisherUSA/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findBooksResponse", namespace = "http://bookpublisherUSA/")
public class FindBooksResponse {

    @XmlElement(name = "return", namespace = "")
    private List<Book> _return;

    /**
     * 
     * @return
     *     returns List<Book>
     */
    public List<Book> getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(List<Book> _return) {
        this._return = _return;
    }

}
