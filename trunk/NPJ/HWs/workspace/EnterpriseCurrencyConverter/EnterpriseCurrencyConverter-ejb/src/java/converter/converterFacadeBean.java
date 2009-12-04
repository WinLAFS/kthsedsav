/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package converter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import object.ConvertionRate;

/**
 * The EJB that provides that implements the converterFacadeLocal interface.
 */
@Stateless
public class converterFacadeBean implements converterFacadeLocal {
    @PersistenceContext
    EntityManager em;
    
    public double convertCurrency(String fromCurrency, double fromAmount, String toCurrency) {
        ConvertionRate fromCR = em.find(ConvertionRate.class, fromCurrency);
        ConvertionRate toCR = em.find(ConvertionRate.class, toCurrency);

        double fromAmountInDollars = 0.0;
        if (fromCR != null) {
            fromAmountInDollars = fromCR.getRateToDollar() * fromAmount;
        }

        double rateFromDollarToToCurrency = 1.0;
        if (toCR != null) {
            rateFromDollarToToCurrency = 1 / toCR.getRateToDollar();
        }

        double amountInToCurrency = rateFromDollarToToCurrency * fromAmountInDollars;

        return amountInToCurrency;
    }

    public List<String> getAvailableCurrencies() {

        System.out.println(">>>get all available currencies::");
        Query query = em.createNamedQuery("ConvertionRate.findAll");
        List result = query.getResultList();
        
        List<String> finalResult = new ArrayList<String>();

        Iterator<ConvertionRate> crIterator = result.iterator();
        while (crIterator.hasNext()) {
            ConvertionRate convertionRate = crIterator.next();
            System.out.println(">>>adding : " + convertionRate.getCurrencyName());
            finalResult.add(convertionRate.getCurrencyName());
        }

        return finalResult;
    }
 
}
