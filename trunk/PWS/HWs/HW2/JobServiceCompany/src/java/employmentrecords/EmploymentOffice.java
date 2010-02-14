/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package employmentrecords;

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
    public String getEmploymentRecord(@WebParam(name = "name")
    String name) {
        
        String xml = "";
        //try {
    
//            FileInputStream fip = new FileInputStream(name);
     System.out.println("sa");
            /*} catch (FileNotFoundException ex) {
            return ex.getMessage();
            }*/
        return xml;
    }

}
