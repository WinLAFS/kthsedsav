/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package university.client;

import java.net.MalformedURLException;
import java.net.URL;
import universityClient.UniversityWS;
import universityClient.UniversityWSService;

/**
 *
 * @author Shum
 */
public class UniversityTestClient {
    public static void main(String[] argv ){
        URL url = getWSDLURL("http://localhost:11983/JobServiceCompanyUDDI/universityWSService?wsdl");
        UniversityWSService service1 = new UniversityWSService(url);
        UniversityWS s1 = service1.getUniversityWSPort();

        System.out.println(s1.getDegree("Melene"));
    }

    private static URL getWSDLURL(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return url;
    }
}
