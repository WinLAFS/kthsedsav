/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bookpublisher;

import bookpublisher.BooksDB.BooksDB;
import bookpublisher.objects.Book;
import bookpublisher.objects.CreditCard;
import bookpublisher.objects.Invoice;
import bookpublisher.objects.Location;
import bookpublisher.objects.SellReturnObj;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebService;

/**
 *
 * @author Shum
 */
@WebService()
public class PublisherWS {

    public List<Book> findBooks(String title, String author, String ISBN) {
        //TODO add logic

        if (title == null) {
            title = "";
        }
        if (author == null) {
            author = "";
        }
        if (ISBN == null) {
            ISBN = "";
        }
        ArrayList<Book> books = null;
        books = BooksDB.getInstance().getBooks(title, author, ISBN);

        return books;
    }

    public SellReturnObj sellBook(String bookISBN, CreditCard card) {
        //TODO implement logic

        Invoice invoice = new Invoice();
        invoice.setBookISBN(bookISBN);
        //invoice.setBuyer(card.getHolderName());
        invoice.setBuyer("Shum");
        invoice.setSum(100);

        Book book = new Book();
        book.setAuthor("Author2");
        book.setISBN(bookISBN);
        book.setTitle("Title2");

        Location location = new Location();
        location.setAddress("bla bla");
        location.setCity("Kista");
        location.setCountry("Sweden");
        location.setPostCode("11122");

        SellReturnObj sro = new SellReturnObj();
        sro.setBookInfo(book);
        sro.setInvoice(invoice);
        sro.setLocation(location);

        return sro;
    }

    public boolean purchaseCancel(String bookISBN, CreditCard card) {

          try {
            FileOutputStream fout = new FileOutputStream("order");
            // Print a line of text
            // Close our output stream
            new PrintStream(fout).println("");
            fout.close();

        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public void orderBookPurchase(String bookISBN, CreditCard card, Location location) {
        try {
            //TODO
            FileOutputStream fout = new FileOutputStream("order");
            // Print a line of text

            String txt = bookISBN;
            txt += "|" + card.getHolderName();
            ArrayList<Book> books = BooksDB.getInstance().getBooks("", "", bookISBN);
            Book book = books.get(0);
            txt += "|" + book.getAuthor();
            txt += "|" + book.getTitle();
            txt += "|" + book.getPrice();
            txt += "|" + location.getAddress();
            txt += "|" + location.getCity();
            txt += "|" + location.getCountry();
            txt += "|" + location.getPostCode();
            new PrintStream(fout).println(txt);
            // Close our output stream
            fout.close();

        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public SellReturnObj bookOrderDone() {
        try {

            // Open an input stream
            FileInputStream fin = new FileInputStream("order");
            // Close our input stream
            SellReturnObj sro = new SellReturnObj();

            String txt = new DataInputStream(fin).readLine();
            StringTokenizer st = new StringTokenizer(txt, "|");

            Book book = new Book();
            String bookISBN = st.nextToken();
            book.setISBN(bookISBN);
            Invoice invoice = new Invoice();
            invoice.setBookISBN(bookISBN);
            invoice.setBuyer(st.nextToken());
            sro.setInvoice(invoice);
            book.setAuthor(st.nextToken());
            book.setTitle(st.nextToken());
            book.setPrice(new Double(st.nextToken()));
            sro.setBookInfo(book);
            Location location = new Location();
            location.setAddress(st.nextToken());
            location.setCity(st.nextToken());
            location.setCountry(st.nextToken());
            location.setPostCode(st.nextToken());
            sro.setLocation(location);
            
            fin.close();
            
            return sro;
        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
