package test;

import org.hibernate.search.annotations.ContainedIn;

public class Publisher extends Publisher_Base {

    public Publisher() {
        super();

        toString();
    }

    public Publisher(String publisherName) {
        this();
        setPublisherName(publisherName);
    }

    //	@Override
    //	public String toString() {
    //		String books = " of";
    //		for (Book book : getPublishedBook()) {
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
    public java.util.Set<test.Book> getPublishedBookSet() {
        return super.getPublishedBookSet();
    }
}
