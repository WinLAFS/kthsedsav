/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package converter;

import java.util.List;
import javax.ejb.Local;

/**
 * The interface of the converter EJB
 */
@Local
public interface converterFacadeLocal {
    /**
     * The method that is used to convert a certain amount from a currency to
     * another. It is using the data stored into the database in order to
     * do the convertion.
     *
     * @param fromCurrency the currency that we will convert
     * @param fromAmount the amount that we will convert from the fromCurrency
     *          to the toCurrency
     * @param toCurrency the target currency of the convertion
     * @return the converted amount
     */
    public double convertCurrency(String fromCurrency, double fromAmount, String toCurrency);

    /**
     * The method used to fetch all the available currency names from the database.
     *
     * @return a List of the available currency names
     */
    public List<String> getAvailableCurrencies();
}
