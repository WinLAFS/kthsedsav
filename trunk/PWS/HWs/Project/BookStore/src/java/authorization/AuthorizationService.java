/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package authorization;

import authorization.beans.UserDataBean;
import authorization.beans.UserDataWithPasswordBean;
import authorization.database.UsersDB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author saibbot
 */
@WebService()
public class AuthorizationService {

    /**
     * Web service operation
     */
    public UserDataBean login(String username, String password) {

//        UsersDB usersDB = UsersDB.getInstance();
//        UserDataWithPasswordBean udwpbb = usersDB.getUserData(username);
//        if (udwpbb == null) {
//            return null;
//        }
//        if (udwpbb.getPassword().equals(password)) {
//            return udwpbb.getUserDataBean();
//        }
//        else {
            return null;
//        }
    }

}
