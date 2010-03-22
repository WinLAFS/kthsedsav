
package bookpublisherusacallback;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.6 in JDK 6
 * Generated source version: 2.1
 * 
 */
@WebServiceClient(name = "PublisherUSAWSCallBackService", targetNamespace = "http://bookpublisherUSAcallback/", wsdlLocation = "file:/D:/KTH/Projects/PWS/HWs/Project/BookStore2/src/java/bookpublisherUSAcallback/wsdl/PublisherUSAWSCallBackService.wsdl")
public class PublisherUSAWSCallBackService
    extends Service
{

    private final static URL PUBLISHERUSAWSCALLBACKSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(bookpublisherusacallback.PublisherUSAWSCallBackService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = bookpublisherusacallback.PublisherUSAWSCallBackService.class.getResource(".");
            url = new URL(baseUrl, "file:/D:/KTH/Projects/PWS/HWs/Project/BookStore2/src/java/bookpublisherUSAcallback/wsdl/PublisherUSAWSCallBackService.wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'file:/D:/KTH/Projects/PWS/HWs/Project/BookStore2/src/java/bookpublisherUSAcallback/wsdl/PublisherUSAWSCallBackService.wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        PUBLISHERUSAWSCALLBACKSERVICE_WSDL_LOCATION = url;
    }

    public PublisherUSAWSCallBackService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public PublisherUSAWSCallBackService() {
        super(PUBLISHERUSAWSCALLBACKSERVICE_WSDL_LOCATION, new QName("http://bookpublisherUSAcallback/", "PublisherUSAWSCallBackService"));
    }

    /**
     * 
     * @return
     *     returns PublisherUSAWSCallBack
     */
    @WebEndpoint(name = "PublisherUSAWSCallBackPort")
    public PublisherUSAWSCallBack getPublisherUSAWSCallBackPort() {
        return super.getPort(new QName("http://bookpublisherUSAcallback/", "PublisherUSAWSCallBackPort"), PublisherUSAWSCallBack.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns PublisherUSAWSCallBack
     */
    @WebEndpoint(name = "PublisherUSAWSCallBackPort")
    public PublisherUSAWSCallBack getPublisherUSAWSCallBackPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://bookpublisherUSAcallback/", "PublisherUSAWSCallBackPort"), PublisherUSAWSCallBack.class, features);
    }

}
