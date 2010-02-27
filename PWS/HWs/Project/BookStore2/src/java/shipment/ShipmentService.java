/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shipment;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.jws.WebService;
import shipment.beans.InvoiceBean;

/**
 *
 * @author saibbot
 */
@WebService()
public class ShipmentService {
    public void orderShipment(String addressSource, String addressDestination,
            double weight, String customerCreditCard) {

        if (weight > 20) {
            return;
        }

        String[] bannedAreas = new String[] {"us", "asia", "france", "greece"};

        for (String area : bannedAreas) {
            if (addressDestination.toLowerCase().indexOf(area) >= 0) {
                return;
            }
        }

        String[] zoneAAreas = new String[] {"sweden", "germany", "finland", "norway", "austria", "latvia"};
        double sourceFactor = 0;
        double destinationFactor = 0;

        for (String area : zoneAAreas) {
            if (addressSource.toLowerCase().indexOf(area) >= 0) {
                sourceFactor = 1;
            }
            if (addressDestination.toLowerCase().indexOf(area) >= 0) {
                destinationFactor = 1;
            }
        }

        sourceFactor = (sourceFactor == 0) ? 2 : 1;
        destinationFactor = (destinationFactor == 0) ? 2 : 1;
        double priceFactor;
        int deliverDatePlus;
        if (sourceFactor == destinationFactor) {
            if (sourceFactor == 1) {
                priceFactor = 1;
                deliverDatePlus = 3;
            }
            else {
                priceFactor = 1.3;
                deliverDatePlus = 5;
            }
        }
        else {
            priceFactor = 1.8;
            deliverDatePlus = 6;
        }
        
        double price = priceFactor * (weight * 5);
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_MONTH, deliverDatePlus);
        Date deliveryDate = calendar.getTime();

        String text = "Sending from " + addressSource + " to " + addressDestination + " item with weight " + weight + " kg.";
        int id = (int) Math.random() * 1000;
        
        InvoiceBean invoiceBean = new InvoiceBean(id, addressDestination, deliveryDate,
                price, "SEK", text);




    }

    public void cancelShipment(String shipmentID) {
        
    }
}
