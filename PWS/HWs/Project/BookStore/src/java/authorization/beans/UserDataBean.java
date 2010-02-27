/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package authorization.beans;

import java.io.Serializable;

/**
 *
 * @author saibbot
 */
public class UserDataBean implements Serializable {
    private String username;
    private String address;
    private String phone;

    public UserDataBean(String username, String address, String phone) {
        this.username = username;
        this.address = address;
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getUsername() {
        return username;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
