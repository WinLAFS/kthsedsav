/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package authorization.database;

import authorization.beans.UserDataBean;
import authorization.beans.UserDataWithPasswordBean;
import java.util.HashMap;

/**
 *
 * @author saibbot
 */
public class UsersDB {
    private static UsersDB instance;

    private HashMap<String, UserDataWithPasswordBean> users;

    private UsersDB() {
        users = new HashMap<String, UserDataWithPasswordBean>();
        UserDataBean u1 = new UserDataBean("user1", "addr 1", "0707999991");
        UserDataWithPasswordBean up1 = new UserDataWithPasswordBean(u1, "pass1");
        users.put(u1.getUsername(), up1);
        UserDataBean u2 = new UserDataBean("user2", "addr 2", "0707999992");
        UserDataWithPasswordBean up2 = new UserDataWithPasswordBean(u2, "pass2");
        users.put(u2.getUsername(), up2);
        UserDataBean u3 = new UserDataBean("user3", "addr 3", "0707999993");
        UserDataWithPasswordBean up3 = new UserDataWithPasswordBean(u3, "pass3");
        users.put(u3.getUsername(), up3);
        UserDataBean u4 = new UserDataBean("user4", "addr 4", "0707999994");
        UserDataWithPasswordBean up4 = new UserDataWithPasswordBean(u4, "pass4");
        users.put(u4.getUsername(), up4);
    }

    public static UsersDB getInstance() {
        if (instance == null) {
            instance = new UsersDB();
        }
        return instance;
    }

    public UserDataWithPasswordBean getUserData(String username) {
         return users.get(username);
    }

}
