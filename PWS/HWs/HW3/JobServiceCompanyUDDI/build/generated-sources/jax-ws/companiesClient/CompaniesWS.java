
package companiesClient;

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
@WebService(name = "CompaniesWS", targetNamespace = "http://companies/")
@XmlSeeAlso({
    ObjectFactory.class
})
public interface CompaniesWS {


    /**
     * 
     * @param arg0
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getCompanyInfo", targetNamespace = "http://companies/", className = "companiesClient.GetCompanyInfo")
    @ResponseWrapper(localName = "getCompanyInfoResponse", targetNamespace = "http://companies/", className = "companiesClient.GetCompanyInfoResponse")
    @Action(input = "http://companies/CompaniesWS/getCompanyInfoRequest", output = "http://companies/CompaniesWS/getCompanyInfoResponse")
    public String getCompanyInfo(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0);

}
