/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package object;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 *
 * @author saibbot
 */
@Entity
@Table(name = "convertionrate")
@NamedQueries({@NamedQuery(name = "ConvertionRate.findAll", query = "SELECT c FROM ConvertionRate c"), @NamedQuery(name = "ConvertionRate.findByCurrencyName", query = "SELECT c FROM ConvertionRate c WHERE c.currencyName = :currencyName"), @NamedQuery(name = "ConvertionRate.findByRateToDollar", query = "SELECT c FROM ConvertionRate c WHERE c.rateToDollar = :rateToDollar")})
public class ConvertionRate implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "currencyName")
    private String currencyName;
    @Basic(optional = false)
    @Column(name = "rateToDollar")
    private double rateToDollar;

    public ConvertionRate() {
    }

    public ConvertionRate(String currencyName) {
        this.currencyName = currencyName;
    }

    public ConvertionRate(String currencyName, double rateToDollar) {
        this.currencyName = currencyName;
        this.rateToDollar = rateToDollar;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public double getRateToDollar() {
        return rateToDollar;
    }

    public void setRateToDollar(double rateToDollar) {
        this.rateToDollar = rateToDollar;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (currencyName != null ? currencyName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ConvertionRate)) {
            return false;
        }
        ConvertionRate other = (ConvertionRate) object;
        if ((this.currencyName == null && other.currencyName != null) || (this.currencyName != null && !this.currencyName.equals(other.currencyName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "object.ConvertionRate[currencyName=" + currencyName + "]";
    }

}
