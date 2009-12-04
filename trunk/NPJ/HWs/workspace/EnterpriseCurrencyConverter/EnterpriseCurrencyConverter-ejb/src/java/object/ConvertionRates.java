/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package object;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * ConvertionsRates is the entity object that defines the schema for storing
 * the convertion rate into the database.
 */
@Entity
public class ConvertionRates implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    private double rateToDollar;

    /**
     * Gets the rate of the currency to dollar; the value that
     * one unit of the current currency should be multiplied,
     * so we can get one dollar.
     *
     * @return the rate
     */
    public double getRateToDollar() {
        return rateToDollar;
    }

    /**
     * Sets the rateToDoller
     *
     * @param rtd the new rate
     */
    public void setRateToDollar(double rtd) {
        this.rateToDollar = rtd;
    }

    /**
     * Gets the name of the currency, that is also the Primary key
     * of the currency into the database.
     *
     * @return the name of the currency
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the name of the currency
     *
     * @param id the new name of the currency
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ConvertionRates)) {
            return false;
        }
        ConvertionRates other = (ConvertionRates) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "object.ConvertionRates[id=" + id + "]";
    }

}
