/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bookpublisher.objects;

/**
 *
 * @author Shum
 */
public class Book {
    private String title;
    private String author;
    private String ISBN;

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



}
