/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package companies;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 *
 * @author Shum
 */
@WebService()
public class CompaniesWS {
    /**
     * Web service operation
     */
    @WebMethod(operationName = "getCompanyInfo")
    public String getCompanyInfo(String company) {
        try {
            InputStreamReader is = new InputStreamReader(this.getClass().getResourceAsStream("/companies/"+company+".xml"));
            BufferedReader in = new BufferedReader(is);
            StringBuffer str = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                str.append(s).append("\n");
            }
            return str.toString();
        } catch (Exception ex) {
            Logger.getLogger(CompaniesWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Error";
    }
}
