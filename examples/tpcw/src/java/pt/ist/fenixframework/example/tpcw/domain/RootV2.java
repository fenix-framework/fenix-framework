package pt.ist.fenixframework.example.tpcw.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import pt.ist.fenixframework.example.tpcw.domain.dto.BookComparatorByDateDescendingAndTitle;
import pt.ist.fenixframework.example.tpcw.domain.dto.BookComparatorByTitle;

import pt.ist.fenixframework.pstm.Transaction;

/** Still incomplete... 
 * 
 * WARNING: RootV2 (this class) is an alternative implementation of the Root class.  This implementation only makes sense when
 * the database was populated from a clean state!!!
 *
 * This is to ensure that all objects domain ids are equal to their ID_INTERNAL.  If this condition is not met, then the code in
 * this class will have un unspecified behaviour.  The queries in this class are performed on the ID_INTERNAL, even though the
 * "id" arguments refer to the domain id.
 */
public class RootV2 extends Root_Base {
    private static BookComparatorByDateDescendingAndTitle BOOK_COMPARATOR_BY_DATE_DESC_AND_TITLE = new BookComparatorByDateDescendingAndTitle();
    private static BookComparatorByTitle BOOK_COMPARATOR_BY_TITLE = new BookComparatorByTitle();
    
    public  RootV2() {
        super();
    }

    public Customer getCustomer(int c_id) {
	return (Customer)Transaction.getDomainObject(Customer.class.getName(), c_id);
    }

    private List<Book> getBooksBySubject(String subject) {
	List<Book> books = new ArrayList<Book>();

	// filter subject
	for (Book book : getBooks()) {
	    String localSubject = book.getSubject();
	    if ((localSubject == subject) || (localSubject != null && localSubject.equals(subject))) {
		books.add(book);
	    }
	}
	return books;
    }

    public List<Book> getBooksSortedByTitle(String search_key, int limit) {
	List<Book> books = this.getBooksBySubject(search_key);
	// sort
	Collections.sort(books, BOOK_COMPARATOR_BY_TITLE);
	// limit result
 	return books.subList(0, Math.min(limit, books.size()));
    }

    public List<Book> getBooksSortedByPublicationDateAndTitle(String subject, int limit) {
	List<Book> books = this.getBooksBySubject(subject);
	// sort
	Collections.sort(books, BOOK_COMPARATOR_BY_DATE_DESC_AND_TITLE);
	// limit result
	return books.subList(0, Math.min(limit, books.size()));
    }

    public Book getBook(int i_id) {
	return (Book)Transaction.getDomainObject(Book.class.getName(), i_id);
    }
}
