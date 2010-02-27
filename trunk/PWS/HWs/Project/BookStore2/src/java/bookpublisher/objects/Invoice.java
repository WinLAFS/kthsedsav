/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bookpublisher.objects;

/**
 *
 * @author Shum
 */
public class Invoice {
    private double sum;
    private String bookISBN;
    private String buyer;

    public String getBookISBN() {
        return bookISBN;
    }

    public void setBookISBN(String bookISBN) {
        this.bookISBN = bookISBN;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    
}
