/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package employmentrecords;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author saibbot
 */
@WebService()
public class EmploymentOffice {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "getEmploymentRecord")
    public String getEmploymentRecord(@WebParam(name = "name") String name) {

        try {
            InputStreamReader is = new InputStreamReader(this.getClass().getResourceAsStream("/employmentrecords/" + name + ".xml"));
            BufferedReader in = new BufferedReader(is);
            StringBuffer str = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                str.append(s).append("\n");
            }
            
            return str.toString();
        } catch (Exception ex) {
            return "Error";
        }

    }
}
