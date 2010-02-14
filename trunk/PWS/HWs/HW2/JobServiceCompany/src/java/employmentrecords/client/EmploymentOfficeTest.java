/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employmentrecords.client;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author saibbot
 */
public class EmploymentOfficeTest {

    public static void main(String[] argv) {
        URL url = getWSDLURL("http://localhost:8080/JobServiceCompany/EmploymentOfficeService?wsdl");
        EmploymentOfficeService service1 = new EmploymentOfficeService(url);
        EmploymentOffice s1 = service1.getEmploymentOfficePort();

        System.out.println(s1.getEmploymentRecord("EmploymentRecord"));
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
