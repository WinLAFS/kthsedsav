/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bookpublisher.objects;

/**
 *
 * @author Shum
 */
public class Location {
    private String country;
    private String city;
    private String address;
    private String postCode;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    
}
