/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package university;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 *
 * @author saibbot
 */
@WebService()
public class KTHUniversityService {
/**
     * Web service operation
     */
    @WebMethod(operationName = "getDegree")
    public String getDegree(String surname) {
        try {
            InputStreamReader is = new InputStreamReader(this.getClass().getResourceAsStream("/university/KTH/"+surname+".xml"));
            BufferedReader in = new BufferedReader(is);
            StringBuffer str = new StringBuffer();
            String s;
            while ((s = in.readLine()) != null) {
                str.append(s).append("\n");
            }
            return str.toString();
        } catch (Exception ex) {
            Logger.getLogger(universityWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Error";
    }
}
