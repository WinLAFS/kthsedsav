/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package shipment.beans;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author saibbot
 */
public class InvoiceBean {
    private int id;
    private String destinationAddress;
    private Date issueDate;
    private Date deliveryDate;
    private Double price;
    private String currency;
    private String text;

    public String getCurrency() {
        return currency;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public Double getPrice() {
        return price;
    }

    public String getText() {
        return text;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    

    public InvoiceBean() {
    }

    public InvoiceBean(int id, String destinationAddress, Date deliveryDate, Double price, String currency, String text) {
        this.id = id;
        this.destinationAddress = destinationAddress;
        this.deliveryDate = deliveryDate;
        this.price = price;
        this.currency = currency;
        this.text = text;
        this.issueDate = Calendar.getInstance().getTime();
    }


}
