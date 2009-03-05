package pt.ist.fenixframework.example.tpcw.domain.dto;

import java.sql.Date;
import java.util.Comparator;

import pt.ist.fenixframework.example.tpcw.domain.Book;

public class BookComparatorByTitle implements Comparator<Book> {
    
    public int compare(Book b1, Book b2) {
	return b1.getTitle().compareTo(b2.getTitle());
    }
}
