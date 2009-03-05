package pt.ist.fenixframework.example.tpcw.domain.dto;

import java.util.Comparator;

import pt.ist.fenixframework.example.tpcw.domain.Book;

public class BookComparatorByDateDescendingAndTitle implements Comparator<Book> {

    public int compare(Book b1, Book b2) {

	int cmp = b1.getPubDate().compareTo(b2.getPubDate());
	if (cmp == 0) {
	    return b1.getTitle().compareTo(b2.getTitle());
	} else {
	    return -cmp;
	}
    }
}
