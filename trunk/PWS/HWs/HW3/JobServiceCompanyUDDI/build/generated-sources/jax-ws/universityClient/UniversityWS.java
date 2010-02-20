
package universityClient;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2-hudson-752-
 * Generated source version: 2.2
 * 
 */
@WebService(name = "universityWS", targetNamespace = "http://university/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface UniversityWS {


    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getDegree", targetNamespace = "http://university/", className = "universityClient.GetDegree")
    @ResponseWrapper(localName = "getDegreeResponse", targetNamespace = "http://university/", className = "universityClient.GetDegreeResponse")
    @Action(input = "http://university/universityWS/getDegreeRequest", output = "http://university/universityWS/getDegreeResponse")
    public String getDegree(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0);

}
