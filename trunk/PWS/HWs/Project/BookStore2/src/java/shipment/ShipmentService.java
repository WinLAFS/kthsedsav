/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shipment;

import bookpublisher.PublisherWS;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.api.message.Headers;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import shipment.beans.InvoiceBean;
import shipmentcallback.ShipmentServiceCallback;
import shipmentcallback.ShipmentServiceCallbackService;

/**
 *
 * @author saibbot
 */
@WebService()
public class ShipmentService {

    private static final String NS_ADDRESSING_2003 =
            "http://schemas.xmlsoap.org/ws/2003/03/addressing";
    /** header : reply to. */
    private static final String HEADER_REPLYTO = "ReplyTo";
    /** header : address. */
    private static final String HEADER_ADDRESS = "Address";
    /** header : message id. */
    private static final String HEADER_MESSAGEID = "MessageID";
    /** header : relates to. */
    private static final String HEADER_RELATESTO = "RelatesTo";

    public void orderShipment(String addressSource, String addressDestination,
            double weight, String customerCreditCard) {

        if (weight > 20) {
            return;
        }

        String[] bannedAreas = new String[]{"asia", "france", "greece"};

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
            txt += "|" + invoiceBean.getDeliveryDate().toGMTString();
            txt += "|" + invoiceBean.getId();
            txt += "|" + invoiceBean.getIssueDate().toGMTString();
            txt += "|" + invoiceBean.getPrice();
            new PrintStream(fout).println(txt);
            fout.close();

            (new Thread() {

                public void run() {
                    //callback code
                    String address = "http://localhost:11987/BookStoreComposite2Service7/casaPort6";
                    ShipmentServiceCallbackService srv = new ShipmentServiceCallbackService();
                    ShipmentServiceCallback portType = srv.getShipmentServiceCallbackPort();
                    WSBindingProvider bp = (WSBindingProvider) portType;

                    bp.setAddress(address);
                    bp.setOutboundHeaders(Headers.create(new QName(NS_ADDRESSING_2003, HEADER_RELATESTO), "11"));

                    try {


                        // Open an input stream
                        FileInputStream fin = new FileInputStream("shipment");

                        String txt = new DataInputStream(fin).readLine();
                        StringTokenizer st = new StringTokenizer(txt, "|");
                        shipmentcallback.InvoiceBean ib = new shipmentcallback.InvoiceBean();
                        ib.setCurrency(st.nextToken());
                        ib.setDestinationAddress(st.nextToken());
                        ib.setText(st.nextToken());

                        GregorianCalendar gc1 = (GregorianCalendar) GregorianCalendar.getInstance();
                        gc1.setTime(new Date(st.nextToken()));
                        XMLGregorianCalendarImpl xgc1 = new XMLGregorianCalendarImpl(gc1);
                        ib.setDeliveryDate(xgc1);

                        //ib.setDeliveryDate(new GregorianCalendar().setTime(new Date(st.nextToken())));
                        ib.setId(new Integer(st.nextToken()));

                        GregorianCalendar gc2 = (GregorianCalendar) GregorianCalendar.getInstance();
                        gc2.setTime(new Date(st.nextToken()));
                        XMLGregorianCalendarImpl xgc2 = new XMLGregorianCalendarImpl(gc2);
                        ib.setIssueDate(xgc2);

                        //ib.setIssueDate(new Date(st.nextToken()));
                        ib.setPrice(new Double(st.nextToken()));

                        fin.close();

                        System.out.println("shipment===========================1" + address);

                        portType.shipmentDone(ib);
                        System.out.println("shipment===========================2" + address);
                    } catch (IOException ex) {
                        Logger.getLogger(ShipmentService.class.getName()).log(Level.SEVERE, null, ex);

                    }
                }
            }).start();


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
