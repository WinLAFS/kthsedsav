/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipment;

import bookpublisher.PublisherWS;
import bookpublisher.objects.Book;
import bookpublisher.objects.Invoice;
import bookpublisher.objects.Location;
import bookpublisher.objects.SellReturnObj;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
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

        String[] bannedAreas = new String[]{"us", "asia", "france", "greece"};

        for (String area : bannedAreas) {
            if (addressDestination.toLowerCase().indexOf(area) >= 0) {
                return;
            }
        }

        String[] zoneAAreas = new String[]{"sweden", "germany", "finland", "norway", "austria", "latvia"};
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
            } else {
                priceFactor = 1.3;
                deliverDatePlus = 5;
            }
        } else {
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

        try {
            FileOutputStream fout = new FileOutputStream("shipment");
            String txt = invoiceBean.getCurrency();
            txt += "|" + invoiceBean.getDestinationAddress();
            txt += "|" + invoiceBean.getText();
            txt += "|" + invoiceBean.getDeliveryDate();
            txt += "|" + invoiceBean.getId();
            txt += "|" + invoiceBean.getIssueDate();
            txt += "|" + invoiceBean.getPrice();
            new PrintStream(fout).println(txt);
            fout.close();

        } catch (IOException ex) {
            Logger.getLogger(ShipmentService.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    public void cancelShipment(String shipmentID) {
        try {
            FileOutputStream fout = new FileOutputStream("shipment");
            new PrintStream(fout).println("");
            fout.close();

        } catch (IOException ex) {
            Logger.getLogger(ShipmentService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public InvoiceBean shipmentDone(String shipmentID) {
        try {

            // Open an input stream
            FileInputStream fin = new FileInputStream("shipment");

            String txt = new DataInputStream(fin).readLine();
            StringTokenizer st = new StringTokenizer(txt, "|");
            InvoiceBean ib = new InvoiceBean();
            ib.setCurrency(st.nextToken());
            ib.setDestinationAddress(st.nextToken());
            ib.setText(st.nextToken());
            ib.setDeliveryDate(new Date(st.nextToken()));
            ib.setId(new Integer(st.nextToken()));
            ib.setIssueDate(new Date(st.nextToken()));
            ib.setPrice(new Double(st.nextToken()));

            fin.close();

            return ib;
        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}