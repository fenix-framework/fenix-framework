package test;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.IndexedEmbedded;

public class Publisher extends Publisher_Base {

    public  Publisher() {
        super();

        // commented out due to ogm initialization bug
        //getBooksPublished();
    }

    public Publisher(String publisherName) {
        this();
        setPublisherName(publisherName);
    }

    //	@Override
    //	public String toString() {
    //		String books = " of";
    //		for (Book book : getBooksPublished()) {
    //			books += " " + book.getBookName();
    //		}
    //		return "Publisher " + getPublisherName() + " of " + books;
    //	}

    @Override
    public String toString() {
        return "Publisher " + getPublisherName();
    }

    @Override
    @ContainedIn
    public java.util.Set<test.Book> getBooksPublished() {
        return super.getBooksPublished();
    }
}
