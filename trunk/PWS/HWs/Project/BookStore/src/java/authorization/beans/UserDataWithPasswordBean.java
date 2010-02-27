/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package authorization.beans;

/**
 *
 * @author saibbot
 */
public class UserDataWithPasswordBean {
    private UserDataBean userDataBean;
    private String password;

    public String getPassword() {
        return password;
    }

    public UserDataBean getUserDataBean() {
        return userDataBean;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserDataBean(UserDataBean userDataBean) {
        this.userDataBean = userDataBean;
    }

    public UserDataWithPasswordBean(UserDataBean userDataBean, String password) {
        this.userDataBean = userDataBean;
        this.password = password;
    }


}
