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
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;

/**
 *
 * @author Shum
 */
@WebService()
public class PublisherWS {
 public List<Book> findBooks(String title, String author, String ISBN){
        //TODO add logic

        if(title == null){
            title = "";
        }
        if(author == null){
            author = "";
        }
        if(ISBN == null){
            ISBN = "";
        }
        ArrayList<Book> books = null;
        books = BooksDB.getInstance().getBooks(title, author, ISBN);
//        Book foundBook = new Book();
//        foundBook.setAuthor("Author1");
//        foundBook.setISBN("12345");
//        foundBook.setTitle("Title1");
//        books.add(foundBook);

        return books;
    }

    public SellReturnObj sellBook(String bookISBN, CreditCard card){
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

     public boolean purchaseCancel(String bookISBN, CreditCard card){

         //TODO add logic
         
         return true;
     }
}
