
package authorization.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import authorization.beans.UserDataBean;

@XmlRootElement(name = "loginResponse", namespace = "http://authorization/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "loginResponse", namespace = "http://authorization/")
public class LoginResponse {

    @XmlElement(name = "return", namespace = "")
    private UserDataBean _return;

    /**
     * 
     * @return
     *     returns UserDataBean
     */
    public UserDataBean getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(UserDataBean _return) {
        this._return = _return;
    }

}
