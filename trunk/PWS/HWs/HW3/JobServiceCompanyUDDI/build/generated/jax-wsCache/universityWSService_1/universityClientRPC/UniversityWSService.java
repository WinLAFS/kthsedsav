
package universityClientRPC;

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
@WebServiceClient(name = "universityWSService", targetNamespace = "http://university/", wsdlLocation = "http://localhost:11983/JobServiceCompany/universityWSService?wsdl")
public class UniversityWSService
    extends Service
{

    private final static URL UNIVERSITYWSSERVICE_WSDL_LOCATION;
    private final static WebServiceException UNIVERSITYWSSERVICE_EXCEPTION;
    private final static QName UNIVERSITYWSSERVICE_QNAME = new QName("http://university/", "universityWSService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://localhost:11983/JobServiceCompany/universityWSService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        UNIVERSITYWSSERVICE_WSDL_LOCATION = url;
        UNIVERSITYWSSERVICE_EXCEPTION = e;
    }

    public UniversityWSService() {
        super(__getWsdlLocation(), UNIVERSITYWSSERVICE_QNAME);
    }

    public UniversityWSService(WebServiceFeature... features) {
        super(__getWsdlLocation(), UNIVERSITYWSSERVICE_QNAME, features);
    }

    public UniversityWSService(URL wsdlLocation) {
        super(wsdlLocation, UNIVERSITYWSSERVICE_QNAME);
    }

    public UniversityWSService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, UNIVERSITYWSSERVICE_QNAME, features);
    }

    public UniversityWSService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public UniversityWSService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns UniversityWS
     */
    @WebEndpoint(name = "universityWSPort")
    public UniversityWS getUniversityWSPort() {
        return super.getPort(new QName("http://university/", "universityWSPort"), UniversityWS.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns UniversityWS
     */
    @WebEndpoint(name = "universityWSPort")
    public UniversityWS getUniversityWSPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://university/", "universityWSPort"), UniversityWS.class, features);
    }

    private static URL __getWsdlLocation() {
        if (UNIVERSITYWSSERVICE_EXCEPTION!= null) {
            throw UNIVERSITYWSSERVICE_EXCEPTION;
        }
        return UNIVERSITYWSSERVICE_WSDL_LOCATION;
    }

}
