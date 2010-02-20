/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package companies.client;

import companiesClient.CompaniesWS;
import companiesClient.CompaniesWSService;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Shum
 */
public class CompaniesTestClient {
    public static void main(String[] argv ){
        URL url = getWSDLURL("http://localhost:11983/JobServiceCompany/CompaniesWSService?wsdl");
        CompaniesWSService service1 = new CompaniesWSService(url);
        CompaniesWS s1 = service1.getCompaniesWSPort();

        System.out.println(s1.getCompanyInfo("IBM"));
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
