/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bookpublisher.BooksDB;

import bookpublisher.objects.Book;
import java.util.ArrayList;

/**
 *
 * @author Shum
 */
public class BooksDB {
    private static BooksDB instance;

    private ArrayList<Book> books = new ArrayList<Book>();

    private BooksDB(){
        Book book1 = new Book();
        book1.setAuthor("Author1");
        book1.setISBN("111");
        book1.setTitle("Title1");
        book1.setPrice(100);
        books.add(book1);

        Book book2 = new Book();
        book2.setAuthor("Author2");
        book2.setISBN("222");
        book2.setTitle("Title2");
        book2.setPrice(50);
        books.add(book2);

        Book book3 = new Book();
        book3.setAuthor("Author3");
        book3.setISBN("333");
        book3.setTitle("Title3");
        book3.setPrice(100);
        books.add(book3);

        Book book4 = new Book();
        book4.setAuthor("Author4");
        book4.setISBN("444");
        book4.setTitle("Title4");
        book4.setPrice(100);
        books.add(book4);
    }

    public static BooksDB getInstance() {
        if (instance == null) {
            instance = new BooksDB();
        }
        return instance;
    }

    public ArrayList<Book> getBooks(String title, String author, String ISBN) {
        ArrayList<Book> returnBooks = new ArrayList<Book>();

        for(Book book : books){
            if((!author.equals("") && book.getAuthor().indexOf(author)>=0) ||
                    (!ISBN.equals("") && book.getISBN().indexOf(ISBN)>=0) ||
                    (!title.equals("") && book.getTitle().indexOf(title)>=0)){
                returnBooks.add(book);
            }
        }

         return returnBooks;
    }
}
