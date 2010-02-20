/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package recruitercompany.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jobservice.ServicesFetch;
import recruitmentClient.RecruiterCompany;
import recruitmentClient.RecruiterCompanyService;

/**
 *
 * @author saibbot
 */
public class RecruiterCompanyTest {

    public static void main(String[] argv) {
        try {
            ServicesFetch sf = new ServicesFetch();
            URL url = new URL(sf.fetchRecruiterService());
            //        URL url = getWSDLURL("http://localhost:11983/JobServiceCompanyUDDI/RecruiterCompanyService?wsdl");
            RecruiterCompanyService service1 = new RecruiterCompanyService(url);
            RecruiterCompany s1 = service1.getRecruiterCompanyPort();
            List<String> keyw = new ArrayList<String>();
            keyw.add("Software");
            keyw.add("Greece");
            System.out.println(s1.findJobs(keyw));
        } catch (MalformedURLException ex) {
            Logger.getLogger(RecruiterCompanyTest.class.getName()).log(Level.SEVERE, null, ex);
        }

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
