
package recruitmentClient;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2-hudson-752-
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "RecruiterCompanyService", targetNamespace = "http://recruitercompany/", wsdlLocation = "http://localhost:11983/JobServiceCompany/RecruiterCompanyService?wsdl")
public class RecruiterCompanyService
    extends Service
{

    private final static URL RECRUITERCOMPANYSERVICE_WSDL_LOCATION;
    private final static WebServiceException RECRUITERCOMPANYSERVICE_EXCEPTION;
    private final static QName RECRUITERCOMPANYSERVICE_QNAME = new QName("http://recruitercompany/", "RecruiterCompanyService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:11983/JobServiceCompany/RecruiterCompanyService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        RECRUITERCOMPANYSERVICE_WSDL_LOCATION = url;
        RECRUITERCOMPANYSERVICE_EXCEPTION = e;
    }

    public RecruiterCompanyService() {
        super(__getWsdlLocation(), RECRUITERCOMPANYSERVICE_QNAME);
    }

    public RecruiterCompanyService(WebServiceFeature... features) {
        super(__getWsdlLocation(), RECRUITERCOMPANYSERVICE_QNAME, features);
    }

    public RecruiterCompanyService(URL wsdlLocation) {
        super(wsdlLocation, RECRUITERCOMPANYSERVICE_QNAME);
    }

    public RecruiterCompanyService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, RECRUITERCOMPANYSERVICE_QNAME, features);
    }

    public RecruiterCompanyService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RecruiterCompanyService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns RecruiterCompany
     */
    @WebEndpoint(name = "RecruiterCompanyPort")
    public RecruiterCompany getRecruiterCompanyPort() {
        return super.getPort(new QName("http://recruitercompany/", "RecruiterCompanyPort"), RecruiterCompany.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns RecruiterCompany
     */
    @WebEndpoint(name = "RecruiterCompanyPort")
    public RecruiterCompany getRecruiterCompanyPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://recruitercompany/", "RecruiterCompanyPort"), RecruiterCompany.class, features);
    }

    private static URL __getWsdlLocation() {
        if (RECRUITERCOMPANYSERVICE_EXCEPTION!= null) {
            throw RECRUITERCOMPANYSERVICE_EXCEPTION;
        }
        return RECRUITERCOMPANYSERVICE_WSDL_LOCATION;
    }

}
