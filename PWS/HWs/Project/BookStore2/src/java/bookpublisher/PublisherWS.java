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
import bookpublishercallback.PublisherWSCallBack;
import bookpublishercallback.PublisherWSCallBackService;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.api.message.Headers;
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
import javax.xml.namespace.QName;

/**
 *
 * @author Shum
 */
@WebService()
public class PublisherWS {

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

    public void purchaseCancel(String bookISBN, CreditCard card) {

          try {
            FileOutputStream fout = new FileOutputStream("order1");
            // Print a line of text
            // Close our output stream
            new PrintStream(fout).println("");
            fout.close();

        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void orderBookPurchase(String bookISBN, CreditCard card, Location location) {
        try {
            //TODO
            FileOutputStream fout = new FileOutputStream("order1");
            // Print a line of text

            if(card==null)
                card = new CreditCard();
            if(location==null)
                location=new Location();
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

            //callback code
             String address = "http://localhost:11985/BookStoreComposite2Service4/casaPort3";
             PublisherWSCallBackService srv = new PublisherWSCallBackService();
             PublisherWSCallBack portType = srv.getPublisherWSCallBackPort();
             WSBindingProvider bp = (WSBindingProvider) portType;

             bp.setAddress(address);
             bp.setOutboundHeaders(Headers.create(new QName(NS_ADDRESSING_2003,HEADER_RELATESTO),"11"));

              try {

            // Open an input stream
            FileInputStream fin = new FileInputStream("order1");
            // Close our input stream
            bookpublishercallback.SellReturnObj sro = new bookpublishercallback.SellReturnObj();

            String txt2 = new DataInputStream(fin).readLine();
            StringTokenizer st = new StringTokenizer(txt2, "|");

            bookpublishercallback.Book book2 = new bookpublishercallback.Book();
            String bookISBN2 = st.nextToken();
            book2.setISBN(bookISBN2);
            bookpublishercallback.Invoice invoice = new bookpublishercallback.Invoice();
            invoice.setBookISBN(bookISBN2);
            invoice.setBuyer(st.nextToken());
            sro.setInvoice(invoice);
            book2.setAuthor(st.nextToken());
            book2.setTitle(st.nextToken());
            book2.setPrice(new Double(st.nextToken()));
            sro.setBookInfo(book2);
            bookpublishercallback.Location location2 = new bookpublishercallback.Location();
            location2.setAddress(st.nextToken());
            location2.setCity(st.nextToken());
            location2.setCountry(st.nextToken());
            location2.setPostCode(st.nextToken());
            sro.setLocation(location2);

            fin.close();

            System.out.println("==========================="+address);
            portType.bookOrderDone(sro);
            System.out.println("==========================="+address);
        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);

        }
             

        } catch (IOException ex) {
            Logger.getLogger(PublisherWS.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public SellReturnObj bookOrderDone() {
        try {

            // Open an input stream
            FileInputStream fin = new FileInputStream("order1");
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
