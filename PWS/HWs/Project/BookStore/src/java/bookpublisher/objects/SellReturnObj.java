/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bookpublisher.objects;

/**
 *
 * @author Shum
 */
public class SellReturnObj {
    private Invoice invoice;
    private Book bookInfo;
    private Location location;

    public Book getBookInfo() {
        return bookInfo;
    }

    public void setBookInfo(Book bookInfo) {
        this.bookInfo = bookInfo;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }


}
