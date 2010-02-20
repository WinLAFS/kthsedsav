/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jobservice.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import jobServiceClient.JobServiceService;
import jobServiceClient.JobService;
import jobservice.ServicesFetch;
import universityClient.UniversityWS;
import universityClient.UniversityWSService;

/**
 *
 * @author Shum
 */
public class JobServiceTestClientKTH {

    public static void main(String[] argv) {
        try {
            ServicesFetch sf = new ServicesFetch();
            URL url = new URL(sf.fetchJobService());
//            URL url = getWSDLURL("http://localhost:11983/JobServiceCompanyUDDI/JobServiceService?wsdl");
            JobServiceService service1 = new JobServiceService(url);
            JobService s1 = service1.getJobServicePort();
            InputStreamReader is = new InputStreamReader(s1.getClass().getResourceAsStream("/jobservice/client/CV_KTH.xml"));
            BufferedReader in = new BufferedReader(is);
            StringBuffer str = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                str.append(s).append("\n");
            }

            System.out.println(s1.createProfile(str.toString()));
        } catch (IOException ex) {
            Logger.getLogger(JobServiceTestClientKTH.class.getName()).log(Level.SEVERE, null, ex);
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
