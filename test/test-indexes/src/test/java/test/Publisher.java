package test;

public class Publisher extends Publisher_Base {

    public  Publisher() {
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
    //		for (Book book : getBooksPublished()) {
    //			books += " " + book.getBookName();
    //		}
    //		return "Publisher " + getPublisherName() + " of " + books;
    //	}

    @Override
    public String toString() {
        return "Publisher " + getPublisherName();
    }
}
